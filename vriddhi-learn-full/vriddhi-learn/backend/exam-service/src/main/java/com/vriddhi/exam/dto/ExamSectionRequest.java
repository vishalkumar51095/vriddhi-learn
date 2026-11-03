package com.vriddhi.exam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ExamSectionRequest(
        @NotBlank String name,
        @Min(1) int questionCount,
        @NotEmpty List<UUID> topicIds
) {
}
