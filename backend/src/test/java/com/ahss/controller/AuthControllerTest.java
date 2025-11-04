package com.ahss.controller;

import com.ahss.dto.PermissionDto;
import com.ahss.dto.RoleDto;
import com.ahss.dto.UserDto;
import com.ahss.dto.request.LoginRequest;
import com.ahss.security.JwtTokenProvider;
import com.ahss.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.qameta.allure.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("Authentication")
@Feature("Login")
@Owner("backend")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        // Match controller's strength for consistent hashes
        encoder = new BCryptPasswordEncoder(12);
        clearInvocations(userService);
    }

    @Test
    @DisplayName("Login unknown user returns 401 and no audit calls")
    @Story("Login unknown user")
    void login_unknown_user_returns_401_and_no_audit_calls() throws Exception {

        Allure.step("Arrange unknown user", () -> {
            String email = "nouser@ahss.com";
            when(userService.getUserByUsernameOrEmail(eq(email))).thenReturn(Optional.empty());
            LoginRequest req = new LoginRequest(email, "SomePass@123");
            String body = objectMapper.writeValueAsString(req);

            Allure.step("Perform login request", () -> {
                try {
                    mockMvc.perform(post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.success", is(false)))
                            .andExpect(jsonPath("$.message", is("Invalid credentials")))
                            .andExpect(jsonPath("$.path", is("/api/v1/auth/login")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });

        Allure.step("Perform login response verification", () -> {
            verify(userService, never()).resetFailedLoginAttempts(anyLong());
            verify(userService, never()).updateLastLogin(anyLong());
            verify(userService, never()).incrementFailedLoginAttempts(anyLong());
        });
    }

    private UserDto activeUser(String email, String rawPassword) {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername("admin");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setPassword(encoder.encode(rawPassword));
        user.setUserStatus(com.ahss.entity.UserStatus.ACTIVE);

        RoleDto adminRole = new RoleDto();
        adminRole.setName("System Administrator");
        PermissionDto p = new PermissionDto();
        p.setName("payments:read");
        adminRole.setPermissions(List.of(p));
        user.setRoles(List.of(adminRole));
        return user;
    }

    @Test
    @DisplayName("Login success returns token and calls audit")
    @Story("Successful login generates JWT and audits login event")
    @Severity(SeverityLevel.CRITICAL)
    void login_success_returns_token_and_calls_audit() throws Exception {

        String email = "admin@ahss.com";
        String password = "Admin@123";

        UserDto user = Allure.step("Arrange active user and mock service", () -> activeUser(email, password));

        when(userService.getUserByUsernameOrEmail(eq(user.getEmail()))).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest(email, password);
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        MvcResult result = Allure.step("Perform login request", () -> mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.path", is("/api/v1/auth/login")))
                .andExpect(jsonPath("$.data.token", not(emptyString())))
                .andReturn());

        String responseJson = result.getResponse().getContentAsString();
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, responseJson);

        // Extract token value from a simple JSON parse
        String token = objectMapper
                .readTree(responseJson)
                .path("data")
                .path("token")
                .asText();
        Allure.addAttachment("JWT Token", MediaType.TEXT_PLAIN_VALUE, token);

        Allure.step("Parse JWT and verify claims", () -> {
            Claims claims = JwtTokenProvider.parse(token);
            Assertions.assertEquals(email, claims.getSubject());
            Assertions.assertEquals(1L, ((Number) claims.get("userId")).longValue());
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            Assertions.assertTrue(roles.contains("System Administrator"));
            Assertions.assertEquals(Boolean.TRUE, claims.get("isAdmin", Boolean.class));
        });

        Allure.step("Verify audit service interactions", () -> {
            verify(userService, times(1)).resetFailedLoginAttempts(eq(1L));
            verify(userService, times(1)).updateLastLogin(eq(1L));
            verify(userService, never()).incrementFailedLoginAttempts(anyLong());
        });
    }

    @Test
    @Story("Login invalid password returns 400 and increments failed attempts")
    @Severity(SeverityLevel.NORMAL)
    void login_invalid_password_returns_400_and_increments_failed_attempts() throws Exception {
        String email = "user@ahss.com";
        UserDto user = activeUser(email, "Correct@123");
        Allure.step("Stub service to return active user for email=" + email,
                () -> when(userService.getUserByUsernameOrEmail(eq(email))).thenReturn(Optional.of(user)));

        LoginRequest req = new LoginRequest(email, "Wrong@123");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        MvcResult result = Allure.step("POST /api/v1/auth/login with invalid password",
                () -> mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Invalid credentials")))
                        .andExpect(jsonPath("$.data.token").doesNotExist())
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());

        Allure.step("Verify audit service interactions", () -> {
            verify(userService, times(1)).incrementFailedLoginAttempts(eq(1L));
            verify(userService, never()).resetFailedLoginAttempts(anyLong());
            verify(userService, never()).updateLastLogin(anyLong());
        });
    }

    @Test
    @Story("Login locked account returns 400 without audit resets")
    @Severity(SeverityLevel.NORMAL)
    void login_locked_account_returns_400_without_audit_resets() throws Exception {
        String email = "locked@ahss.com";
        UserDto user = Allure.step("Prepare user email", () -> activeUser(email, "Secret@123"));
        Allure.step("Mark user account locked for +1 hour",
                () -> user.setAccountLockedUntil(LocalDateTime.now().plusHours(1)));
        Allure.step("Stub service to return locked user for email=" + email,
                () -> when(userService.getUserByUsernameOrEmail(eq(email))).thenReturn(Optional.of(user)));

        LoginRequest req = new LoginRequest(email, "Secret@123");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        MvcResult result = Allure.step("POST /api/v1/auth/login for locked user",
                () -> mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("temporarily locked")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
        Allure.step("Verify audit service interactions", () -> {
            verify(userService, never()).resetFailedLoginAttempts(anyLong());
            verify(userService, never()).updateLastLogin(anyLong());
            verify(userService, never()).incrementFailedLoginAttempts(anyLong());
        });
    }

    @Test
    @Story("Login inactive account returns 400 without audit resets")
    @Severity(SeverityLevel.MINOR)
    void login_inactive_account_returns_400_without_audit_resets() throws Exception {
        String email = "inactive@ahss.com";
        UserDto user = activeUser(email, "Secret@123");
        Allure.step("Mark user status INACTIVE", () -> user.setUserStatus(com.ahss.entity.UserStatus.INACTIVE));
        Allure.step("Stub service to return inactive user for email=" + email,
                () -> when(userService.getUserByUsernameOrEmail(eq(email))).thenReturn(Optional.of(user)));

        LoginRequest req = new LoginRequest(email, "Secret@123");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        MvcResult result = Allure.step("POST /api/v1/auth/login for inactive user",
                () -> mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("not active")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());

        verify(userService, never()).resetFailedLoginAttempts(anyLong());
        verify(userService, never()).updateLastLogin(anyLong());
        verify(userService, never()).incrementFailedLoginAttempts(anyLong());
    }

    @Test
    @Story("Login with missing fields returns 400 and message")
    @Severity(SeverityLevel.MINOR)
    void login_missing_fields_returns_400_and_message() throws Exception {
        LoginRequest req = new LoginRequest("", "");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        MvcResult result = Allure.step("POST /api/v1/auth/login with missing fields",
                () -> mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Username and password are required")))
                        .andExpect(jsonPath("$.path", is("/api/v1/auth/login")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());

        verifyNoInteractions(userService);
    }
}