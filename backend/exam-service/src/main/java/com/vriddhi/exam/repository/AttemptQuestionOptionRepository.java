package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.AttemptQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttemptQuestionOptionRepository extends JpaRepository<AttemptQuestionOption, UUID> {
    List<AttemptQuestionOption> findByAttemptQuestionIdOrderBySortOrder(UUID attemptQuestionId);
}
