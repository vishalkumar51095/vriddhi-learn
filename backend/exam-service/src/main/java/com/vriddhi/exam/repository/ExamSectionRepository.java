package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamSectionRepository extends JpaRepository<ExamSection, UUID> {
    List<ExamSection> findByExamIdOrderBySortOrder(UUID examId);
}
