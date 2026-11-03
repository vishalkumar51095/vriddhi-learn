package com.vriddhi.exam.controller;

import com.vriddhi.exam.dto.DailyMockStatus;
import com.vriddhi.exam.service.DailyMockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Today's timed mock. The attempt itself is then taken through the normal /api/attempts/{id}/... endpoints. */
@RestController
@RequestMapping("/api/attempts/daily")
@RequiredArgsConstructor
public class DailyMockController {

    private final DailyMockService dailyMockService;

    /** Starts today's mock for this category, or resumes / reports the one that already exists. */
    @PostMapping
    public DailyMockStatus start(@RequestParam String category, Authentication authentication) {
        return dailyMockService.startOrResume(userId(authentication), category);
    }

    @GetMapping("/today")
    public DailyMockStatus today(@RequestParam String category, Authentication authentication) {
        return dailyMockService.today(userId(authentication), category);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
