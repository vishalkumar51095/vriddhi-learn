package com.vriddhi.progress.repository;

import com.vriddhi.progress.entity.ProcessedAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedAttemptRepository extends JpaRepository<ProcessedAttempt, UUID> {
}
