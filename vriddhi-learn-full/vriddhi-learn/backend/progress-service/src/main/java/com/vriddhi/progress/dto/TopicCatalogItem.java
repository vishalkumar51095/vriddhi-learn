package com.vriddhi.progress.dto;

import java.util.UUID;

/** Mirrors content-service's PublishedTopicResponse (GET /internal/topics/published). */
public record TopicCatalogItem(
        UUID topicId,
        String topicName,
        String subjectName,
        String categoryCode,
        long publishedQuestions
) {
}
