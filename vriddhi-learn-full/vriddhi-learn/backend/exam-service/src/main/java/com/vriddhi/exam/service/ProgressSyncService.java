package com.vriddhi.exam.service;

import com.vriddhi.exam.client.ProgressClient;
import com.vriddhi.exam.entity.AnswerStatus;
import com.vriddhi.exam.entity.AttemptAnswer;
import com.vriddhi.exam.entity.AttemptQuestion;
import com.vriddhi.exam.entity.AttemptQuestionOption;
import com.vriddhi.exam.entity.AttemptStatus;
import com.vriddhi.exam.entity.ExamAttempt;
import com.vriddhi.exam.repository.AttemptAnswerRepository;
import com.vriddhi.exam.repository.AttemptQuestionOptionRepository;
import com.vriddhi.exam.repository.AttemptQuestionRepository;
import com.vriddhi.exam.repository.ExamAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Delivers the result of a finished attempt to progress-service so mocks feed the revision queue, topic
 * accuracy and streak just like practice does. Only answered, auto-gradable questions are reported;
 * skipped questions are left out so a timed-out mock does not drag accuracy down.
 * The two services have separate databases, so this is eventually consistent: a failed call leaves the
 * attempt PENDING and ProgressSyncScheduler retries it. progress-service ignores a repeated attemptId.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressSyncService {

    private final ExamAttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final AttemptQuestionOptionRepository attemptOptionRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final ProgressClient progressClient;

    public void sync(UUID attemptId) {
        ExamAttempt attempt = attemptRepository.findById(attemptId).orElse(null);
        if (attempt == null || attempt.getStatus() == AttemptStatus.IN_PROGRESS
                || !"PENDING".equals(attempt.getProgressSyncStatus())) {
            return;
        }
        try {
            List<ProgressClient.ResultItem> results = collectResults(attempt);
            if (!results.isEmpty()) {
                progressClient.postResults(attempt.getId(), attempt.getUserId(), results);
            }
            attemptRepository.updateProgressSyncStatus(attempt.getId(), results.isEmpty() ? "NOT_APPLICABLE" : "DONE");
        } catch (RuntimeException ex) {
            log.warn("Progress sync failed for attempt {} (it stays PENDING and will be retried): {}", attemptId, ex.getMessage());
        }
    }

    List<ProgressClient.ResultItem> collectResults(ExamAttempt attempt) {
        List<ProgressClient.ResultItem> results = new ArrayList<>();
        for (AttemptQuestion question : attemptQuestionRepository.findByAttemptIdOrderBySortOrder(attempt.getId())) {
            if (question.getTopicId() == null || isManuallyGraded(question.getQuestionType())) {
                continue;
            }
            AttemptAnswer answer = attemptAnswerRepository.findById(question.getId()).orElse(null);
            if (answer == null || !(answer.getStatus() == AnswerStatus.ANSWERED || answer.getStatus() == AnswerStatus.ANSWERED_MARKED)) {
                continue;
            }
            Set<UUID> correctIds = attemptOptionRepository.findByAttemptQuestionIdOrderBySortOrder(question.getId()).stream()
                    .filter(AttemptQuestionOption::isCorrect)
                    .map(AttemptQuestionOption::getId)
                    .collect(Collectors.toSet());
            Set<UUID> selectedIds = answer.getSelectedOptionIds() == null
                    ? Set.of()
                    : new HashSet<>(Arrays.asList(answer.getSelectedOptionIds()));
            results.add(new ProgressClient.ResultItem(
                    question.getQuestionId(),
                    question.getTopicId(),
                    Objects.requireNonNullElse(question.getTopicName(), "Topic"),
                    Objects.requireNonNullElse(question.getSubjectName(), "Subject"),
                    correctIds.equals(selectedIds)));
        }
        return results;
    }

    private boolean isManuallyGraded(String type) {
        return "FILL_BLANK".equals(type) || "CODING".equals(type) || "SUBJECTIVE".equals(type);
    }
}
