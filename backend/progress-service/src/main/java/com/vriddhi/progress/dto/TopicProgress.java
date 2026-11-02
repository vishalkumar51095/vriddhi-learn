package com.vriddhi.progress.dto;

import java.time.Instant;
import java.util.UUID;

public record TopicProgress(
        UUID topicId,
        String topicName,
        String subjectName,
        int attempts,
        int correct,
        double accuracy,
        Instant lastPracticedAt
) {
}
