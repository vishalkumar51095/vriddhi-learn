package com.vriddhi.auth.dao;

import com.vriddhi.auth.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * Read-side queries for the admin console. Plain SQL on purpose: these are
 * aggregations and filtered listings that read more clearly (and are easier to
 * test against a real PostgreSQL — see infra/tools/verify_sql.py, which executes
 * every statement below) than the equivalent JPQL. Parameters are CAST explicitly so
 * PostgreSQL never has to guess a type for a NULL/empty value.
 */
@Repository
@RequiredArgsConstructor
public class AdminQueryDao {

    private final NamedParameterJdbcTemplate jdbc;

    // test-params: {"q": "", "role": "", "status": "", "limit": 20, "offset": 0}
    private static final String SEARCH_USERS = """
            SELECT u.id, u.email, u.full_name, u.status, u.created_at, u.last_login_at,
                   COALESCE(string_agg(r.name, ',' ORDER BY r.name), '') AS roles
              FROM users u
              LEFT JOIN user_roles ur ON ur.user_id = u.id
              LEFT JOIN roles r ON r.id = ur.role_id
             WHERE (CAST(:q AS text) = ''
                    OR u.email ILIKE '%' || CAST(:q AS text) || '%'
                    OR u.full_name ILIKE '%' || CAST(:q AS text) || '%')
               AND (CAST(:status AS text) = '' OR u.status = CAST(:status AS text))
               AND (CAST(:role AS text) = '' OR EXISTS (
                       SELECT 1 FROM user_roles ur2 JOIN roles r2 ON r2.id = ur2.role_id
                        WHERE ur2.user_id = u.id AND r2.name = CAST(:role AS text)))
             GROUP BY u.id
             ORDER BY u.created_at DESC
             LIMIT :limit OFFSET :offset
            """;

    // test-params: {"q": "", "role": "", "status": ""}
    private static final String COUNT_USERS = """
            SELECT COUNT(*) FROM users u
             WHERE (CAST(:q AS text) = ''
                    OR u.email ILIKE '%' || CAST(:q AS text) || '%'
                    OR u.full_name ILIKE '%' || CAST(:q AS text) || '%')
               AND (CAST(:status AS text) = '' OR u.status = CAST(:status AS text))
               AND (CAST(:role AS text) = '' OR EXISTS (
                       SELECT 1 FROM user_roles ur2 JOIN roles r2 ON r2.id = ur2.role_id
                        WHERE ur2.user_id = u.id AND r2.name = CAST(:role AS text)))
            """;

    // test-params: {}
    private static final String USER_TOTALS = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE status = 'ACTIVE') AS active,
                   COUNT(*) FILTER (WHERE status = 'SUSPENDED') AS suspended,
                   COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
                   COUNT(*) FILTER (WHERE created_at >= now() - interval '7 days') AS new_last_7_days
              FROM users
            """;

    // test-params: {}
    private static final String USERS_BY_ROLE = """
            SELECT r.name, COUNT(ur.user_id) AS cnt
              FROM roles r LEFT JOIN user_roles ur ON ur.role_id = r.id
             GROUP BY r.name ORDER BY r.name
            """;

    // test-params: {"days": 14}
    private static final String REGISTRATIONS_PER_DAY = """
            SELECT CAST(g AS date) AS day,
                   (SELECT COUNT(*) FROM users u WHERE CAST(u.created_at AS date) = CAST(g AS date)) AS cnt
              FROM generate_series(CURRENT_DATE - (CAST(:days AS int) - 1), CURRENT_DATE, INTERVAL '1 day') AS g
             ORDER BY day
            """;

    // test-params: {}
    private static final String LOGIN_ACTIVITY = """
            SELECT COUNT(*) FILTER (WHERE event_type = 'LOGIN_SUCCESS') AS ok,
                   COUNT(*) FILTER (WHERE event_type IN ('LOGIN_FAILURE', 'LOGIN_DENIED')) AS failed
              FROM audit_logs
             WHERE created_at >= now() - interval '24 hours'
            """;

    // test-params: {"event": "", "q": "", "actorId": "", "limit": 20, "offset": 0}
    private static final String SEARCH_AUDIT = """
            SELECT id, actor_id, actor_email, event_type, target_type, target_id, details, ip_address, created_at
              FROM audit_logs
             WHERE (CAST(:event AS text) = '' OR event_type = CAST(:event AS text))
               AND (CAST(:actorId AS text) = '' OR CAST(actor_id AS text) = CAST(:actorId AS text))
               AND (CAST(:q AS text) = ''
                    OR actor_email ILIKE '%' || CAST(:q AS text) || '%'
                    OR details ILIKE '%' || CAST(:q AS text) || '%'
                    OR target_id ILIKE '%' || CAST(:q AS text) || '%')
             ORDER BY created_at DESC, id DESC
             LIMIT :limit OFFSET :offset
            """;

    // test-params: {"event": "", "q": "", "actorId": ""}
    private static final String COUNT_AUDIT = """
            SELECT COUNT(*) FROM audit_logs
             WHERE (CAST(:event AS text) = '' OR event_type = CAST(:event AS text))
               AND (CAST(:actorId AS text) = '' OR CAST(actor_id AS text) = CAST(:actorId AS text))
               AND (CAST(:q AS text) = ''
                    OR actor_email ILIKE '%' || CAST(:q AS text) || '%'
                    OR details ILIKE '%' || CAST(:q AS text) || '%'
                    OR target_id ILIKE '%' || CAST(:q AS text) || '%')
            """;

    // test-params: {}
    private static final String PUBLIC_USER_COUNTS = """
            SELECT COUNT(DISTINCT u.id) FILTER (WHERE r.name = 'LEARNER') AS learners,
                   COUNT(DISTINCT u.id) FILTER (WHERE r.name = 'TEACHER') AS teachers
              FROM users u
              JOIN user_roles ur ON ur.user_id = u.id
              JOIN roles r ON r.id = ur.role_id
             WHERE u.status = 'ACTIVE'
            """;

    public PagedResponse<AdminUserRow> searchUsers(String q, String role, String status, int page, int size) {
        var p = new MapSqlParameterSource().addValue("q", q).addValue("role", role).addValue("status", status)
                .addValue("limit", size).addValue("offset", page * size);
        List<AdminUserRow> rows = jdbc.query(SEARCH_USERS, p, (rs, i) -> new AdminUserRow(
                rs.getObject("id", UUID.class), rs.getString("email"), rs.getString("full_name"),
                rs.getString("status"), splitRoles(rs.getString("roles")),
                ts(rs, "created_at"), ts(rs, "last_login_at")));
        Long total = jdbc.queryForObject(COUNT_USERS, p, Long.class);
        return PagedResponse.of(rows, total == null ? 0 : total, page, size);
    }

    public UserStatsResponse userStats(int days) {
        long[] t = jdbc.queryForObject(USER_TOTALS, new MapSqlParameterSource(), (rs, i) ->
                new long[]{rs.getLong("total"), rs.getLong("active"), rs.getLong("suspended"),
                        rs.getLong("pending"), rs.getLong("new_last_7_days")});
        Map<String, Long> byRole = new LinkedHashMap<>();
        jdbc.query(USERS_BY_ROLE, new MapSqlParameterSource(), rs -> { byRole.put(rs.getString("name"), rs.getLong("cnt")); });
        List<DailyCount> perDay = jdbc.query(REGISTRATIONS_PER_DAY, new MapSqlParameterSource("days", days),
                (rs, i) -> new DailyCount(rs.getDate("day").toLocalDate(), rs.getLong("cnt")));
        long[] l = jdbc.queryForObject(LOGIN_ACTIVITY, new MapSqlParameterSource(),
                (rs, i) -> new long[]{rs.getLong("ok"), rs.getLong("failed")});
        return new UserStatsResponse(t[0], t[1], t[2], t[3], t[4], l[0], l[1], byRole, perDay);
    }

    public PagedResponse<AuditLogRow> searchAudit(String event, String q, String actorId, int page, int size) {
        var p = new MapSqlParameterSource().addValue("event", event).addValue("q", q).addValue("actorId", actorId)
                .addValue("limit", size).addValue("offset", page * size);
        List<AuditLogRow> rows = jdbc.query(SEARCH_AUDIT, p, (rs, i) -> new AuditLogRow(
                rs.getLong("id"), rs.getObject("actor_id", UUID.class), rs.getString("actor_email"),
                rs.getString("event_type"), rs.getString("target_type"), rs.getString("target_id"),
                rs.getString("details"), rs.getString("ip_address"), ts(rs, "created_at")));
        Long total = jdbc.queryForObject(COUNT_AUDIT, p, Long.class);
        return PagedResponse.of(rows, total == null ? 0 : total, page, size);
    }

    public PublicUserStats publicUserStats() {
        return jdbc.queryForObject(PUBLIC_USER_COUNTS, new MapSqlParameterSource(),
                (rs, i) -> new PublicUserStats(rs.getLong("learners"), rs.getLong("teachers")));
    }

    private static List<String> splitRoles(String csv) {
        return csv == null || csv.isBlank() ? List.of() : Arrays.asList(csv.split(","));
    }

    private static Instant ts(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }
}
