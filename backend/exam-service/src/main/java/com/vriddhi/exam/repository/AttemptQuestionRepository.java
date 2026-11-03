package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.AttemptQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, UUID> {
    List<AttemptQuestion> findByAttemptIdOrderBySortOrder(UUID attemptId);
}
