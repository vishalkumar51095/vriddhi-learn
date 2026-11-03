package com.vriddhi.exam.dto;

import java.util.List;
import java.util.UUID;

/** Mirrors progress-service's DailyPlan. The date stays a plain string; exam-service only needs the items. */
public record ProgressDailyPlanDto(
        String date,
        String categoryCode,
        int totalQuestions,
        List<Item> items
) {
    public record Item(
            String kind,
            UUID questionId,
            UUID topicId,
            String topicName,
            String subjectName,
            int count,
            String reason
    ) {
    }
}
