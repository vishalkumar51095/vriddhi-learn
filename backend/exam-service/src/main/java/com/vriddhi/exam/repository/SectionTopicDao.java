package com.vriddhi.exam.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * exam_section_topics is a pure join table (section_id, topic_id) with no
 * natural single id, so it's accessed via plain JDBC rather than mapped as
 * a JPA @Entity/repository.
 */
@Repository
@RequiredArgsConstructor
public class SectionTopicDao {

    private final JdbcTemplate jdbcTemplate;

    public List<UUID> findTopicIdsBySection(UUID sectionId) {
        return jdbcTemplate.queryForList(
                "select topic_id from exam_section_topics where section_id = ?", UUID.class, sectionId);
    }

    public void link(UUID sectionId, UUID topicId) {
        jdbcTemplate.update(
                "insert into exam_section_topics (section_id, topic_id) values (?, ?)", sectionId, topicId);
    }
}
