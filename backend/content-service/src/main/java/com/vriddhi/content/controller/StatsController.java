package com.vriddhi.content.controller;

import com.vriddhi.content.dao.ContentStatsDao;
import com.vriddhi.content.dto.StatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final ContentStatsDao dao;

    /** Unauthenticated aggregate counts for the public home page. */
    @GetMapping("/api/public/stats/content")
    public PublicContentStats publicStats() {
        return dao.publicStats();
    }

    /** ADMIN only (SecurityConfig: /api/admin/**). */
    @GetMapping("/api/admin/stats/content")
    public AdminContentStats adminStats() {
        return dao.adminStats(14);
    }
}
