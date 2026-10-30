package com.vriddhi.exam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exam_attempts")
@Getter
@Setter
@NoArgsConstructor
public class ExamAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    /** Server-enforced deadline — the client-side timer is purely a display; this field is authoritative. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(precision = 6, scale = 2)
    private BigDecimal score;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "skipped_count")
    private Integer skippedCount;

    @Column(name = "manual_pending_count", nullable = false)
    private int manualPendingCount;

    /** Highest global question sort_order visited so far — enforces FORWARD_ONLY server-side. */
    @Column(name = "max_sort_order_reached", nullable = false)
    private int maxSortOrderReached;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
