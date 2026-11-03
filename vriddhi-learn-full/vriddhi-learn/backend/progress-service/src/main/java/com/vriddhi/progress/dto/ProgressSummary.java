package com.vriddhi.progress.dto;

import java.util.List;

public record ProgressSummary(
        long totalAttempts,
        long totalCorrect,
        double accuracy,
        int streakDays,
        long answeredToday,
        long dueForReview,
        List<TopicProgress> topics
) {
}
