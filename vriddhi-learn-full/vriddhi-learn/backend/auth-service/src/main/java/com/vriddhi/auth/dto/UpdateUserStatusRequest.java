package com.vriddhi.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        String status // ACTIVE, INACTIVE, SUSPENDED, PENDING, DELETED
) {
}
