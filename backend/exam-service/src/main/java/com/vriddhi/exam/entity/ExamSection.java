package com.vriddhi.exam.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "exam_sections")
@Getter
@Setter
@NoArgsConstructor
public class ExamSection {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "question_count", nullable = false)
    private int questionCount;
}
