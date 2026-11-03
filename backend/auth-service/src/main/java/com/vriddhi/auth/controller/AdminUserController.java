package com.vriddhi.auth.controller;

import com.vriddhi.auth.dao.AdminQueryDao;
import com.vriddhi.auth.dto.UpdateUserRolesRequest;
import com.vriddhi.auth.dto.UpdateUserStatusRequest;
import com.vriddhi.auth.dto.UserResponse;
import com.vriddhi.auth.dto.admin.AdminUserRow;
import com.vriddhi.auth.dto.admin.PagedResponse;
import com.vriddhi.auth.entity.Role;
import com.vriddhi.auth.entity.User;
import com.vriddhi.auth.entity.UserStatus;
import com.vriddhi.auth.exception.BadRequestException;
import com.vriddhi.auth.exception.NotFoundException;
import com.vriddhi.auth.repository.RoleRepository;
import com.vriddhi.auth.repository.UserRepository;
import com.vriddhi.auth.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** ADMIN-only (SecurityConfig: /api/admin/**). Every change is written to the audit log. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminQueryDao adminQueryDao;
    private final AuditService auditService;

    @GetMapping
    public PagedResponse<AdminUserRow> list(@RequestParam(defaultValue = "") String q,
                                            @RequestParam(defaultValue = "") String role,
                                            @RequestParam(defaultValue = "") String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return adminQueryDao.searchUsers(q.trim(), role.trim().toUpperCase(), status.trim().toUpperCase(),
                Math.max(page, 0), safeSize);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    @Transactional
    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request,
                                     Authentication authentication) {
        UUID actorId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown status: " + request.status());
        }
        if (id.equals(actorId) && newStatus != UserStatus.ACTIVE) {
            throw new BadRequestException("You can't suspend or deactivate your own account");
        }
        UserStatus before = user.getStatus();
        user.setStatus(newStatus);
        User saved = userRepository.save(user);
        audit(actorId, "USER_STATUS_CHANGED", saved, before + " → " + newStatus);
        return UserResponse.from(saved);
    }

    /**
     * Replaces a user's entire role set, e.g. {"roles": ["LEARNER", "TEACHER"]}. The user must sign in
     * again afterwards: roles are baked into the JWT at issue time.
     */
    @Transactional
    @PatchMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable UUID id, @Valid @RequestBody UpdateUserRolesRequest request,
                                    Authentication authentication) {
        UUID actorId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        Set<Role> resolved = new HashSet<>();
        for (String roleName : request.roles()) {
            resolved.add(roleRepository.findByNameIgnoreCase(roleName)
                    .orElseThrow(() -> new BadRequestException("Unknown role: " + roleName)));
        }
        if (id.equals(actorId) && resolved.stream().noneMatch(r -> r.getName().equals("ADMIN"))) {
            throw new BadRequestException("You can't remove your own admin role");
        }

        Set<String> before = new TreeSet<>(user.roleNames());
        user.setRoles(resolved);
        User saved = userRepository.save(user);
        audit(actorId, "USER_ROLES_CHANGED", saved, before + " → " + new TreeSet<>(saved.roleNames()));
        return UserResponse.from(saved);
    }

    private void audit(UUID actorId, String event, User target, String details) {
        String actorEmail = userRepository.findById(actorId).map(User::getEmail).orElse(null);
        auditService.record(actorId, actorEmail, event, "USER", target.getId().toString(),
                target.getEmail() + ": " + details);
    }
}
