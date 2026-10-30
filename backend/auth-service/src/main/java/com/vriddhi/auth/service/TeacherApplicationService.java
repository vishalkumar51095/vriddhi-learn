package com.vriddhi.auth.service;

import com.vriddhi.auth.dao.TeacherApplicationDao;
import com.vriddhi.auth.dto.admin.PagedResponse;
import com.vriddhi.auth.dto.admin.TeacherApplicationRow;
import com.vriddhi.auth.entity.Role;
import com.vriddhi.auth.entity.User;
import com.vriddhi.auth.entity.UserStatus;
import com.vriddhi.auth.exception.BadRequestException;
import com.vriddhi.auth.exception.NotFoundException;
import com.vriddhi.auth.repository.RoleRepository;
import com.vriddhi.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/** Admin-side review of teacher applications. Approval is the ONLY place a self-registered user becomes a TEACHER. */
@Service
@RequiredArgsConstructor
public class TeacherApplicationService {

    private static final Set<String> STATUSES = Set.of("PENDING", "APPROVED", "REJECTED");

    private final TeacherApplicationDao dao;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    public PagedResponse<TeacherApplicationRow> list(String status, int page, int size) {
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!s.isEmpty() && !STATUSES.contains(s)) throw new BadRequestException("Unknown status: " + status);
        return dao.search(s, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    @Transactional
    public TeacherApplicationRow approve(UUID id, UUID actorId, String note) {
        TeacherApplicationRow app = requirePending(id);
        User user = userRepository.findById(app.userId()).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("This account is " + user.getStatus().name().toLowerCase()
                    + " — reactivate it before approving the application");
        }
        Role teacher = roleRepository.findByNameIgnoreCase("TEACHER")
                .orElseThrow(() -> new IllegalStateException("TEACHER role missing — check Flyway seed data"));

        if (!dao.markReviewed(id, "APPROVED", actorId, clean(note))) {
            throw new BadRequestException("This application has already been reviewed");
        }
        if (!user.roleNames().contains("TEACHER")) {
            user.getRoles().add(teacher);
            userRepository.save(user);
        }
        audit(actorId, "TEACHER_APPLICATION_APPROVED", app, "Approved; TEACHER role granted");
        return dao.findById(id).orElseThrow(() -> new NotFoundException("Application not found"));
    }

    @Transactional
    public TeacherApplicationRow reject(UUID id, UUID actorId, String note) {
        String reason = clean(note);
        if (reason == null || reason.length() < 3) throw new BadRequestException("Please give a reason for rejecting");
        TeacherApplicationRow app = requirePending(id);
        if (!dao.markReviewed(id, "REJECTED", actorId, reason)) {
            throw new BadRequestException("This application has already been reviewed");
        }
        audit(actorId, "TEACHER_APPLICATION_REJECTED", app, "Rejected: " + reason);
        return dao.findById(id).orElseThrow(() -> new NotFoundException("Application not found"));
    }

    private TeacherApplicationRow requirePending(UUID id) {
        TeacherApplicationRow app = dao.findById(id).orElseThrow(() -> new NotFoundException("Application not found"));
        if (!"PENDING".equals(app.status())) throw new BadRequestException("This application has already been reviewed");
        return app;
    }

    private static String clean(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private void audit(UUID actorId, String event, TeacherApplicationRow app, String details) {
        String actorEmail = userRepository.findById(actorId).map(User::getEmail).orElse(null);
        auditService.record(actorId, actorEmail, event, "TEACHER_APPLICATION", app.id().toString(),
                app.email() + ": " + details);
    }
}
