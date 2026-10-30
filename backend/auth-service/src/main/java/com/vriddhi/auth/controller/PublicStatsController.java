package com.vriddhi.auth.controller;

import com.vriddhi.auth.dao.AdminQueryDao;
import com.vriddhi.auth.dto.admin.PublicUserStats;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Unauthenticated, aggregate-only numbers for the public home page. No personal data. */
@RestController
@RequiredArgsConstructor
public class PublicStatsController {

    private final AdminQueryDao dao;

    @GetMapping("/api/public/stats/users")
    public PublicUserStats users() {
        return dao.publicUserStats();
    }
}
