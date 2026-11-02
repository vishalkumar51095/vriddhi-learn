package com.vriddhi.progress.repository;

import com.vriddhi.progress.entity.TopicStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicStatRepository extends JpaRepository<TopicStat, UUID> {

    Optional<TopicStat> findByUserIdAndTopicId(UUID userId, UUID topicId);

    List<TopicStat> findByUserId(UUID userId);
}
