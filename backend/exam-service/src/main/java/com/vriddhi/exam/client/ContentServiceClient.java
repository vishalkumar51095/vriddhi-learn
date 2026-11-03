package com.vriddhi.exam.client;

import com.vriddhi.exam.dto.ContentQuestionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

/**
 * Talks to content-service's /internal/** endpoints only — this is the one
 * synchronous cross-service call the exam engine makes, and only at attempt
 * start (to snapshot questions). Every check during the attempt itself is
 * served from the snapshot already stored locally, so a content-service
 * outage can't break an exam already in progress.
 */
@Component
@RequiredArgsConstructor
public class ContentServiceClient {

    private final WebClient contentServiceWebClient;

    public List<ContentQuestionDto> fetchRandomQuestionsForTopics(List<UUID> topicIds, int count) {
        return contentServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/questions/random")
                        // .toArray() so each id becomes its own repeated topicIds=...
                        // query param — passing the List itself here would be a single
                        // varargs element and get serialized as one bracketed string.
                        .queryParam("topicIds", (Object[]) topicIds.toArray(new UUID[0]))
                        .queryParam("count", count)
                        .build())
                .retrieve()
                .bodyToFlux(ContentQuestionDto.class)
                .collectList()
                .block();
    }
}
