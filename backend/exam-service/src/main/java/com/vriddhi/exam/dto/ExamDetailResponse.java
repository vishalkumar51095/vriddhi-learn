package com.vriddhi.exam.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ExamDetailResponse(
        UUID id,
        String title,
        String categoryCode,
        int durationMinutes,
        BigDecimal negativeMarking,
        String navigationMode,
        String status,
        List<SectionSummary> sections
) {
    public record SectionSummary(UUID id, String name, int questionCount) {}
}
