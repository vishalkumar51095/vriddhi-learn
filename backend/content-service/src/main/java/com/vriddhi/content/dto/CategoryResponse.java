package com.vriddhi.content.dto;

import java.util.UUID;

public record CategoryResponse(UUID id, String code, String name, String description) {
}
