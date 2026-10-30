package com.vriddhi.auth.dto;

import java.util.UUID;

/** Deliberately carries no tokens: applying does not sign you in as anything but you still have to log in. */
public record TeacherApplicationSubmittedResponse(UUID applicationId, String status, String email) {
}
