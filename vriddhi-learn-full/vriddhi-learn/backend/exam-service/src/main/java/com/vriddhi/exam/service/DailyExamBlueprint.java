package com.vriddhi.exam.service;

import com.vriddhi.exam.dto.ContentTopicDto;
import com.vriddhi.exam.dto.ProgressDailyPlanDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Pure logic (no Spring, no database) that turns a daily plan into exam sections and a time limit.
 * Kept separate so it can be unit-tested on its own.
 *
 * A plan item is either one specific question that is due for revision (kind QUESTION) or "N questions
 * from this topic" (kind TOPIC). Exam sections can only draw random questions from topics, so a due
 * question is represented by its topic here.
 */
public final class DailyExamBlueprint {

    public static final double MINUTES_PER_QUESTION = 1.5;
    public static final int MIN_MINUTES = 5;
    static final int FALLBACK_TOPICS = 5;
    static final int FALLBACK_PER_TOPIC = 2;

    private static final List<String> REASON_ORDER = List.of("DUE_FOR_REVIEW", "WEAK_TOPIC", "NEW_TOPIC", "MIXED_REVIEW");

    private DailyExamBlueprint() {
    }

    public record SectionSpec(String name, List<UUID> topicIds, int questionCount) {
    }

    /** One section per reason (Revision, Weak topics, New topics, Mixed review), in that fixed order. */
    public static List<SectionSpec> sectionsFromPlan(List<ProgressDailyPlanDto.Item> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Map<String, List<ProgressDailyPlanDto.Item>> byReason = new LinkedHashMap<>();
        for (String reason : REASON_ORDER) {
            byReason.put(reason, new ArrayList<>());
        }
        for (ProgressDailyPlanDto.Item item : items) {
            if (item.count() <= 0 || item.topicId() == null) {
                continue;
            }
            String key = byReason.containsKey(item.reason()) ? item.reason() : "MIXED_REVIEW";
            byReason.get(key).add(item);
        }
        List<SectionSpec> sections = new ArrayList<>();
        for (Map.Entry<String, List<ProgressDailyPlanDto.Item>> entry : byReason.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            LinkedHashSet<UUID> topics = new LinkedHashSet<>();
            int count = 0;
            for (ProgressDailyPlanDto.Item item : entry.getValue()) {
                topics.add(item.topicId());
                count += item.count();
            }
            sections.add(new SectionSpec(sectionName(entry.getKey()), new ArrayList<>(topics), count));
        }
        return sections;
    }

    /** Used when progress-service is unavailable: a seeded-random mix of topics from the category. */
    public static List<SectionSpec> fallbackSections(List<ContentTopicDto> catalog, String categoryCode, long seed) {
        List<ContentTopicDto> candidates = new ArrayList<>();
        if (catalog != null) {
            for (ContentTopicDto topic : catalog) {
                if (categoryCode == null || categoryCode.equals(topic.categoryCode())) {
                    candidates.add(topic);
                }
            }
        }
        if (candidates.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(candidates, new Random(seed));
        List<UUID> topicIds = new ArrayList<>();
        int total = 0;
        for (ContentTopicDto topic : candidates.subList(0, Math.min(FALLBACK_TOPICS, candidates.size()))) {
            int take = (int) Math.min(FALLBACK_PER_TOPIC, topic.publishedQuestions());
            if (take > 0) {
                topicIds.add(topic.topicId());
                total += take;
            }
        }
        return total == 0 ? List.of() : List.of(new SectionSpec(sectionName("MIXED_REVIEW"), topicIds, total));
    }

    public static int totalQuestions(List<SectionSpec> sections) {
        int total = 0;
        for (SectionSpec section : sections) {
            total += section.questionCount();
        }
        return total;
    }

    /** 1.5 minutes per question, never less than MIN_MINUTES (10 questions = 15 minutes). */
    public static int durationMinutes(int totalQuestions) {
        return Math.max(MIN_MINUTES, (int) Math.ceil(totalQuestions * MINUTES_PER_QUESTION));
    }

    static String sectionName(String reason) {
        return switch (reason) {
            case "DUE_FOR_REVIEW" -> "Revision";
            case "WEAK_TOPIC" -> "Weak topics";
            case "NEW_TOPIC" -> "New topics";
            default -> "Mixed review";
        };
    }
}
