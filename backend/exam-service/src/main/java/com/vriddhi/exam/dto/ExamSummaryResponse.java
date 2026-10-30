package com.vriddhi.exam.dto;

import java.util.UUID;

public record ExamSummaryResponse(
        UUID id,
        String title,
        String categoryCode,
        int durationMinutes,
        int sectionCount,
        int totalQuestions,
        String status
) {
}
