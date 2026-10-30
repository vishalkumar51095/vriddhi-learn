package com.vriddhi.content.controller;

import com.vriddhi.content.dto.CategoryResponse;
import com.vriddhi.content.dto.SubjectResponse;
import com.vriddhi.content.dto.TopicResponse;
import com.vriddhi.content.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/api/categories")
    public List<CategoryResponse> categories() {
        return catalogService.listCategories();
    }

    @GetMapping("/api/subjects")
    public List<SubjectResponse> subjects(@RequestParam String category) {
        return catalogService.subjectsForCategory(category);
    }

    @GetMapping("/api/topics")
    public List<TopicResponse> topics(@RequestParam("subjectId") UUID subjectId) {
        return catalogService.topicsForSubject(subjectId);
    }
}
