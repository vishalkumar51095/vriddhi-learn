package com.vriddhi.learning.dto;

import java.util.UUID;

public record LessonSummary(
        UUID id,
        String slug,
        String title,
        String categoryCode,
        String subjectName,
        UUID topicId,
        String topicName,
        int sortOrder,
        boolean completed
) {
}
