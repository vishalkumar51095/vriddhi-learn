package com.vriddhi.learning.service;

import com.vriddhi.learning.dto.LessonImportItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Parses a lesson file: a small "key: value" header between two --- lines, then the Markdown body.
 *
 *   ---
 *   slug: java-day-01-lambda
 *   title: Lambda and Functional Interfaces
 *   category: JAVA_INTERVIEW
 *   subject: Java 8 Essentials
 *   topic: Lambda & Functional Interfaces
 *   topicId: 2f0c...            (optional; the content-service topic id)
 *   order: 1
 *   ---
 */
public final class LessonFileParser {

    private LessonFileParser() {
    }

    public static LessonImportItem parse(String filename, String text) {
        String normalized = text.replace("\r\n", "\n");
        if (!normalized.startsWith("---\n")) {
            throw new IllegalArgumentException(filename + ": file must start with a --- front-matter block");
        }
        int end = normalized.indexOf("\n---\n", 3);
        if (end < 0) {
            throw new IllegalArgumentException(filename + ": front-matter block is not closed with ---");
        }
        String header = normalized.substring(4, end);
        String body = normalized.substring(end + 5).strip();

        Map<String, String> meta = new HashMap<>();
        for (String line : header.split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                meta.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
            }
        }
        String topicId = meta.get("topicId");
        return new LessonImportItem(
                require(meta, "slug", filename),
                require(meta, "title", filename),
                require(meta, "category", filename),
                require(meta, "subject", filename),
                topicId == null || topicId.isBlank() ? null : UUID.fromString(topicId),
                meta.get("topic"),
                Integer.parseInt(meta.getOrDefault("order", "0")),
                body);
    }

    private static String require(Map<String, String> meta, String key, String filename) {
        String value = meta.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(filename + ": missing '" + key + "' in front matter");
        }
        return value;
    }
}
