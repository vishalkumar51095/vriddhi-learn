package com.vriddhi.exam.controller;

import com.vriddhi.exam.dto.*;
import com.vriddhi.exam.service.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * The exam-taking API: start an attempt, fetch/answer one question at a
 * time, mark for review, read the palette, and submit — mirroring the
 * exam-engine prototype's flow one-to-one, but with the server as the
 * source of truth for the timer, navigation-mode rules, and scoring.
 */
@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping("/start")
    public ResponseEntity<StartAttemptResponse> start(@RequestParam UUID examId, Authentication auth) {
        UUID userId = userId(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(attemptService.start(examId, userId));
    }

    @GetMapping("/{attemptId}/questions/{attemptQuestionId}")
    public AttemptQuestionDetailResponse getQuestion(@PathVariable UUID attemptId,
                                                       @PathVariable UUID attemptQuestionId,
                                                       Authentication auth) {
        return attemptService.getQuestion(attemptId, userId(auth), attemptQuestionId);
    }

    @PutMapping("/{attemptId}/questions/{attemptQuestionId}/answer")
    public ResponseEntity<Void> answer(@PathVariable UUID attemptId,
                                        @PathVariable UUID attemptQuestionId,
                                        @RequestBody SubmitAnswerRequest request,
                                        Authentication auth) {
        attemptService.submitAnswer(attemptId, userId(auth), attemptQuestionId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{attemptId}/questions/{attemptQuestionId}/answer")
    public ResponseEntity<Void> clearAnswer(@PathVariable UUID attemptId,
                                             @PathVariable UUID attemptQuestionId,
                                             Authentication auth) {
        attemptService.clearAnswer(attemptId, userId(auth), attemptQuestionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{attemptId}/questions/{attemptQuestionId}/mark")
    public ResponseEntity<Void> mark(@PathVariable UUID attemptId,
                                      @PathVariable UUID attemptQuestionId,
                                      @Valid @RequestBody MarkForReviewRequest request,
                                      Authentication auth) {
        attemptService.markForReview(attemptId, userId(auth), attemptQuestionId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{attemptId}/palette")
    public PaletteResponse palette(@PathVariable UUID attemptId, Authentication auth) {
        return attemptService.getPalette(attemptId, userId(auth));
    }

    @PostMapping("/{attemptId}/submit")
    public AttemptResultResponse submit(@PathVariable UUID attemptId, Authentication auth) {
        return attemptService.submit(attemptId, userId(auth));
    }

    @GetMapping("/{attemptId}/result")
    public AttemptResultResponse result(@PathVariable UUID attemptId, Authentication auth) {
        return attemptService.getResult(attemptId, userId(auth));
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
