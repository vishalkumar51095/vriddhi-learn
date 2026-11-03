package com.vriddhi.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "question_versions")
@Getter
@Setter
@NoArgsConstructor
public class QuestionVersion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "fill_blank_answer", length = 500)
    private String fillBlankAnswer;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Options are fetched separately via QuestionOptionRepository — kept as a
    // plain foreign key on QuestionOption rather than a mapped association,
    // so the two tables can be queried independently in the service layer.
}
