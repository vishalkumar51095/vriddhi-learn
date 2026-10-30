package com.vriddhi.auth.controller;

import com.vriddhi.auth.dto.AuthResponse;
import com.vriddhi.auth.dto.LoginRequest;
import com.vriddhi.auth.dto.RefreshRequest;
import com.vriddhi.auth.dto.RegisterRequest;
import com.vriddhi.auth.dto.TeacherApplicationSubmittedResponse;
import com.vriddhi.auth.dto.TeacherRegisterRequest;
import com.vriddhi.auth.dto.UserResponse;
import com.vriddhi.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/register-teacher")
    public ResponseEntity<TeacherApplicationSubmittedResponse> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerTeacher(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(currentUserId(authentication)));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody com.vriddhi.auth.dto.UpdateProfileRequest request,
                                                   Authentication authentication) {
        return ResponseEntity.ok(authService.updateProfile(currentUserId(authentication), request));
    }

    private UUID currentUserId(Authentication authentication) {
        // JwtAuthFilter puts the token's subject (the user id) in as the principal.
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
