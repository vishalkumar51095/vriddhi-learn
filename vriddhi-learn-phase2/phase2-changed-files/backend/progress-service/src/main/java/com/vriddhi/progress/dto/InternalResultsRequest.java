package com.vriddhi.progress.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Sent by exam-service when an attempt finishes. attemptId makes the call idempotent. */
public record InternalResultsRequest(
        @NotNull UUID attemptId,
        @NotNull UUID userId,
        @NotEmpty List<@Valid RecordResultRequest> results
) {
}
