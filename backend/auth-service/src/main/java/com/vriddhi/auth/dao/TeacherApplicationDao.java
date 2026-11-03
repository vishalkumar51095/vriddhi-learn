package com.vriddhi.auth.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vriddhi.auth.dto.admin.PagedResponse;
import com.vriddhi.auth.dto.admin.TeacherApplicationRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Plain SQL for teacher applications (same approach as AdminQueryDao). The read statements carry
 * `test-params` so infra/tools/verify_sql.py runs them against a real PostgreSQL.
 */
@Repository
@RequiredArgsConstructor
public class TeacherApplicationDao {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final NamedParameterJdbcTemplate jdbc;

    // The two SELECTs repeat the column list on purpose: verify_sql.py only picks up self-contained text blocks.
    // test-params: {"status": "", "limit": 20, "offset": 0}
    private static final String SEARCH_APPLICATIONS = """
            SELECT a.id, a.user_id, u.full_name, u.email, CAST(a.subjects AS text) AS subjects,
                   a.experience_years, a.qualification, a.organization, a.bio, a.status, a.review_note,
                   rb.email AS reviewed_by_email, a.reviewed_at, a.created_at
              FROM teacher_applications a
              JOIN users u ON u.id = a.user_id
              LEFT JOIN users rb ON rb.id = a.reviewed_by
             WHERE (CAST(:status AS text) = '' OR a.status = CAST(:status AS text))
             ORDER BY a.created_at DESC, a.id DESC
             LIMIT :limit OFFSET :offset
            """;

    // test-params: {"status": ""}
    private static final String COUNT_APPLICATIONS = """
            SELECT COUNT(*) FROM teacher_applications a
             WHERE (CAST(:status AS text) = '' OR a.status = CAST(:status AS text))
            """;

    // test-params: {"id": "00000000-0000-0000-0000-000000000000"}
    private static final String FIND_APPLICATION = """
            SELECT a.id, a.user_id, u.full_name, u.email, CAST(a.subjects AS text) AS subjects,
                   a.experience_years, a.qualification, a.organization, a.bio, a.status, a.review_note,
                   rb.email AS reviewed_by_email, a.reviewed_at, a.created_at
              FROM teacher_applications a
              JOIN users u ON u.id = a.user_id
              LEFT JOIN users rb ON rb.id = a.reviewed_by
             WHERE a.id = CAST(:id AS uuid)
            """;

    private static final String INSERT_APPLICATION = """
            INSERT INTO teacher_applications (user_id, subjects, experience_years, qualification, organization, bio)
            VALUES (CAST(:userId AS uuid), CAST(:subjects AS jsonb), :years, :qualification, :organization, :bio)
            RETURNING id
            """;

    /** The status guard makes review idempotent-safe: a second reviewer gets 0 rows updated. */
    private static final String MARK_REVIEWED = """
            UPDATE teacher_applications
               SET status = :status, reviewed_by = CAST(:reviewer AS uuid), reviewed_at = now(), review_note = :note
             WHERE id = CAST(:id AS uuid) AND status = 'PENDING'
            """;

    public UUID insert(UUID userId, List<String> subjects, int years, String qualification, String organization, String bio) {
        try {
            return jdbc.queryForObject(INSERT_APPLICATION, new MapSqlParameterSource()
                            .addValue("userId", userId.toString())
                            .addValue("subjects", JSON.writeValueAsString(subjects))
                            .addValue("years", years)
                            .addValue("qualification", qualification)
                            .addValue("organization", organization)
                            .addValue("bio", bio),
                    (rs, i) -> rs.getObject("id", UUID.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise subjects", e);
        }
    }

    public PagedResponse<TeacherApplicationRow> search(String status, int page, int size) {
        var p = new MapSqlParameterSource().addValue("status", status)
                .addValue("limit", size).addValue("offset", page * size);
        List<TeacherApplicationRow> rows = jdbc.query(SEARCH_APPLICATIONS, p, (rs, i) -> map(rs));
        Long total = jdbc.queryForObject(COUNT_APPLICATIONS, p, Long.class);
        return PagedResponse.of(rows, total == null ? 0 : total, page, size);
    }

    public Optional<TeacherApplicationRow> findById(UUID id) {
        List<TeacherApplicationRow> rows = jdbc.query(FIND_APPLICATION,
                new MapSqlParameterSource("id", id.toString()), (rs, i) -> map(rs));
        return rows.stream().findFirst();
    }

    /** @return true if this call moved the application out of PENDING. */
    public boolean markReviewed(UUID id, String status, UUID reviewerId, String note) {
        return jdbc.update(MARK_REVIEWED, new MapSqlParameterSource()
                .addValue("id", id.toString())
                .addValue("status", status)
                .addValue("reviewer", reviewerId.toString())
                .addValue("note", note)) == 1;
    }

    private static TeacherApplicationRow map(ResultSet rs) throws SQLException {
        List<String> subjects;
        try {
            subjects = JSON.readValue(rs.getString("subjects"), new TypeReference<List<String>>() { });
        } catch (JsonProcessingException e) {
            subjects = List.of();
        }
        return new TeacherApplicationRow(
                rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
                rs.getString("full_name"), rs.getString("email"), subjects,
                rs.getInt("experience_years"), rs.getString("qualification"), rs.getString("organization"),
                rs.getString("bio"), rs.getString("status"), rs.getString("review_note"),
                rs.getString("reviewed_by_email"), ts(rs, "reviewed_at"), ts(rs, "created_at"));
    }

    private static Instant ts(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }
}
