package com.vriddhi.learning.service;

import com.vriddhi.learning.dto.LessonDetail;
import com.vriddhi.learning.dto.LessonSummary;
import com.vriddhi.learning.entity.Lesson;
import com.vriddhi.learning.entity.LessonCompletion;
import com.vriddhi.learning.exception.NotFoundException;
import com.vriddhi.learning.repository.LessonCompletionRepository;
import com.vriddhi.learning.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessons;
    private final LessonCompletionRepository completions;

    /** Published lessons, optionally narrowed by topic or category, each flagged with this learner's completion. */
    @Transactional(readOnly = true)
    public List<LessonSummary> list(UUID userId, String categoryCode, UUID topicId) {
        List<Lesson> found;
        if (topicId != null) {
            found = lessons.findByTopicIdAndStatusOrderBySortOrderAsc(topicId, Lesson.PUBLISHED);
        } else if (categoryCode != null && !categoryCode.isBlank()) {
            found = lessons.findByCategoryCodeAndStatusOrderBySortOrderAsc(categoryCode, Lesson.PUBLISHED);
        } else {
            found = lessons.findByStatusOrderBySortOrderAsc(Lesson.PUBLISHED);
        }
        Set<UUID> done = new HashSet<>(completions.findLessonIdsByUserId(userId));
        return found.stream().map(l -> toSummary(l, done.contains(l.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public LessonDetail get(String slug, UUID userId) {
        Lesson lesson = findPublished(slug);
        boolean completed = completions.existsByUserIdAndLessonId(userId, lesson.getId());
        return new LessonDetail(lesson.getId(), lesson.getSlug(), lesson.getTitle(), lesson.getCategoryCode(),
                lesson.getSubjectName(), lesson.getTopicId(), lesson.getTopicName(), lesson.getSortOrder(),
                completed, lesson.getBodyMarkdown());
    }

    @Transactional
    public void markComplete(String slug, UUID userId) {
        Lesson lesson = findPublished(slug);
        if (!completions.existsByUserIdAndLessonId(userId, lesson.getId())) {
            completions.save(new LessonCompletion(userId, lesson.getId()));
        }
    }

    @Transactional
    public void unmarkComplete(String slug, UUID userId) {
        Lesson lesson = findPublished(slug);
        completions.deleteByUserIdAndLessonId(userId, lesson.getId());
    }

    private Lesson findPublished(String slug) {
        return lessons.findBySlug(slug)
                .filter(l -> Lesson.PUBLISHED.equals(l.getStatus()))
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + slug));
    }

    private LessonSummary toSummary(Lesson l, boolean completed) {
        return new LessonSummary(l.getId(), l.getSlug(), l.getTitle(), l.getCategoryCode(), l.getSubjectName(),
                l.getTopicId(), l.getTopicName(), l.getSortOrder(), completed);
    }
}
