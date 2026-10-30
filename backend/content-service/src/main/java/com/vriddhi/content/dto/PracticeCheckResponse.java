package com.vriddhi.content.dto;

import java.util.List;
import java.util.UUID;

public record PracticeCheckResponse(
        boolean manualReview,
        Boolean correct,          // null when manualReview is true
        List<UUID> correctOptionIds,
        String explanation
) {
}
