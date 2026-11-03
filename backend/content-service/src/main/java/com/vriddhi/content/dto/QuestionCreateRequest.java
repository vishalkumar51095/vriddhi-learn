package com.vriddhi.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record QuestionCreateRequest(
        @NotNull(message = "Topic is required")
        UUID topicId,

        @NotNull(message = "Question type is required")
        String type, // SINGLE_CORRECT, MULTIPLE_CORRECT, TRUE_FALSE, FILL_BLANK, CODING, SUBJECTIVE

        @NotNull(message = "Difficulty is required")
        String difficulty, // EASY, MEDIUM, HARD

        @NotBlank(message = "Question text is required")
        String questionText,

        String codeSnippet,

        String explanation,

        String fillBlankAnswer,

        @Size(max = 8, message = "At most 8 options")
        List<OptionRequest> options,

        List<String> tags
) {
}
