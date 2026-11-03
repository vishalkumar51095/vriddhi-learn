package com.vriddhi.content.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Response shapes for the stats endpoints (public, admin, and teacher). */
public final class StatsDtos {
    private StatsDtos() {}

    public record PublicContentStats(long publishedQuestions, long subjects, long topics, long categories) {}

    public record DailyCount(LocalDate date, long count) {}

    public record AdminContentStats(long totalQuestions, long pendingReview,
                                    Map<String, Long> byStatus, Map<String, Long> byDifficulty,
                                    Map<String, Long> byType, List<DailyCount> createdPerDay) {}

    public record TeacherSummary(long total, Map<String, Long> byStatus) {}
}
