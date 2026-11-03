package com.vriddhi.content.dao;

import com.vriddhi.content.dto.StatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Aggregations for the public home page, the admin dashboard and the teacher dashboard. */
@Repository
@RequiredArgsConstructor
public class ContentStatsDao {

    private final NamedParameterJdbcTemplate jdbc;

    // test-params: {}
    private static final String PUBLIC_COUNTS = """
            SELECT (SELECT COUNT(*) FROM questions WHERE status = 'PUBLISHED') AS published_questions,
                   (SELECT COUNT(*) FROM subjects) AS subjects,
                   (SELECT COUNT(*) FROM topics) AS topics,
                   (SELECT COUNT(*) FROM categories) AS categories
            """;

    // test-params: {}
    private static final String BY_STATUS = "SELECT status AS k, COUNT(*) AS cnt FROM questions GROUP BY status ORDER BY status";

    // test-params: {}
    private static final String BY_DIFFICULTY = "SELECT difficulty AS k, COUNT(*) AS cnt FROM questions WHERE status = 'PUBLISHED' GROUP BY difficulty ORDER BY difficulty";

    // test-params: {}
    private static final String BY_TYPE = "SELECT type AS k, COUNT(*) AS cnt FROM questions WHERE status = 'PUBLISHED' GROUP BY type ORDER BY type";

    // test-params: {"days": 14}
    private static final String CREATED_PER_DAY = """
            SELECT CAST(g AS date) AS day,
                   (SELECT COUNT(*) FROM questions q WHERE CAST(q.created_at AS date) = CAST(g AS date)) AS cnt
              FROM generate_series(CURRENT_DATE - (CAST(:days AS int) - 1), CURRENT_DATE, INTERVAL '1 day') AS g
             ORDER BY day
            """;

    // test-params: {"uid": "00000000-0000-0000-0000-000000000001"}
    private static final String MINE_BY_STATUS = """
            SELECT status AS k, COUNT(*) AS cnt FROM questions
             WHERE created_by = CAST(:uid AS uuid) GROUP BY status ORDER BY status
            """;

    public PublicContentStats publicStats() {
        return jdbc.queryForObject(PUBLIC_COUNTS, new MapSqlParameterSource(), (rs, i) -> new PublicContentStats(
                rs.getLong("published_questions"), rs.getLong("subjects"), rs.getLong("topics"), rs.getLong("categories")));
    }

    public AdminContentStats adminStats(int days) {
        Map<String, Long> byStatus = counts(BY_STATUS, new MapSqlParameterSource());
        List<DailyCount> perDay = jdbc.query(CREATED_PER_DAY, new MapSqlParameterSource("days", days),
                (rs, i) -> new DailyCount(rs.getDate("day").toLocalDate(), rs.getLong("cnt")));
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new AdminContentStats(total, byStatus.getOrDefault("PENDING_REVIEW", 0L), byStatus,
                counts(BY_DIFFICULTY, new MapSqlParameterSource()), counts(BY_TYPE, new MapSqlParameterSource()), perDay);
    }

    public TeacherSummary teacherSummary(UUID userId) {
        Map<String, Long> byStatus = counts(MINE_BY_STATUS, new MapSqlParameterSource("uid", userId.toString()));
        return new TeacherSummary(byStatus.values().stream().mapToLong(Long::longValue).sum(), byStatus);
    }

    private Map<String, Long> counts(String sql, MapSqlParameterSource params) {
        Map<String, Long> out = new LinkedHashMap<>();
        jdbc.query(sql, params, rs -> { out.put(rs.getString("k"), rs.getLong("cnt")); });
        return out;
    }
}
