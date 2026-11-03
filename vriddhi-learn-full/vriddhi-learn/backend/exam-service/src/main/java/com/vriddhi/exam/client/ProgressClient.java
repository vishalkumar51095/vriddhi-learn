package com.vriddhi.exam.client;

import com.vriddhi.exam.dto.ProgressDailyPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** Calls progress-service's /internal endpoints (service-to-service, role EXAM_SERVICE). */
@Component
@RequiredArgsConstructor
public class ProgressClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient progressServiceWebClient;

    public ProgressDailyPlanDto dailyPlan(UUID userId, String category) {
        return progressServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/daily-plan")
                        .queryParam("userId", userId)
                        .queryParam("category", category)
                        .build())
                .retrieve()
                .bodyToMono(ProgressDailyPlanDto.class)
                .block(TIMEOUT);
    }

    /** Idempotent on the progress side: the same attemptId is only ever applied once. */
    public void postResults(UUID attemptId, UUID userId, List<ResultItem> results) {
        progressServiceWebClient.post()
                .uri("/internal/results")
                .bodyValue(new ResultsRequest(attemptId, userId, results))
                .retrieve()
                .toBodilessEntity()
                .block(TIMEOUT);
    }

    public record ResultItem(UUID questionId, UUID topicId, String topicName, String subjectName, boolean correct) {
    }

    public record ResultsRequest(UUID attemptId, UUID userId, List<ResultItem> results) {
    }
}
