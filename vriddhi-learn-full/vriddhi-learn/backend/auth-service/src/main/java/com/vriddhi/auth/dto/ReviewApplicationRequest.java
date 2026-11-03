package com.vriddhi.auth.dto;

import jakarta.validation.constraints.Size;

public record ReviewApplicationRequest(
        @Size(max = 500, message = "Note must be 500 characters or fewer")
        String note
) {
}
