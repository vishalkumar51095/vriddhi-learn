package com.vriddhi.progress.service;

import com.vriddhi.progress.client.ContentCatalogClient;
import com.vriddhi.progress.dto.DailyPlan;
import com.vriddhi.progress.dto.DailyPlanItem;
import com.vriddhi.progress.dto.TopicCatalogItem;
import com.vriddhi.progress.entity.ReviewState;
import com.vriddhi.progress.entity.TopicStat;
import com.vriddhi.progress.repository.ReviewStateRepository;
import com.vriddhi.progress.repository.TopicStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Builds today's practice set for one learner. Order of priority:
 *   1. questions due for spaced-repetition review (up to MAX_DUE),
 *   2. the weakest topics (at least MIN_ATTEMPTS answers and accuracy below WEAK_THRESHOLD),
 *   3. topics the learner has never practised, in catalog order,
 *   4. a seeded-random mix of the remaining topics.
 * The same learner gets the same topic mix all day (seeded by user id + date); the due questions naturally
 * disappear from the plan as they are answered because their next review moves into the future.
 */
@Service
@RequiredArgsConstructor
public class DailyPlanService {

    static final int TOTAL = 10;
    static final int MAX_DUE = 4;
    static final int WEAK_TOPICS = 2;
    static final int PER_TOPIC = 2;
    static final int MIN_ATTEMPTS = 3;
    static final double WEAK_THRESHOLD = 0.6;

    private final ContentCatalogClient catalogClient;
    private final ReviewStateRepository reviewStates;
    private final TopicStatRepository topicStats;

    public DailyPlan plan(UUID userId, String categoryCode) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        List<TopicCatalogItem> catalog = catalogClient.publishedTopics().stream()
                .filter(t -> categoryCode == null || categoryCode.isBlank() || categoryCode.equals(t.categoryCode()))
                .toList();
        Map<UUID, TopicCatalogItem> byId = new LinkedHashMap<>();
        for (TopicCatalogItem topic : catalog) {
            byId.put(topic.topicId(), topic);
        }

        List<DailyPlanItem> items = new ArrayList<>();
        int remaining = TOTAL;

        // 1) due for revision
        List<ReviewState> due = reviewStates.findByUserIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(
                userId, Instant.now(), PageRequest.of(0, MAX_DUE * 3));
        int dueAdded = 0;
        for (ReviewState state : due) {
            if (dueAdded >= MAX_DUE || remaining == 0) {
                break;
            }
            TopicCatalogItem topic = byId.get(state.getTopicId());
            if (topic == null) {
                continue; // different category, or the topic no longer has published questions
            }
            items.add(new DailyPlanItem("QUESTION", state.getQuestionId(), topic.topicId(), topic.topicName(),
                    topic.subjectName(), 1, "DUE_FOR_REVIEW"));
            dueAdded++;
            remaining--;
        }

        // 2) weak topics
        List<TopicStat> stats = topicStats.findByUserId(userId);
        Set<UUID> practiced = new HashSet<>();
        for (TopicStat stat : stats) {
            practiced.add(stat.getTopicId());
        }
        List<TopicStat> weak = stats.stream()
                .filter(s -> byId.containsKey(s.getTopicId()))
                .filter(s -> s.getAttempts() >= MIN_ATTEMPTS && s.accuracy() < WEAK_THRESHOLD)
                .sorted(Comparator.comparingDouble(TopicStat::accuracy))
                .limit(WEAK_TOPICS)
                .toList();
        Set<UUID> usedTopics = new HashSet<>();
        for (TopicStat stat : weak) {
            if (remaining == 0) {
                break;
            }
            DailyPlanItem item = topicItem(byId.get(stat.getTopicId()), Math.min(PER_TOPIC, remaining), "WEAK_TOPIC");
            items.add(item);
            usedTopics.add(item.topicId());
            remaining -= item.count();
        }

        // 3) never-practised topics, in catalog order (a learner usually works through topics in sequence)
        int freshAdded = 0;
        for (TopicCatalogItem topic : catalog) {
            if (remaining == 0 || freshAdded >= 2) {
                break;
            }
            if (practiced.contains(topic.topicId()) || usedTopics.contains(topic.topicId())) {
                continue;
            }
            DailyPlanItem item = topicItem(topic, Math.min(PER_TOPIC, remaining), "NEW_TOPIC");
            items.add(item);
            usedTopics.add(item.topicId());
            remaining -= item.count();
            freshAdded++;
        }

        // 4) seeded-random fill from the rest of the catalog
        Random random = new Random(Objects.hash(userId, today.toEpochDay()));
        List<TopicCatalogItem> pool = new ArrayList<>(catalog);
        Collections.shuffle(pool, random);
        for (TopicCatalogItem topic : pool) {
            if (remaining == 0) {
                break;
            }
            if (usedTopics.contains(topic.topicId())) {
                continue;
            }
            DailyPlanItem item = topicItem(topic, Math.min(PER_TOPIC, remaining), "MIXED_REVIEW");
            items.add(item);
            usedTopics.add(item.topicId());
            remaining -= item.count();
        }
        // very small catalog: top up from a topic already in the plan
        if (remaining > 0 && !pool.isEmpty()) {
            DailyPlanItem item = topicItem(pool.get(0), remaining, "MIXED_REVIEW");
            items.add(item);
            remaining -= item.count();
        }

        int total = items.stream().mapToInt(DailyPlanItem::count).sum();
        return new DailyPlan(today, categoryCode, total, items);
    }

    /** A TOPIC item never asks for more questions than the topic has published. */
    private DailyPlanItem topicItem(TopicCatalogItem topic, int wanted, String reason) {
        int count = (int) Math.max(1, Math.min(wanted, topic.publishedQuestions()));
        return new DailyPlanItem("TOPIC", null, topic.topicId(), topic.topicName(), topic.subjectName(), count, reason);
    }
}
