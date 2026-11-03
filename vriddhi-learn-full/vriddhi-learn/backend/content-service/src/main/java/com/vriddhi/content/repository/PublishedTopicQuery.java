package com.vriddhi.content.repository;

import com.vriddhi.content.dto.PublishedTopicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Read-only catalog query for other services (progress-service builds the daily plan from it).
 * Plain JDBC keeps the aggregate query simple and avoids projection alias-case surprises.
 */
@Repository
@RequiredArgsConstructor
public class PublishedTopicQuery {

    private static final String SQL = """
            SELECT t.id AS topic_id,
                   t.name AS topic_name,
                   s.name AS subject_name,
                   c.code AS category_code,
                   COUNT(q.id) AS published_questions
            FROM topics t
            JOIN subjects s ON s.id = t.subject_id
            JOIN categories c ON c.id = s.category_id
            JOIN questions q ON q.topic_id = t.id AND q.status = 'PUBLISHED'
            GROUP BY t.id, t.name, t.sort_order, s.name, s.sort_order, c.code
            ORDER BY c.code, s.sort_order, t.sort_order, t.name
            """;

    private final JdbcTemplate jdbc;

    public List<PublishedTopicResponse> findAll() {
        return jdbc.query(SQL, (rs, rowNum) -> new PublishedTopicResponse(
                rs.getObject("topic_id", UUID.class),
                rs.getString("topic_name"),
                rs.getString("subject_name"),
                rs.getString("category_code"),
                rs.getLong("published_questions")));
    }
}
