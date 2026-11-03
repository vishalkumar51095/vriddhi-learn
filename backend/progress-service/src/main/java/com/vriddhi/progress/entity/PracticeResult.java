package com.vriddhi.progress.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "practice_results")
@Getter
@Setter
@NoArgsConstructor
public class PracticeResult {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(name = "topic_name", nullable = false, length = 150)
    private String topicName;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt = Instant.now();
}
