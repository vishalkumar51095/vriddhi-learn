package com.vriddhi.progress.repository;

import com.vriddhi.progress.entity.PracticeResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PracticeResultRepository extends JpaRepository<PracticeResult, UUID> {

    long countByUserIdAndAnsweredAtGreaterThanEqual(UUID userId, Instant since);

    /** Distinct UTC calendar days (yyyy-MM-dd) on which the learner answered at least one question, newest first. */
    @Query(value = "SELECT DISTINCT to_char(answered_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS d "
            + "FROM practice_results WHERE user_id = :userId ORDER BY d DESC LIMIT 400", nativeQuery = true)
    List<String> findRecentPracticeDays(@Param("userId") UUID userId);
}
