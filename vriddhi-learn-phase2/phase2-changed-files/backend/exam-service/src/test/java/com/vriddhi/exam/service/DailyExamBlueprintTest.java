package com.vriddhi.exam.service;

import com.vriddhi.exam.dto.ContentTopicDto;
import com.vriddhi.exam.dto.ProgressDailyPlanDto;
import com.vriddhi.exam.service.DailyExamBlueprint.SectionSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyExamBlueprintTest {

    private static final UUID T1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID T2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID T3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static ProgressDailyPlanDto.Item item(String kind, UUID topic, int count, String reason) {
        return new ProgressDailyPlanDto.Item(kind, null, topic, "Topic", "Subject", count, reason);
    }

    @Test
    void planBecomesOneSectionPerReasonInFixedOrder() {
        List<ProgressDailyPlanDto.Item> items = List.of(
                item("TOPIC", T3, 2, "NEW_TOPIC"),
                item("QUESTION", T1, 1, "DUE_FOR_REVIEW"),
                item("QUESTION", T1, 1, "DUE_FOR_REVIEW"),
                item("TOPIC", T2, 2, "WEAK_TOPIC"));

        List<SectionSpec> sections = DailyExamBlueprint.sectionsFromPlan(items);

        assertEquals(3, sections.size());
        assertEquals("Revision", sections.get(0).name());
        assertEquals(2, sections.get(0).questionCount());
        assertEquals(List.of(T1), sections.get(0).topicIds()); // the same topic is listed once
        assertEquals("Weak topics", sections.get(1).name());
        assertEquals("New topics", sections.get(2).name());
        assertEquals(6, DailyExamBlueprint.totalQuestions(sections));
    }

    @Test
    void unknownReasonsAreTreatedAsMixedReviewAndEmptyItemsAreIgnored() {
        List<SectionSpec> sections = DailyExamBlueprint.sectionsFromPlan(List.of(
                item("TOPIC", T1, 3, "SOMETHING_NEW"),
                item("TOPIC", T2, 0, "WEAK_TOPIC")));

        assertEquals(1, sections.size());
        assertEquals("Mixed review", sections.get(0).name());
        assertEquals(3, sections.get(0).questionCount());
    }

    @Test
    void emptyPlanGivesNoSections() {
        assertTrue(DailyExamBlueprint.sectionsFromPlan(List.of()).isEmpty());
        assertTrue(DailyExamBlueprint.sectionsFromPlan(null).isEmpty());
    }

    @Test
    void durationIsOnePointFiveMinutesPerQuestionWithAFiveMinuteFloor() {
        assertEquals(15, DailyExamBlueprint.durationMinutes(10));
        assertEquals(12, DailyExamBlueprint.durationMinutes(8));
        assertEquals(5, DailyExamBlueprint.durationMinutes(2));
        assertEquals(5, DailyExamBlueprint.durationMinutes(0));
        assertEquals(8, DailyExamBlueprint.durationMinutes(5));
    }

    @Test
    void fallbackPicksTopicsOfTheRequestedCategoryOnly() {
        List<ContentTopicDto> catalog = List.of(
                new ContentTopicDto(T1, "A", "S", "JAVA", 9),
                new ContentTopicDto(T2, "B", "S", "JAVA", 9),
                new ContentTopicDto(T3, "C", "S", "OTHER", 9));

        List<SectionSpec> sections = DailyExamBlueprint.fallbackSections(catalog, "JAVA", 42L);

        assertEquals(1, sections.size());
        assertEquals(4, sections.get(0).questionCount()); // 2 topics x 2 questions
        assertEquals(2, sections.get(0).topicIds().size());
        assertTrue(!sections.get(0).topicIds().contains(T3));
    }

    @Test
    void fallbackNeverAsksForMoreQuestionsThanATopicHas() {
        List<ContentTopicDto> catalog = List.of(new ContentTopicDto(T1, "A", "S", "JAVA", 1));

        List<SectionSpec> sections = DailyExamBlueprint.fallbackSections(catalog, "JAVA", 1L);

        assertEquals(1, DailyExamBlueprint.totalQuestions(sections));
    }

    @Test
    void fallbackIsEmptyForAnUnknownCategoryAndStableForTheSameSeed() {
        List<ContentTopicDto> catalog = List.of(
                new ContentTopicDto(T1, "A", "S", "JAVA", 9),
                new ContentTopicDto(T2, "B", "S", "JAVA", 9),
                new ContentTopicDto(T3, "C", "S", "JAVA", 9));

        assertTrue(DailyExamBlueprint.fallbackSections(catalog, "NOPE", 1L).isEmpty());
        assertEquals(DailyExamBlueprint.fallbackSections(catalog, "JAVA", 7L), DailyExamBlueprint.fallbackSections(catalog, "JAVA", 7L));
    }
}
