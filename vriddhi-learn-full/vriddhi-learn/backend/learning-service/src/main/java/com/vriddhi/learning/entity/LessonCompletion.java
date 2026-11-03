package com.vriddhi.learning.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_completions")
@Getter
@Setter
@NoArgsConstructor
public class LessonCompletion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt = Instant.now();

    public LessonCompletion(UUID userId, UUID lessonId) {
        this.userId = userId;
        this.lessonId = lessonId;
    }
}
