package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.AttemptStatus;
import com.vriddhi.exam.entity.ExamAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, UUID> {
    Page<ExamAttempt> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);
    Optional<ExamAttempt> findByIdAndUserId(UUID id, UUID userId);
    List<ExamAttempt> findByStatus(AttemptStatus status);
}
