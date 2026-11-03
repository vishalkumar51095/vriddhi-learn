package com.vriddhi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vriddhi.auth.dto.LoginRequest;
import com.vriddhi.auth.dto.RegisterRequest;
import com.vriddhi.auth.dto.TeacherRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Uses an in-memory/test PostgreSQL instance (configure via application-test.yml
 * or Testcontainers) — not runnable in this sandbox, included as the intended
 * test shape for CI (doc section 22: automated unit/integration tests).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin_returnsTokensAndLearnerRole() throws Exception {
        RegisterRequest register = new RegisterRequest("Ananya Sharma", "ananya@example.com", "SecurePass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.roles[0]").value("LEARNER"));

        LoginRequest login = new LoginRequest("ananya@example.com", "SecurePass123", null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest register = new RegisterRequest("Rohit Verma", "rohit@example.com", "CorrectPass123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest badLogin = new LoginRequest("rohit@example.com", "WrongPassword", null);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerTeacher_createsPendingApplication_withoutTokensOrTeacherAccess() throws Exception {
        TeacherRegisterRequest apply = new TeacherRegisterRequest("Meera Iyer", "meera@example.com", "SecurePass123",
                List.of("Java", "Spring Boot"), 6, "MCA", "Example Institute",
                "I have taught Java and Spring Boot for six years and want to write practice questions.");

        mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apply)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        // Applying must not grant the role: the teacher portal stays closed until an admin approves.
        LoginRequest teacherPortal = new LoginRequest("meera@example.com", "SecurePass123", "TEACHER");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teacherPortal)))
                .andExpect(status().isForbidden());

        LoginRequest studentPortal = new LoginRequest("meera@example.com", "SecurePass123", "STUDENT");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentPortal)))
                .andExpect(status().isOk());
    }

    @Test
    void registerTeacher_withShortBio_returns400() throws Exception {
        TeacherRegisterRequest apply = new TeacherRegisterRequest("Short Bio", "shortbio@example.com", "SecurePass123",
                List.of("Java"), 2, "MCA", null, "Too short");

        mockMvc.perform(post("/api/auth/register-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apply)))
                .andExpect(status().isBadRequest());
    }
}
