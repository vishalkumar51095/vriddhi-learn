package com.vriddhi.learning.repository;

import com.vriddhi.learning.entity.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, UUID> {

    boolean existsByUserIdAndLessonId(UUID userId, UUID lessonId);

    void deleteByUserIdAndLessonId(UUID userId, UUID lessonId);

    @Query("select c.lessonId from LessonCompletion c where c.userId = :userId")
    List<UUID> findLessonIdsByUserId(@Param("userId") UUID userId);
}
