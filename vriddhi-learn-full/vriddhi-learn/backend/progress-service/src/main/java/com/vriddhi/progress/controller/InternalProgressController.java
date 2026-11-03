package com.vriddhi.progress.controller;

import com.vriddhi.progress.dto.DailyPlan;
import com.vriddhi.progress.dto.InternalResultsRequest;
import com.vriddhi.progress.dto.InternalResultsResponse;
import com.vriddhi.progress.service.DailyPlanService;
import com.vriddhi.progress.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service only. api-gateway does not route /internal/**, and SecurityConfig requires the
 * EXAM_SERVICE (or ADMIN) role. Unlike /api/progress, the learner id comes from the request here because
 * the caller is a trusted service acting for a learner.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalProgressController {

    private final ProgressService progressService;
    private final DailyPlanService dailyPlanService;

    @GetMapping("/daily-plan")
    public DailyPlan dailyPlan(@RequestParam UUID userId, @RequestParam(required = false) String category) {
        return dailyPlanService.plan(userId, category);
    }

    @PostMapping("/results")
    public InternalResultsResponse results(@Valid @RequestBody InternalResultsRequest request) {
        boolean applied = progressService.recordAttempt(request.attemptId(), request.userId(), request.results());
        return new InternalResultsResponse(applied);
    }
}
