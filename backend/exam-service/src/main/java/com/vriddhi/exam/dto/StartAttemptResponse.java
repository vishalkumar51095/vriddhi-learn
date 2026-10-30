package com.vriddhi.exam.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StartAttemptResponse(
        UUID attemptId,
        Instant expiresAt,
        String navigationMode,
        List<AttemptQuestionSummary> questions
) {
    public record AttemptQuestionSummary(
            UUID attemptQuestionId,
            UUID sectionId,
            String sectionName,
            int sortOrder,
            String status
    ) {}
}
