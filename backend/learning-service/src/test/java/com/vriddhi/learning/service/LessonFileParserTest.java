package com.vriddhi.learning.service;

import com.vriddhi.learning.dto.LessonImportItem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LessonFileParserTest {

    private static final String TOPIC_ID = "2f0c3d9e-1111-4222-8333-444455556666";

    private static final String VALID = String.join("\n",
            "---",
            "slug: java-day-01-lambda",
            "title: Lambdas and Functional Interfaces",
            "category: JAVA_INTERVIEW",
            "subject: Java 8 Essentials",
            "topic: Lambda & Functional Interfaces",
            "topicId: " + TOPIC_ID,
            "order: 1",
            "---",
            "",
            "## 1. Why lambdas?",
            "",
            "Body text.",
            "");

    @Test
    void parsesFrontMatterAndBody() {
        LessonImportItem item = LessonFileParser.parse("a.md", VALID);

        assertEquals("java-day-01-lambda", item.slug());
        assertEquals("Lambdas and Functional Interfaces", item.title());
        assertEquals("JAVA_INTERVIEW", item.categoryCode());
        assertEquals("Java 8 Essentials", item.subjectName());
        assertEquals("Lambda & Functional Interfaces", item.topicName());
        assertEquals(UUID.fromString(TOPIC_ID), item.topicId());
        assertEquals(1, item.sortOrder());
        assertEquals("## 1. Why lambdas?\n\nBody text.", item.bodyMarkdown());
    }

    @Test
    void topicIsOptionalForGeneralLessons() {
        String text = "---\nslug: s\ntitle: T\ncategory: C\nsubject: S\norder: 14\n---\n\nBody";
        LessonImportItem item = LessonFileParser.parse("b.md", text);

        assertNull(item.topicId());
        assertNull(item.topicName());
        assertEquals(14, item.sortOrder());
    }

    @Test
    void windowsLineEndingsAreAccepted() {
        LessonImportItem item = LessonFileParser.parse("c.md", VALID.replace("\n", "\r\n"));

        assertEquals("java-day-01-lambda", item.slug());
        assertEquals("## 1. Why lambdas?\n\nBody text.", item.bodyMarkdown());
    }

    @Test
    void missingFrontMatterIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> LessonFileParser.parse("d.md", "just text"));
    }

    @Test
    void unclosedFrontMatterIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> LessonFileParser.parse("e.md", "---\nslug: s\ntitle: T\n"));
    }

    @Test
    void requiredKeysAreEnforced() {
        String noSlug = "---\ntitle: T\ncategory: C\nsubject: S\n---\nBody";
        assertThrows(IllegalArgumentException.class, () -> LessonFileParser.parse("f.md", noSlug));
    }
}
