package com.vriddhi.learning.dto;

import java.util.UUID;

public record LessonDetail(
        UUID id,
        String slug,
        String title,
        String categoryCode,
        String subjectName,
        UUID topicId,
        String topicName,
        int sortOrder,
        boolean completed,
        String bodyMarkdown
) {
}
