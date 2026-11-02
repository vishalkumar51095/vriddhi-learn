package com.vriddhi.learning.controller;

import com.vriddhi.learning.dto.ImportRequest;
import com.vriddhi.learning.dto.ImportResult;
import com.vriddhi.learning.service.LessonImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminLessonController {

    private final LessonImportService importService;

    /** ADMIN only (see SecurityConfig). Idempotent: re-posting the same lessons changes nothing. */
    @PostMapping("/api/admin/lessons/import")
    public ImportResult importLessons(@Valid @RequestBody ImportRequest request) {
        return importService.importAll(request.lessons());
    }
}
