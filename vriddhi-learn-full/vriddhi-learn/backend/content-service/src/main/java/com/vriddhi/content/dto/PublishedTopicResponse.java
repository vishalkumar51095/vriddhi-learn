package com.vriddhi.content.dto;

import java.util.UUID;

/** A topic that has at least one PUBLISHED question, with its subject/category context. Internal use only. */
public record PublishedTopicResponse(
        UUID topicId,
        String topicName,
        String subjectName,
        String categoryCode,
        long publishedQuestions
) {
}
