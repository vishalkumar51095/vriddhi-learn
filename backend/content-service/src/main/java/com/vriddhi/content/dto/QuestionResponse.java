package com.vriddhi.content.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record QuestionResponse(
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
        List<QuestionOptionResponse> options,
        Set<String> tags,
        Instant updatedAt
) {
}
