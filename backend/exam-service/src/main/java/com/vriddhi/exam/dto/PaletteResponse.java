package com.vriddhi.exam.dto;

import java.util.List;
import java.util.UUID;

public record PaletteResponse(
        long secondsRemaining,
        List<PaletteItem> items
) {
    public record PaletteItem(UUID attemptQuestionId, String sectionName, int sortOrder, String status) {}
}
