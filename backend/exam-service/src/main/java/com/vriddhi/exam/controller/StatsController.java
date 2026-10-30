package com.vriddhi.exam.controller;

import com.vriddhi.exam.dao.ExamStatsDao;
import com.vriddhi.exam.dto.StatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final ExamStatsDao dao;

    /** Unauthenticated aggregate numbers for the public home page. */
    @GetMapping("/api/public/stats/exams")
    public PublicExamStats publicStats() {
        return dao.publicStats();
    }

    /** Unauthenticated list of published papers (title/duration/sections only) for the public home page. */
    @GetMapping("/api/public/exams")
    public List<PublicExam> publicExams() {
        return dao.publicExams();
    }

    /** ADMIN only (SecurityConfig: /api/admin/**). */
    @GetMapping("/api/admin/stats/exams")
    public AdminExamStats adminStats() {
        return dao.adminStats(14);
    }

    /** ADMIN only: every exam including drafts, with attempt counts. */
    @GetMapping("/api/admin/exams")
    public List<AdminExamRow> adminExams() {
        return dao.adminExams();
    }
}
