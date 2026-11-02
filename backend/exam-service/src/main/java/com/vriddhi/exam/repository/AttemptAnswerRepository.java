package com.vriddhi.exam.repository;

import com.vriddhi.exam.entity.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, UUID> {
}
