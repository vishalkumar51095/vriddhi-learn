package com.vriddhi.exam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attempt_answers")
@Getter
@Setter
@NoArgsConstructor
public class AttemptAnswer {

    @Id
    @Column(name = "attempt_question_id")
    private UUID attemptQuestionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnswerStatus status = AnswerStatus.NOT_VISITED;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "selected_option_ids", columnDefinition = "uuid[]")
    private UUID[] selectedOptionIds;

    @Column(name = "free_text_answer", columnDefinition = "TEXT")
    private String freeTextAnswer;

    @Column(name = "answered_at")
    private Instant answeredAt;
}
