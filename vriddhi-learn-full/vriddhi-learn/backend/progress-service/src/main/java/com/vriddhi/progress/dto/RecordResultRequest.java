package com.vriddhi.progress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RecordResultRequest(
        @NotNull UUID questionId,
        @NotNull UUID topicId,
        @NotBlank String topicName,
        @NotBlank String subjectName,
        @NotNull Boolean correct
) {
}
