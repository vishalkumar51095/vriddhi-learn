package com.vriddhi.progress.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Leitner-box state of one question for one learner. */
@Entity
@Table(name = "review_states")
@Getter
@Setter
@NoArgsConstructor
public class ReviewState {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(nullable = false)
    private int box = 1;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_correct", nullable = false)
    private boolean lastCorrect;

    @Column(name = "next_review_at", nullable = false)
    private Instant nextReviewAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
