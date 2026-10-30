package com.vriddhi.content.dto;

import jakarta.validation.constraints.NotBlank;

public record OptionRequest(
        @NotBlank(message = "Option text is required")
        String text,
        boolean correct
) {
}
