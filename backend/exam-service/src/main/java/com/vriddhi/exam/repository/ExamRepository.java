package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.Exam;
import com.vriddhi.exam.entity.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
    Page<Exam> findByStatusAndCategoryCode(ExamStatus status, String categoryCode, Pageable pageable);
    Page<Exam> findByStatus(ExamStatus status, Pageable pageable);
}
