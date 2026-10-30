package com.vriddhi.auth.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record AuditLogRow(long id, UUID actorId, String actorEmail, String eventType, String targetType,
                          String targetId, String details, String ipAddress, Instant createdAt) {
}
