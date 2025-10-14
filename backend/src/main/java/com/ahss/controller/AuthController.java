package com.ahss.controller;

import com.ahss.dto.UserDto;
import com.ahss.dto.request.LoginRequest;
import com.ahss.dto.response.ApiResponse;
import com.ahss.entity.UserStatus;
import com.ahss.security.JwtTokenProvider;
import com.ahss.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginRequest loginRequest) {
        log.debug("=== AuthController.login() - Starting authentication process ===");
        log.debug("Request body received: {}", loginRequest);

        try {

            log.debug("Extracted username: {}", loginRequest.username());
            log.debug("Password provided: {}", null != loginRequest.password() && !loginRequest.password().isEmpty() ? "Yes" : "No");

            if (null == loginRequest.username() || null == loginRequest.password() || loginRequest.username().trim().isEmpty() || loginRequest.password().trim().isEmpty()) {
                log.warn("Authentication failed: Missing username or password");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ok(null, "Username and password are required", "/api/v1/auth/login"));
            }
            final String username = loginRequest.username().trim().toLowerCase();

            Optional<UserDto> userOpt = userService.getUserByUsernameOrEmail(username);
            if (userOpt.isEmpty()) {
                log.warn("Authentication failed: User not found for username/email: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.notOk(null, "Invalid credentials", "/api/v1/auth/login"));
            }

            UserDto user = userOpt.get();
            log.debug("User found - ID: {}, Username: {}, Email: {}, Status: {}",
                    user.getId(), user.getUsername(), user.getEmail(), user.getUserStatus());
            log.debug("User has password set: {}", user.getPassword() != null && !user.getPassword().isEmpty());

            // Validate password using BCrypt
            boolean isValidPassword = false;
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                log.debug("Validating password with BCrypt");
                isValidPassword = passwordEncoder.matches(loginRequest.password(), user.getPassword());
                log.debug("Password validation result: {}", isValidPassword);
            } else {
                log.warn("User {} has no password set in database", loginRequest.username());
            }

            if (!isValidPassword) {
                log.warn("Authentication failed: Invalid password for user: {}", username);
                // Update failed login attempts
                userService.incrementFailedLoginAttempts(user.getId());
                log.debug("Incremented failed login attempts for user ID: {}", user.getId());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.notOk(null, "Invalid credentials", "/api/v1/auth/login"));
            }

            // Check if account is locked
            if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                log.warn("Authentication failed: Account locked until {} for user: {}",
                        user.getAccountLockedUntil(), username);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.notOk(null, "Account is temporarily locked. Please try again later.", "/api/v1/auth/login"));
            }

            // Check if account is active
            if (user.getUserStatus() != UserStatus.ACTIVE) {
                log.warn("Authentication failed: Account status is {} for user: {}",
                        user.getUserStatus(), username);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.notOk(null, "Account is not active. Please contact administrator.", "/api/v1/auth/login"));
            }

            log.debug("All authentication checks passed for user: {}", username);

            // Reset failed login attempts on successful login
            userService.resetFailedLoginAttempts(user.getId());
            log.debug("Reset failed login attempts for user ID: {}", user.getId());

            // Update last login timestamp
            userService.updateLastLogin(user.getId());
            log.debug("Updated last login timestamp for user ID: {}", user.getId());

            // Generate token with user information and roles
            log.debug("Generating JWT token for user: {}", username);
            String token = JwtTokenProvider.generateTokenWithUserInfo(user);
            log.debug("JWT token generated successfully. Token length: {}", token != null ? token.length() : 0);
            log.info("Authentication successful for user: {}", username);

            assert token != null;
            return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token), "Login successful", "/api/v1/auth/login"));

        } catch (Exception e) {
            log.error("ERROR in login process: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.notOk(null, "Internal server error", "/api/v1/auth/login"));
        }
    }
}
