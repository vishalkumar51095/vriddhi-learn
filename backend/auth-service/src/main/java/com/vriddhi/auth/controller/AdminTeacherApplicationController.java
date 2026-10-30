package com.vriddhi.auth.controller;

import com.vriddhi.auth.dto.ReviewApplicationRequest;
import com.vriddhi.auth.dto.admin.PagedResponse;
import com.vriddhi.auth.dto.admin.TeacherApplicationRow;
import com.vriddhi.auth.service.TeacherApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** ADMIN-only (SecurityConfig: /api/admin/**). Every decision is written to the audit log. */
@RestController
@RequestMapping("/api/admin/teacher-applications")
@RequiredArgsConstructor
public class AdminTeacherApplicationController {

    private final TeacherApplicationService service;

    @GetMapping
    public PagedResponse<TeacherApplicationRow> list(@RequestParam(defaultValue = "") String status,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return service.list(status, page, size);
    }

    @PostMapping("/{id}/approve")
    public TeacherApplicationRow approve(@PathVariable UUID id,
                                         @Valid @RequestBody(required = false) ReviewApplicationRequest request,
                                         Authentication authentication) {
        return service.approve(id, actorId(authentication), request == null ? null : request.note());
    }

    @PostMapping("/{id}/reject")
    public TeacherApplicationRow reject(@PathVariable UUID id,
                                        @Valid @RequestBody ReviewApplicationRequest request,
                                        Authentication authentication) {
        return service.reject(id, actorId(authentication), request.note());
    }

    private static UUID actorId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
