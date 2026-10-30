package com.vriddhi.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param portal optional: STUDENT, TEACHER or ADMIN. When present, the account must hold the
 *               matching role or the sign-in is refused (403) even with a correct password.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        String portal
) {
}
