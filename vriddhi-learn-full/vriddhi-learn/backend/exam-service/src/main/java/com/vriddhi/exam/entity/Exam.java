package com.vriddhi.exam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
public class Exam {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "negative_marking", nullable = false, precision = 4, scale = 2)
    private BigDecimal negativeMarking = new BigDecimal("0.25");

    @Enumerated(EnumType.STRING)
    @Column(name = "navigation_mode", nullable = false, length = 20)
    private NavigationMode navigationMode = NavigationMode.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamKind kind = ExamKind.STANDARD;

    /** Only set for DAILY exams: the one learner who may take it. */
    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    /** Only set for DAILY exams: the (UTC) day it was generated for. */
    @Column(name = "plan_date")
    private LocalDate planDate;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
