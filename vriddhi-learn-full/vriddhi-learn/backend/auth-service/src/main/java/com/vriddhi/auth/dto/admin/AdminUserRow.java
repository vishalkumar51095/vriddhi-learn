package com.vriddhi.auth.dto.admin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserRow(UUID id, String email, String fullName, String status, List<String> roles,
                           Instant createdAt, Instant lastLoginAt) {
}
