package com.vriddhi.progress.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topic_stats")
@Getter
@Setter
@NoArgsConstructor
public class TopicStat {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(name = "topic_name", nullable = false, length = 150)
    private String topicName;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "last_practiced_at", nullable = false)
    private Instant lastPracticedAt = Instant.now();

    public double accuracy() {
        return attempts == 0 ? 0.0 : (double) correctCount / attempts;
    }
}
