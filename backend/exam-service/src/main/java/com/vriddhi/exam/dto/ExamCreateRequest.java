package com.vriddhi.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ExamCreateRequest(
        @NotBlank String title,
        @NotBlank String categoryCode,
        @Min(1) int durationMinutes,
        @NotNull BigDecimal negativeMarking,
        @NotBlank String navigationMode, // FREE, FORWARD_ONLY, SECTION_LOCKED
        @NotEmpty @Valid List<ExamSectionRequest> sections
) {
}
