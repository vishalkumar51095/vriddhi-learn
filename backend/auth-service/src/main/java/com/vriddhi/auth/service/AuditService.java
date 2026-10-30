package com.vriddhi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Writes the audit trail. REQUIRES_NEW matters: a failed sign-in throws (and rolls back the
 * caller's transaction) right after being recorded, and the record of the failure must survive.
 * Auditing must never break the request it describes, so failures here are logged, not thrown.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final NamedParameterJdbcTemplate jdbc;

    // test-params: {"actorId": "", "actorEmail": "a@b.c", "event": "LOGIN_SUCCESS", "targetType": "USER", "targetId": "x", "details": "d", "ip": "127.0.0.1"}
    static final String INSERT_AUDIT = """
            INSERT INTO audit_logs (actor_id, actor_email, event_type, target_type, target_id, details, ip_address)
            VALUES (CAST(NULLIF(CAST(:actorId AS text), '') AS uuid), :actorEmail, :event, :targetType, :targetId, :details, :ip)
            """;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID actorId, String actorEmail, String event, String targetType, String targetId, String details) {
        try {
            jdbc.update(INSERT_AUDIT, new MapSqlParameterSource()
                    .addValue("actorId", actorId == null ? "" : actorId.toString())
                    .addValue("actorEmail", actorEmail)
                    .addValue("event", event)
                    .addValue("targetType", targetType)
                    .addValue("targetId", targetId)
                    .addValue("details", details)
                    .addValue("ip", clientIp()));
        } catch (Exception e) {
            log.warn("Could not write audit log {}: {}", event, e.getMessage());
        }
    }

    /** First hop of X-Forwarded-For (set by the gateway), else the socket address. */
    private static String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            var request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
            return request.getRemoteAddr();
        }
        return null;
    }
}
