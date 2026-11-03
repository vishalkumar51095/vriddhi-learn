package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.AttemptStatus;
import com.vriddhi.exam.entity.ExamAttempt;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, UUID> {
    Page<ExamAttempt> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);
    Optional<ExamAttempt> findByIdAndUserId(UUID id, UUID userId);
    List<ExamAttempt> findByStatus(AttemptStatus status);

    Optional<ExamAttempt> findFirstByExamIdAndUserIdOrderByStartedAtDesc(UUID examId, UUID userId);

    List<ExamAttempt> findByProgressSyncStatusAndSubmittedAtAfter(String progressSyncStatus, Instant after);

    // REQUIRES_NEW: this is called from an afterCommit callback, where joining the (already committed)
    // original transaction would silently drop the update.
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("update ExamAttempt a set a.progressSyncStatus = :status where a.id = :id")
    int updateProgressSyncStatus(@Param("id") UUID id, @Param("status") String status);
}
