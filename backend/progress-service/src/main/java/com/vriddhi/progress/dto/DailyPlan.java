package com.vriddhi.progress.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyPlan(
        LocalDate date,
        String categoryCode,
        int totalQuestions,
        List<DailyPlanItem> items
) {
}
