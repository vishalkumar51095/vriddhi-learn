package com.vriddhi.exam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "attempt_question_options")
@Getter
@Setter
@NoArgsConstructor
public class AttemptQuestionOption {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "attempt_question_id", nullable = false)
    private UUID attemptQuestionId;

    @Column(name = "option_text", nullable = false, length = 1000)
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
