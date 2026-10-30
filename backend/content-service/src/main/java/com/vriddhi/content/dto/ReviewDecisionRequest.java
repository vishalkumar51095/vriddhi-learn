package com.vriddhi.content.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewDecisionRequest(
        @NotBlank(message = "Decision is required")
        String decision, // APPROVE or REJECT
        String comment
) {
}
