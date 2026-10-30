package com.vriddhi.auth.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(
        @NotEmpty(message = "At least one role is required")
        Set<String> roles // e.g. ["LEARNER", "TEACHER"] — replaces the user's role set entirely
) {
}
