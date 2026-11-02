package com.vriddhi.content.dto;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String text,
        // Only populated for teacher/admin/review views — learner-facing
        // responses during an active attempt must never include this.
        Boolean correct
) {
}
