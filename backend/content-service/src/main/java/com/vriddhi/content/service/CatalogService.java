package com.vriddhi.content.service;

import com.vriddhi.content.dto.CategoryResponse;
import com.vriddhi.content.dto.SubjectResponse;
import com.vriddhi.content.dto.TopicResponse;
import com.vriddhi.content.exception.NotFoundException;
import com.vriddhi.content.repository.CategoryRepository;
import com.vriddhi.content.repository.SubjectRepository;
import com.vriddhi.content.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Backs the /subjects and /topics browsing endpoints used by preparation trees (doc section 3). */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getDescription()))
                .collect(Collectors.toList());
    }

    public List<SubjectResponse> subjectsForCategory(String categoryCode) {
        var category = categoryRepository.findByCodeIgnoreCase(categoryCode)
                .orElseThrow(() -> new NotFoundException("Unknown category: " + categoryCode));
        return subjectRepository.findByCategory_IdOrderBySortOrder(category.getId()).stream()
                .map(s -> new SubjectResponse(s.getId(), s.getName(), s.getDescription()))
                .collect(Collectors.toList());
    }

    public List<TopicResponse> topicsForSubject(UUID subjectId) {
        return topicRepository.findBySubject_IdOrderBySortOrder(subjectId).stream()
                .map(t -> new TopicResponse(t.getId(), subjectId, t.getName(), t.getDescription()))
                .collect(Collectors.toList());
    }
}
