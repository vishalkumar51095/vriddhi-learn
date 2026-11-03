package com.vriddhi.exam.service;

import com.vriddhi.exam.entity.ExamAttempt;
import com.vriddhi.exam.repository.ExamAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Retries attempts whose results could not be delivered to progress-service (it was down, or the call timed out). */
@Component
@RequiredArgsConstructor
public class ProgressSyncScheduler {

    private static final long RETRY_WINDOW_HOURS = 24;

    private final ExamAttemptRepository attemptRepository;
    private final ProgressSyncService progressSyncService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void retryPending() {
        Instant since = Instant.now().minus(RETRY_WINDOW_HOURS, ChronoUnit.HOURS);
        for (ExamAttempt attempt : attemptRepository.findByProgressSyncStatusAndSubmittedAtAfter("PENDING", since)) {
            progressSyncService.sync(attempt.getId());
        }
    }
}
