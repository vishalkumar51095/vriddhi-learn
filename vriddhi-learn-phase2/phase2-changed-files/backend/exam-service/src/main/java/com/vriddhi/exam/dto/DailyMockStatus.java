package com.vriddhi.exam.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * state: NOT_STARTED (nothing yet today), IN_PROGRESS (attemptId + expiresAt say how long is left) or
 * COMPLETED (submitted or timed out; open /api/attempts/{attemptId}/result for the full result).
 * score and correctCount are filled once the attempt has been finalized.
 */
public record DailyMockStatus(
        String state,
        UUID examId,
        UUID attemptId,
        Instant expiresAt,
        Integer durationMinutes,
        Integer totalQuestions,
        BigDecimal score,
        Integer correctCount
) {
    public static DailyMockStatus notStarted() {
        return new DailyMockStatus("NOT_STARTED", null, null, null, null, null, null, null);
    }
}
