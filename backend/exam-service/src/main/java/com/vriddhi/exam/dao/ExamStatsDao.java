package com.vriddhi.exam.dao;

import com.vriddhi.exam.dto.StatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ExamStatsDao {

    private final NamedParameterJdbcTemplate jdbc;

    // test-params: {}
    private static final String PUBLIC_COUNTS = """
            SELECT (SELECT COUNT(*) FROM exams WHERE status = 'PUBLISHED') AS published_exams,
                   (SELECT COUNT(*) FROM exam_attempts WHERE status <> 'IN_PROGRESS') AS attempts_taken
            """;

    // test-params: {}
    private static final String PUBLIC_EXAMS = """
            SELECT e.id, e.title, e.category_code, e.duration_minutes, e.navigation_mode,
                   (SELECT COUNT(*) FROM exam_sections s WHERE s.exam_id = e.id) AS sections,
                   COALESCE((SELECT SUM(s.question_count) FROM exam_sections s WHERE s.exam_id = e.id), 0) AS total_questions
              FROM exams e
             WHERE e.status = 'PUBLISHED'
             ORDER BY e.created_at, e.title
            """;

    // test-params: {}
    private static final String ADMIN_EXAMS = """
            SELECT e.id, e.title, e.category_code, e.duration_minutes, e.navigation_mode, e.status, e.created_at,
                   (SELECT COUNT(*) FROM exam_sections s WHERE s.exam_id = e.id) AS sections,
                   COALESCE((SELECT SUM(s.question_count) FROM exam_sections s WHERE s.exam_id = e.id), 0) AS total_questions,
                   (SELECT COUNT(*) FROM exam_attempts a WHERE a.exam_id = e.id) AS attempts,
                   (SELECT AVG(a.score) FROM exam_attempts a WHERE a.exam_id = e.id AND a.status <> 'IN_PROGRESS') AS avg_score
              FROM exams e
             ORDER BY e.created_at DESC, e.title
            """;

    // test-params: {}
    private static final String EXAMS_BY_STATUS = "SELECT status AS k, COUNT(*) AS cnt FROM exams GROUP BY status ORDER BY status";

    // test-params: {}
    private static final String ATTEMPT_TOTALS = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE status = 'IN_PROGRESS') AS in_progress,
                   COUNT(*) FILTER (WHERE status = 'SUBMITTED') AS submitted,
                   COUNT(*) FILTER (WHERE status = 'AUTO_SUBMITTED') AS auto_submitted,
                   AVG(score) FILTER (WHERE status <> 'IN_PROGRESS') AS avg_score
              FROM exam_attempts
            """;

    // test-params: {"days": 14}
    private static final String ATTEMPTS_PER_DAY = """
            SELECT CAST(g AS date) AS day,
                   (SELECT COUNT(*) FROM exam_attempts a WHERE CAST(a.started_at AS date) = CAST(g AS date)) AS cnt
              FROM generate_series(CURRENT_DATE - (CAST(:days AS int) - 1), CURRENT_DATE, INTERVAL '1 day') AS g
             ORDER BY day
            """;

    // test-params: {}
    private static final String TOP_EXAMS = """
            SELECT e.title, COUNT(a.id) AS attempts,
                   AVG(a.score) FILTER (WHERE a.status <> 'IN_PROGRESS') AS avg_score
              FROM exams e LEFT JOIN exam_attempts a ON a.exam_id = e.id
             GROUP BY e.id, e.title
             ORDER BY attempts DESC, e.title
             LIMIT 5
            """;

    public PublicExamStats publicStats() {
        return jdbc.queryForObject(PUBLIC_COUNTS, new MapSqlParameterSource(),
                (rs, i) -> new PublicExamStats(rs.getLong("published_exams"), rs.getLong("attempts_taken")));
    }

    public List<PublicExam> publicExams() {
        return jdbc.query(PUBLIC_EXAMS, new MapSqlParameterSource(), (rs, i) -> new PublicExam(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("category_code"),
                rs.getInt("duration_minutes"), rs.getString("navigation_mode"),
                rs.getInt("sections"), rs.getInt("total_questions")));
    }

    public List<AdminExamRow> adminExams() {
        return jdbc.query(ADMIN_EXAMS, new MapSqlParameterSource(), (rs, i) -> new AdminExamRow(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getString("category_code"),
                rs.getInt("duration_minutes"), rs.getString("navigation_mode"), rs.getString("status"),
                rs.getInt("sections"), rs.getInt("total_questions"), rs.getLong("attempts"),
                num(rs, "avg_score"), rs.getTimestamp("created_at").toInstant()));
    }

    public AdminExamStats adminStats(int days) {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        jdbc.query(EXAMS_BY_STATUS, new MapSqlParameterSource(), rs -> { byStatus.put(rs.getString("k"), rs.getLong("cnt")); });
        long totalExams = byStatus.values().stream().mapToLong(Long::longValue).sum();

        Object[] a = jdbc.queryForObject(ATTEMPT_TOTALS, new MapSqlParameterSource(), (rs, i) -> new Object[]{
                rs.getLong("total"), rs.getLong("in_progress"), rs.getLong("submitted"),
                rs.getLong("auto_submitted"), num(rs, "avg_score")});
        List<DailyCount> perDay = jdbc.query(ATTEMPTS_PER_DAY, new MapSqlParameterSource("days", days),
                (rs, i) -> new DailyCount(rs.getDate("day").toLocalDate(), rs.getLong("cnt")));
        List<TopExam> top = jdbc.query(TOP_EXAMS, new MapSqlParameterSource(),
                (rs, i) -> new TopExam(rs.getString("title"), rs.getLong("attempts"), num(rs, "avg_score")));

        return new AdminExamStats(totalExams, byStatus, (Long) a[0], (Long) a[1], (Long) a[2], (Long) a[3],
                (Double) a[4], perDay, top);
    }

    private static Double num(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v == null ? null : v.doubleValue();
    }
}
