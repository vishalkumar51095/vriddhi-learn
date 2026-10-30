package com.vriddhi.auth.dto.admin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TeacherApplicationRow(UUID id, UUID userId, String fullName, String email, List<String> subjects,
                                    int experienceYears, String qualification, String organization, String bio,
                                    String status, String reviewNote, String reviewedByEmail,
                                    Instant reviewedAt, Instant createdAt) {
}
