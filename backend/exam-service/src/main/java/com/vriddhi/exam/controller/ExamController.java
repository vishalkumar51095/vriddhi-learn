package com.vriddhi.exam.controller;

import com.vriddhi.exam.dto.ExamCreateRequest;
import com.vriddhi.exam.dto.ExamDetailResponse;
import com.vriddhi.exam.dto.ExamSummaryResponse;
import com.vriddhi.exam.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public ResponseEntity<ExamDetailResponse> create(@Valid @RequestBody ExamCreateRequest request, Authentication auth) {
        UUID createdBy = UUID.fromString((String) auth.getPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.create(request, createdBy));
    }

    @PostMapping("/{id}/publish")
    public ExamDetailResponse publish(@PathVariable UUID id) {
        return examService.publish(id);
    }

    @GetMapping("/{id}")
    public ExamDetailResponse get(@PathVariable UUID id) {
        return examService.getDetail(id);
    }

    @GetMapping
    public Page<ExamSummaryResponse> list(@RequestParam(required = false) String category,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return examService.listPublished(category, PageRequest.of(page, size));
    }
}
