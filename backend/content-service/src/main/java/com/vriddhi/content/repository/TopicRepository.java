package com.vriddhi.content.repository;

import com.vriddhi.content.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findBySubject_IdOrderBySortOrder(UUID subjectId);
}
