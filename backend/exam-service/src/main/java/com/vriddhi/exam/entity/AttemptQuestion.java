package com.vriddhi.exam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** An immutable snapshot of one question, taken at attempt-start time. */
@Entity
@Table(name = "attempt_questions")
@Getter
@Setter
@NoArgsConstructor
public class AttemptQuestion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "section_name", nullable = false, length = 150)
    private String sectionName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "question_version_id", nullable = false)
    private UUID questionVersionId;

    @Column(name = "question_type", nullable = false, length = 30)
    private String questionType;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "fill_blank_answer", length = 500)
    private String fillBlankAnswer;
}
