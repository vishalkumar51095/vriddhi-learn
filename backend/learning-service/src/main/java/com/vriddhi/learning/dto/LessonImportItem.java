package com.vriddhi.learning.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/** One lesson to insert or update, identified by its slug. topicId/topicName may be null for general lessons. */
public record LessonImportItem(
        @NotBlank String slug,
        @NotBlank String title,
        @NotBlank String categoryCode,
        @NotBlank String subjectName,
        UUID topicId,
        String topicName,
        int sortOrder,
        @NotBlank String bodyMarkdown
) {
}
