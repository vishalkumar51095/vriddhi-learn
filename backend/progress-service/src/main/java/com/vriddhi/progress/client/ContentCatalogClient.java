package com.vriddhi.progress.client;

import com.vriddhi.progress.dto.TopicCatalogItem;
import com.vriddhi.progress.exception.CatalogUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/** Reads content-service's /internal/topics/published (topics that currently have published questions). */
@Component
@RequiredArgsConstructor
public class ContentCatalogClient {

    private final WebClient contentServiceWebClient;

    public List<TopicCatalogItem> publishedTopics() {
        try {
            List<TopicCatalogItem> topics = contentServiceWebClient.get()
                    .uri("/internal/topics/published")
                    .retrieve()
                    .bodyToFlux(TopicCatalogItem.class)
                    .collectList()
                    .block();
            return topics == null ? List.of() : topics;
        } catch (RuntimeException ex) {
            throw new CatalogUnavailableException("content-service topic catalog call failed", ex);
        }
    }
}
