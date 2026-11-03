package com.vriddhi.content.dto;

import java.util.UUID;

public record TopicResponse(UUID id, UUID subjectId, String name, String description) {
}
