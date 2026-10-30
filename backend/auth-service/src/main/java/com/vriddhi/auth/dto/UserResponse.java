package com.vriddhi.auth.dto;

import com.vriddhi.auth.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String status,
        Set<String> roles,
        Instant createdAt,
        Instant lastLoginAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus().name(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
