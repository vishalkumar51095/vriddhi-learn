package com.vriddhi.content.repository;

import com.vriddhi.content.entity.Question;
import com.vriddhi.content.entity.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    Page<Question> findByTopic_IdAndStatus(UUID topicId, QuestionStatus status, Pageable pageable);

    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);

    Page<Question> findByCreatedBy(UUID createdBy, Pageable pageable);

    @Query("""
           select q from Question q
           where q.status = com.vriddhi.content.entity.QuestionStatus.PUBLISHED
             and q.topic.id in :topicIds
           order by function('random')
           """)
    List<Question> findRandomPublishedByTopics(@Param("topicIds") List<UUID> topicIds, Pageable pageable);
}
