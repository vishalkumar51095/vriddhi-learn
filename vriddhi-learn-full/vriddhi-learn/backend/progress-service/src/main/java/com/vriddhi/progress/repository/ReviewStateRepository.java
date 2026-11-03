package com.vriddhi.progress.repository;

import com.vriddhi.progress.entity.ReviewState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewStateRepository extends JpaRepository<ReviewState, UUID> {

    Optional<ReviewState> findByUserIdAndQuestionId(UUID userId, UUID questionId);

    long countByUserIdAndNextReviewAtLessThanEqual(UUID userId, Instant now);

    List<ReviewState> findByUserIdAndNextReviewAtLessThanEqualOrderByNextReviewAtAsc(UUID userId, Instant now, Pageable pageable);
}
