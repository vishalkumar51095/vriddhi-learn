package com.vriddhi.exam.dto;

import java.util.List;
import java.util.UUID;

/** What the exam-taking screen renders for one question — never includes which option is correct. */
public record AttemptQuestionDetailResponse(
        UUID attemptQuestionId,
        String sectionName,
        int sortOrder,
        String questionType,
        String questionText,
        String codeSnippet,
        List<OptionView> options,
        String status,
        List<UUID> selectedOptionIds,
        String freeTextAnswer,
        long secondsRemaining
) {
    public record OptionView(UUID id, String text) {}
}
