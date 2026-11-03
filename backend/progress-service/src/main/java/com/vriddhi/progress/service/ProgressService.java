package com.vriddhi.progress.service;

import com.vriddhi.progress.dto.ProgressSummary;
import com.vriddhi.progress.dto.RecordResultRequest;
import com.vriddhi.progress.dto.TopicProgress;
import com.vriddhi.progress.entity.PracticeResult;
import com.vriddhi.progress.entity.ReviewState;
import com.vriddhi.progress.entity.TopicStat;
import com.vriddhi.progress.repository.PracticeResultRepository;
import com.vriddhi.progress.repository.ReviewStateRepository;
import com.vriddhi.progress.repository.TopicStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final PracticeResultRepository results;
    private final TopicStatRepository topicStats;
    private final ReviewStateRepository reviewStates;

    /** Records one answered question: appends the result, updates the topic stats and moves the Leitner box. */
    @Transactional
    public void record(UUID userId, RecordResultRequest request) {
        boolean correct = Boolean.TRUE.equals(request.correct());
        Instant now = Instant.now();

        PracticeResult result = new PracticeResult();
        result.setUserId(userId);
        result.setQuestionId(request.questionId());
        result.setTopicId(request.topicId());
        result.setTopicName(request.topicName());
        result.setSubjectName(request.subjectName());
        result.setCorrect(correct);
        result.setAnsweredAt(now);
        results.save(result);

        TopicStat stat = topicStats.findByUserIdAndTopicId(userId, request.topicId()).orElseGet(() -> {
            TopicStat created = new TopicStat();
            created.setUserId(userId);
            created.setTopicId(request.topicId());
            return created;
        });
        stat.setTopicName(request.topicName());
        stat.setSubjectName(request.subjectName());
        stat.setAttempts(stat.getAttempts() + 1);
        if (correct) {
            stat.setCorrectCount(stat.getCorrectCount() + 1);
        }
        stat.setLastPracticedAt(now);
        topicStats.save(stat);

        ReviewState state = reviewStates.findByUserIdAndQuestionId(userId, request.questionId()).orElseGet(() -> {
            ReviewState created = new ReviewState();
            created.setUserId(userId);
            created.setQuestionId(request.questionId());
            created.setBox(Leitner.MIN_BOX);
            return created;
        });
        int newBox = Leitner.nextBox(state.getBox(), correct);
        state.setTopicId(request.topicId());
        state.setBox(newBox);
        state.setAttempts(state.getAttempts() + 1);
        state.setLastCorrect(correct);
        state.setNextReviewAt(now.plus(Leitner.interval(newBox)));
        state.setUpdatedAt(now);
        reviewStates.save(state);
    }

    @Transactional(readOnly = true)
    public ProgressSummary summary(UUID userId) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        List<TopicStat> stats = topicStats.findByUserId(userId);
        long attempts = stats.stream().mapToLong(TopicStat::getAttempts).sum();
        long correct = stats.stream().mapToLong(TopicStat::getCorrectCount).sum();
        List<TopicProgress> topics = stats.stream()
                .sorted(Comparator.comparingDouble(TopicStat::accuracy))
                .map(s -> new TopicProgress(s.getTopicId(), s.getTopicName(), s.getSubjectName(),
                        s.getAttempts(), s.getCorrectCount(), s.accuracy(), s.getLastPracticedAt()))
                .toList();

        long answeredToday = results.countByUserIdAndAnsweredAtGreaterThanEqual(
                userId, today.atStartOfDay().toInstant(ZoneOffset.UTC));
        long due = reviewStates.countByUserIdAndNextReviewAtLessThanEqual(userId, now);

        return new ProgressSummary(attempts, correct, attempts == 0 ? 0.0 : (double) correct / attempts,
                streakDays(userId, today), answeredToday, due, topics);
    }

    /** Consecutive UTC days with at least one answer, counting back from today (or from yesterday if today is empty). */
    private int streakDays(UUID userId, LocalDate today) {
        Set<LocalDate> days = new HashSet<>();
        for (String d : results.findRecentPracticeDays(userId)) {
            days.add(LocalDate.parse(d));
        }
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
