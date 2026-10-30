package com.vriddhi.exam.dto;

import java.util.List;
import java.util.UUID;

/** Mirrors content-service's QuestionResponse — the shape returned by /internal/questions/**. */
public record ContentQuestionDto(
        UUID id,
        UUID topicId,
        String topicName,
        String subjectName,
        String type,
        String difficulty,
        String status,
        int versionNumber,
        String questionText,
        String codeSnippet,
        String explanation,
        List<ContentOptionDto> options,
        List<String> tags
) {
    public record ContentOptionDto(UUID id, String text, Boolean correct) {}
}
