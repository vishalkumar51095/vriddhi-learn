package com.vriddhi.progress.dto;

import java.util.UUID;

/**
 * kind QUESTION: practise exactly questionId (it is due for revision). kind TOPIC: fetch `count` random
 * questions from topicId. reason is one of DUE_FOR_REVIEW, WEAK_TOPIC, NEW_TOPIC, MIXED_REVIEW.
 */
public record DailyPlanItem(
        String kind,
        UUID questionId,
        UUID topicId,
        String topicName,
        String subjectName,
        int count,
        String reason
) {
}
