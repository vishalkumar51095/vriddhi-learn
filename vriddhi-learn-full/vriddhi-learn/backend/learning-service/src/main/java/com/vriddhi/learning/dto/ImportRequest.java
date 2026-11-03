package com.vriddhi.learning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ImportRequest(@NotEmpty List<@Valid LessonImportItem> lessons) {
}
