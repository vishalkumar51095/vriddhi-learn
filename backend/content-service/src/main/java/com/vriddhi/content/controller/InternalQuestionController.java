package com.vriddhi.content.controller;

import com.vriddhi.content.dto.QuestionResponse;
import com.vriddhi.content.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Service-to-service only (see SecurityConfig: /internal/** requires the
 * EXAM_SERVICE role, granted to exam-service's own machine credentials, not
 * to any human user). Exam Service calls this when starting an attempt, to
 * snapshot full question + correct-answer data into attempt_answers so that
 * a later edit to the question can never change an in-progress or completed
 * attempt's scoring.
 */
@RestController
@RequestMapping("/internal/questions")
@RequiredArgsConstructor
public class InternalQuestionController {

    private final QuestionService questionService;

    @GetMapping("/{id}")
    public QuestionResponse getFullQuestion(@PathVariable UUID id) {
        return questionService.getById(id); // includes correct answers
    }

    @GetMapping("/random")
    public List<QuestionResponse> randomForTopics(@RequestParam List<UUID> topicIds,
                                                    @RequestParam(defaultValue = "10") int count) {
        return questionService.randomForInternal(topicIds, count);
    }
}
