package com.vriddhi.content.repository;

import com.vriddhi.content.entity.QuestionVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionVersionRepository extends JpaRepository<QuestionVersion, UUID> {
    List<QuestionVersion> findByQuestionIdOrderByVersionNumberDesc(UUID questionId);
}
