package com.vriddhi.learning.service;

import com.vriddhi.learning.dto.ImportResult;
import com.vriddhi.learning.dto.LessonImportItem;
import com.vriddhi.learning.entity.Lesson;
import com.vriddhi.learning.exception.BadRequestException;
import com.vriddhi.learning.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Idempotent bulk upsert keyed by slug. Running the same import twice changes nothing; changing a lesson's
 * text, title, ordering or topic updates exactly that lesson. Used by the startup loader (classpath files)
 * and by the ADMIN import endpoint (runtime, no redeploy).
 */
@Service
@RequiredArgsConstructor
public class LessonImportService {

    private final LessonRepository lessons;

    @Transactional
    public ImportResult importAll(List<LessonImportItem> items) {
        Set<String> seen = new HashSet<>();
        int created = 0;
        int updated = 0;
        int unchanged = 0;
        for (LessonImportItem item : items) {
            if (!seen.add(item.slug())) {
                throw new BadRequestException("Duplicate slug in import: " + item.slug());
            }
            String hash = hashOf(item);
            Optional<Lesson> existing = lessons.findBySlug(item.slug());
            if (existing.isEmpty()) {
                Lesson lesson = new Lesson();
                apply(lesson, item, hash);
                lessons.save(lesson);
                created++;
            } else if (!hash.equals(existing.get().getContentHash())) {
                apply(existing.get(), item, hash);
                updated++;
            } else {
                unchanged++;
            }
        }
        return new ImportResult(created, updated, unchanged);
    }

    private void apply(Lesson lesson, LessonImportItem item, String hash) {
        lesson.setSlug(item.slug());
        lesson.setTitle(item.title());
        lesson.setCategoryCode(item.categoryCode());
        lesson.setSubjectName(item.subjectName());
        lesson.setTopicId(item.topicId());
        lesson.setTopicName(item.topicName());
        lesson.setSortOrder(item.sortOrder());
        lesson.setBodyMarkdown(item.bodyMarkdown());
        lesson.setContentHash(hash);
        lesson.setStatus(Lesson.PUBLISHED);
        lesson.setUpdatedAt(Instant.now());
    }

    private String hashOf(LessonImportItem item) {
        String joined = String.join("\u0000", item.slug(), item.title(), item.categoryCode(), item.subjectName(),
                String.valueOf(item.topicId()), String.valueOf(item.topicName()), String.valueOf(item.sortOrder()),
                item.bodyMarkdown());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(joined.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
