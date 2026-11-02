package com.vriddhi.content.controller;

import com.vriddhi.content.dto.PublishedTopicResponse;
import com.vriddhi.content.repository.PublishedTopicQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service-to-service only: /internal/** is not routed by the gateway and SecurityConfig requires the
 * EXAM_SERVICE, PROGRESS_SERVICE or ADMIN role.
 */
@RestController
@RequestMapping("/internal/topics")
@RequiredArgsConstructor
public class InternalCatalogController {

    private final PublishedTopicQuery publishedTopics;

    @GetMapping("/published")
    public List<PublishedTopicResponse> published() {
        return publishedTopics.findAll();
    }
}
