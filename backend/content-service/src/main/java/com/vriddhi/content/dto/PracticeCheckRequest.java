package com.vriddhi.content.dto;

import java.util.List;
import java.util.UUID;

public record PracticeCheckRequest(
        List<UUID> selectedOptionIds,
        String freeTextAnswer
) {
}
