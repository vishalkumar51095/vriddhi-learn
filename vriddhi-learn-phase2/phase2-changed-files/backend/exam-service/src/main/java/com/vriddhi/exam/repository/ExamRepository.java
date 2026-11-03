package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.Exam;
import com.vriddhi.exam.entity.ExamStatus;
import org.springframework.data.domain.Page;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.vriddhi.exam.entity.ExamKind;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
    Page<Exam> findByStatusAndCategoryCode(ExamStatus status, String categoryCode, Pageable pageable);
    Page<Exam> findByStatus(ExamStatus status, Pageable pageable);

    Page<Exam> findByStatusAndKindAndCategoryCode(ExamStatus status, ExamKind kind, String categoryCode, Pageable pageable);

    Page<Exam> findByStatusAndKind(ExamStatus status, ExamKind kind, Pageable pageable);

    Optional<Exam> findByOwnerUserIdAndCategoryCodeAndPlanDateAndKind(UUID ownerUserId, String categoryCode,
                                                                      LocalDate planDate, ExamKind kind);

    /** Row lock: serializes concurrent "start my daily mock" requests so a learner never gets two attempts. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Exam e where e.id = :id")
    Optional<Exam> findByIdForUpdate(@Param("id") UUID id);
}
