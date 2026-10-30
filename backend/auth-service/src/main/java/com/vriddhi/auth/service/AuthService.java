package com.vriddhi.auth.service;

import com.vriddhi.auth.dao.TeacherApplicationDao;
import com.vriddhi.auth.dto.*;
import com.vriddhi.auth.entity.RefreshToken;
import com.vriddhi.auth.entity.Role;
import com.vriddhi.auth.entity.User;
import com.vriddhi.auth.entity.UserStatus;
import com.vriddhi.auth.exception.BadRequestException;
import com.vriddhi.auth.exception.ForbiddenException;
import com.vriddhi.auth.exception.NotFoundException;
import com.vriddhi.auth.exception.UnauthorizedException;
import com.vriddhi.auth.repository.RefreshTokenRepository;
import com.vriddhi.auth.repository.RoleRepository;
import com.vriddhi.auth.repository.UserRepository;
import com.vriddhi.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final TeacherApplicationDao teacherApplicationDao;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = createLearnerAccount(request.fullName(), request.email(), request.password());
        auditService.record(user.getId(), user.getEmail(), "REGISTER", "USER", user.getId().toString(), "Self-registration as learner");
        return issueTokens(user);
    }

    /**
     * "Apply to teach". Creates an ordinary LEARNER account and a PENDING application; TEACHER is granted only when an
     * administrator approves it (TeacherApplicationService), so this endpoint can't be used to self-assign a role.
     */
    @Transactional
    public TeacherApplicationSubmittedResponse registerTeacher(TeacherRegisterRequest request) {
        String bio = request.bio().trim();
        if (bio.length() < 30) throw new BadRequestException("Bio must be at least 30 characters");

        Map<String, String> unique = new LinkedHashMap<>();
        for (String subject : request.subjects()) {
            String trimmed = subject.trim();
            if (!trimmed.isEmpty()) unique.putIfAbsent(trimmed.toLowerCase(), trimmed);
        }
        if (unique.isEmpty()) throw new BadRequestException("Pick at least one subject");
        if (unique.size() > 8) throw new BadRequestException("Pick at most 8 subjects");

        User user = createLearnerAccount(request.fullName(), request.email(), request.password());
        String organization = request.organization() == null || request.organization().isBlank()
                ? null : request.organization().trim();
        UUID applicationId = teacherApplicationDao.insert(user.getId(), List.copyOf(unique.values()),
                request.experienceYears(), request.qualification().trim(), organization, bio);

        auditService.record(user.getId(), user.getEmail(), "REGISTER", "USER", user.getId().toString(),
                "Registered as learner with a teacher application");
        auditService.record(user.getId(), user.getEmail(), "TEACHER_APPLICATION_SUBMITTED", "TEACHER_APPLICATION",
                applicationId.toString(), "Subjects: " + String.join(", ", unique.values()));
        return new TeacherApplicationSubmittedResponse(applicationId, "PENDING", user.getEmail());
    }

    private User createLearnerAccount(String fullName, String rawEmail, String rawPassword) {
        String email = rawEmail.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        Role learnerRole = roleRepository.findByNameIgnoreCase("LEARNER")
                .orElseThrow(() -> new IllegalStateException("LEARNER role missing — check Flyway seed data"));

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        // New self-registrations start ACTIVE; flip to PENDING here if email verification is required.
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(new HashSet<>(List.of(learnerRole)));
        user.setLastLoginAt(Instant.now());

        // saveAndFlush: the teacher application is inserted with plain JDBC in the same transaction and
        // references this row, so it has to exist in the database before that statement runs.
        return userRepository.saveAndFlush(user);
    }

    /**
     * optional portal STUDENT, TEACHER or ADMIN. The password is checked first, so a wrong
     *                       password never reveals which roles an account holds.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        String portal = request.portal() == null || request.portal().isBlank() ? null : request.portal().trim().toUpperCase();
        String via = portal == null ? "" : " (" + portal.toLowerCase() + " portal)";

        Optional<User> found = userRepository.findByEmailIgnoreCase(email);
        if (found.isEmpty() || !passwordEncoder.matches(request.password(), found.get().getPasswordHash())) {
            auditService.record(found.map(User::getId).orElse(null), email, "LOGIN_FAILURE", "USER", null,
                    "Wrong email or password" + via);
            throw new UnauthorizedException("Invalid email or password");
        }
        User user = found.get();

        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            auditService.record(user.getId(), email, "LOGIN_DENIED", "USER", user.getId().toString(),
                    "Account is " + user.getStatus().name().toLowerCase() + via);
            throw new UnauthorizedException(user.getStatus() == UserStatus.SUSPENDED
                    ? "This account has been suspended. Please contact an administrator."
                    : "Invalid email or password");
        }

        if (portal != null) {
            Set<String> roles = user.roleNames();
            boolean allowed = switch (portal) {
                case "STUDENT" -> roles.contains("LEARNER");
                case "TEACHER" -> roles.contains("TEACHER");
                case "ADMIN" -> roles.contains("ADMIN") || roles.contains("SUPER_ADMIN");
                default -> throw new BadRequestException("Unknown portal: " + portal);
            };
            if (!allowed) {
                auditService.record(user.getId(), email, "LOGIN_DENIED", "USER", user.getId().toString(),
                        "Not authorised for the " + portal.toLowerCase() + " portal (roles: " + String.join(",", roles) + ")");
                throw new ForbiddenException(switch (portal) {
                    case "STUDENT" -> "This isn't a student account. Use the teacher or admin sign-in instead.";
                    case "TEACHER" -> "This account doesn't have teacher access yet. Apply to teach, or wait for an administrator to approve your application.";
                    default -> "This account doesn't have administrator access.";
                });
            }
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        auditService.record(user.getId(), email, "LOGIN_SUCCESS", "USER", user.getId().toString(), "Signed in" + via);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!stored.isValid()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            throw new UnauthorizedException("This account is no longer active");
        }

        // Rotate: revoke the used token and issue a brand new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        userRepository.findById(userId).ifPresent(u ->
                auditService.record(u.getId(), u.getEmail(), "LOGOUT", "USER", u.getId().toString(), "Signed out"));
    }

    public UserResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String before = user.getFullName();
        user.setFullName(request.fullName().trim());
        User saved = userRepository.save(user);
        auditService.record(saved.getId(), saved.getEmail(), "PROFILE_UPDATE", "USER", saved.getId().toString(),
                "Name changed from \"" + before + "\" to \"" + saved.getFullName() + "\"");
        return UserResponse.from(saved);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateOpaqueRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(sha256(rawRefreshToken))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtService.refreshTokenTtl()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, rawRefreshToken, jwtService.accessTokenTtlSeconds(), UserResponse.from(user));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
