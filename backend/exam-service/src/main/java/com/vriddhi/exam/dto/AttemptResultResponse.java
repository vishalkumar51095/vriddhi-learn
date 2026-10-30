package com.vriddhi.exam.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptResultResponse(
        UUID attemptId,
        String status, // SUBMITTED or AUTO_SUBMITTED
        BigDecimal score,
        int totalQuestions,
        int correctCount,
        int wrongCount,
        int skippedCount,
        int manualPendingCount,
        Instant startedAt,
        Instant submittedAt,
        List<SectionResult> sectionResults
) {
    public record SectionResult(String sectionName, int correct, int gradable) {}
}
