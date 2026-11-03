package com.vriddhi.learning.repository;

import com.vriddhi.learning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    Optional<Lesson> findBySlug(String slug);

    List<Lesson> findByStatusOrderBySortOrderAsc(String status);

    List<Lesson> findByCategoryCodeAndStatusOrderBySortOrderAsc(String categoryCode, String status);

    List<Lesson> findByTopicIdAndStatusOrderBySortOrderAsc(UUID topicId, String status);
}
