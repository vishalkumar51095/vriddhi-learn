package com.vriddhi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Public "apply to teach" form. Creates a learner account + a PENDING application — never a TEACHER directly. */
public record TeacherRegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 255, message = "Full name is too long")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotEmpty(message = "Pick at least one subject")
        @Size(max = 8, message = "Pick at most 8 subjects")
        List<@NotBlank(message = "Subject can't be blank") @Size(max = 60, message = "Subject name is too long") String> subjects,

        @NotNull(message = "Experience is required")
        @Min(value = 0, message = "Experience can't be negative")
        @Max(value = 60, message = "Experience must be 60 years or less")
        Integer experienceYears,

        @NotBlank(message = "Qualification is required")
        @Size(max = 200, message = "Qualification is too long")
        String qualification,

        @Size(max = 200, message = "Organisation is too long")
        String organization,

        @NotBlank(message = "Bio is required")
        @Size(min = 30, max = 600, message = "Bio must be between 30 and 600 characters")
        String bio
) {
}
