package com.vriddhi.exam.service;

import com.vriddhi.exam.entity.AttemptStatus;
import com.vriddhi.exam.entity.ExamAttempt;
import com.vriddhi.exam.repository.ExamAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Belt-and-suspenders alongside AttemptService's lazy expiry check: a
 * learner who closes the tab mid-exam still gets auto-submitted promptly,
 * without waiting for their next request (doc: "closing this tab does not
 * pause it").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttemptExpiryScheduler {

    private final ExamAttemptRepository attemptRepository;
    private final AttemptService attemptService;

    @Scheduled(fixedDelay = 30_000)
    public void sweepExpiredAttempts() {
        List<ExamAttempt> inProgress = attemptRepository.findByStatus(AttemptStatus.IN_PROGRESS);
        for (ExamAttempt attempt : inProgress) {
            if (attempt.isExpired()) {
                try {
                    attemptService.submit(attempt.getId(), attempt.getUserId());
                } catch (Exception e) {
                    log.warn("Failed to auto-submit expired attempt {}: {}", attempt.getId(), e.getMessage());
                }
            }
        }
    }
}
