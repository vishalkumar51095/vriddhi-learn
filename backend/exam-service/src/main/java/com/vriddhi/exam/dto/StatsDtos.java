package com.vriddhi.exam.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Response shapes for the public, admin and exam-listing endpoints backed by ExamStatsDao. */
public final class StatsDtos {
    private StatsDtos() {}

    public record PublicExamStats(long publishedExams, long attemptsTaken) {}

    public record PublicExam(UUID id, String title, String categoryCode, int durationMinutes,
                             String navigationMode, int sections, int totalQuestions) {}

    public record DailyCount(LocalDate date, long count) {}

    public record TopExam(String title, long attempts, Double avgScore) {}

    public record AdminExamStats(long totalExams, Map<String, Long> examsByStatus,
                                 long attemptsTotal, long attemptsInProgress, long attemptsSubmitted,
                                 long attemptsAutoSubmitted, Double avgScore,
                                 List<DailyCount> attemptsPerDay, List<TopExam> topExams) {}

    public record AdminExamRow(UUID id, String title, String categoryCode, int durationMinutes,
                               String navigationMode, String status, int sections, int totalQuestions,
                               long attempts, Double avgScore, Instant createdAt) {}
}
