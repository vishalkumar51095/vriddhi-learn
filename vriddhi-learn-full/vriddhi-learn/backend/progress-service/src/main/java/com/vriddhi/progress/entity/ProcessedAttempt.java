package com.vriddhi.progress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** An exam-service attempt whose results have already been applied. */
@Entity
@Table(name = "processed_attempts")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedAttempt {

    @Id
    @Column(name = "attempt_id")
    private UUID attemptId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt = Instant.now();

    public ProcessedAttempt(UUID attemptId) {
        this.attemptId = attemptId;
    }
}
