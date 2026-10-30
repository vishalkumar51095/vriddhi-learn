package com.vriddhi.content.repository;

import com.vriddhi.content.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByCategory_IdOrderBySortOrder(UUID categoryId);
}
