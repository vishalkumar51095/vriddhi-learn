package com.vriddhi.progress.controller;

import com.vriddhi.progress.dto.DailyPlan;
import com.vriddhi.progress.dto.ProgressSummary;
import com.vriddhi.progress.dto.RecordResultRequest;
import com.vriddhi.progress.service.DailyPlanService;
import com.vriddhi.progress.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final DailyPlanService dailyPlanService;

    /** Called by the frontend after a practice answer has been checked by content-service. */
    @PostMapping("/results")
    public ResponseEntity<Void> record(Authentication authentication, @Valid @RequestBody RecordResultRequest request) {
        progressService.record(userId(authentication), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ProgressSummary summary(Authentication authentication) {
        return progressService.summary(userId(authentication));
    }

    @GetMapping("/daily-plan")
    public DailyPlan dailyPlan(Authentication authentication, @RequestParam(required = false) String category) {
        return dailyPlanService.plan(userId(authentication), category);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
