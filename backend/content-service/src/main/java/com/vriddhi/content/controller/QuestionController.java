package com.vriddhi.content.controller;

import com.vriddhi.content.dto.PracticeCheckRequest;
import com.vriddhi.content.dto.PracticeCheckResponse;
import com.vriddhi.content.dto.QuestionCreateRequest;
import com.vriddhi.content.dto.QuestionResponse;
import com.vriddhi.content.dto.ReviewDecisionRequest;
import com.vriddhi.content.entity.QuestionStatus;
import com.vriddhi.content.dao.ContentStatsDao;
import com.vriddhi.content.dto.StatsDtos.TeacherSummary;
import com.vriddhi.content.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final ContentStatsDao statsDao;

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionCreateRequest request,
                                                     Authentication authentication) {
        UUID authorId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.create(request, authorId));
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody QuestionCreateRequest request,
                                    Authentication authentication) {
        UUID authorId = UUID.fromString((String) authentication.getPrincipal());
        return questionService.createNewVersion(id, request, authorId);
    }

    @PostMapping("/{id}/submit-for-review")
    public QuestionResponse submitForReview(@PathVariable UUID id) {
        return questionService.submitForReview(id);
    }

    @PostMapping("/{id}/review")
    public QuestionResponse review(@PathVariable UUID id, @Valid @RequestBody ReviewDecisionRequest request) {
        return questionService.review(id, request);
    }

    /** The signed-in teacher's own questions, newest first (includes drafts and correct answers). */
    @GetMapping("/mine")
    public Page<QuestionResponse> mine(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        Authentication authentication) {
        UUID me = UUID.fromString((String) authentication.getPrincipal());
        return questionService.listMine(me, PageRequest.of(page, Math.min(Math.max(size, 1), 100),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt")));
    }

    @GetMapping("/mine/summary")
    public TeacherSummary mineSummary(Authentication authentication) {
        return statsDao.teacherSummary(UUID.fromString((String) authentication.getPrincipal()));
    }

    /** Authoring view — includes correct answers. Requires TEACHER/ADMIN/CONTENT_REVIEWER (enforced upstream). */
    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable UUID id) {
        return questionService.getById(id);
    }

    /** Learner practice view — correct answers stripped, must be PUBLISHED. */
    @GetMapping("/{id}/practice")
    public QuestionResponse getForPractice(@PathVariable UUID id) {
        return questionService.getForLearner(id);
    }

    /** A random published question from the given topics, for practice mode. */
    @GetMapping("/random")
    public List<QuestionResponse> random(@RequestParam List<UUID> topicIds,
                                          @RequestParam(defaultValue = "1") int count) {
        return questionService.randomForLearner(topicIds, count);
    }

    /** Grades one practice attempt on the spot — no exam attempt is created. */
    @PostMapping("/{id}/practice/check")
    public PracticeCheckResponse checkPractice(@PathVariable UUID id, @RequestBody PracticeCheckRequest request) {
        return questionService.checkAnswer(id, request);
    }

    @GetMapping
    public Page<QuestionResponse> list(@RequestParam(required = false) UUID topicId,
                                        @RequestParam(defaultValue = "PUBLISHED") String status,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return questionService.list(topicId, QuestionStatus.valueOf(status), PageRequest.of(page, size));
    }
}
