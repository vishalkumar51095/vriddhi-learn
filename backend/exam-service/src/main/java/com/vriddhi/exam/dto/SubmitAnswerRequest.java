package com.vriddhi.exam.dto;

import java.util.List;
import java.util.UUID;

public record SubmitAnswerRequest(
        List<UUID> selectedOptionIds, // single/multi-correct
        String freeTextAnswer         // fill-blank/coding
) {
}
