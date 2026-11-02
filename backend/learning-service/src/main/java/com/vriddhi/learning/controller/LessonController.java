package com.vriddhi.learning.controller;

import com.vriddhi.learning.dto.LessonDetail;
import com.vriddhi.learning.dto.LessonSummary;
import com.vriddhi.learning.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/api/lessons")
    public List<LessonSummary> list(Authentication authentication,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) UUID topicId) {
        return lessonService.list(userId(authentication), category, topicId);
    }

    @GetMapping("/api/lessons/{slug}")
    public LessonDetail get(Authentication authentication, @PathVariable String slug) {
        return lessonService.get(slug, userId(authentication));
    }

    @PostMapping("/api/lessons/{slug}/complete")
    public ResponseEntity<Void> complete(Authentication authentication, @PathVariable String slug) {
        lessonService.markComplete(slug, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/lessons/{slug}/complete")
    public ResponseEntity<Void> uncomplete(Authentication authentication, @PathVariable String slug) {
        lessonService.unmarkComplete(slug, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
