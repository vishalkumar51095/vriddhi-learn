package com.vriddhi.exam.dto;

import java.util.UUID;

/** Mirrors content-service's PublishedTopicResponse. */
public record ContentTopicDto(
        UUID topicId,
        String topicName,
        String subjectName,
        String categoryCode,
        long publishedQuestions
) {
}
