package com.vriddhi.learning.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    public static final String PUBLISHED = "PUBLISHED";

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "category_code", nullable = false, length = 60)
    private String categoryCode;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    /** content-service topic this lesson belongs to (nullable: some lessons, such as a strategy guide, are general). */
    @Column(name = "topic_id")
    private UUID topicId;

    @Column(name = "topic_name", length = 150)
    private String topicName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "body_markdown", nullable = false, columnDefinition = "TEXT")
    private String bodyMarkdown;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false, length = 20)
    private String status = PUBLISHED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
