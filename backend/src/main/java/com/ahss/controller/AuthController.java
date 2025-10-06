package com.ahss.controller;

import com.ahss.dto.UserDto;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ok(null, "Username and password are required", "/api/v1/auth/login"));
            }
            

            
            Optional<UserDto> userOpt = userService.getUserByUsernameOrEmail(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.notOk(null, "Invalid credentials", "/api/v1/auth/login"));
            }
            
            UserDto user = userOpt.get();
        
        // Validate password using BCrypt
        boolean isValidPassword = false;
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            log.info("Password from request: {}", password);
            log.info("Password from database: {}", user.getPassword());
            isValidPassword = passwordEncoder.matches(password, user.getPassword());
        }
        
        if (!isValidPassword) {
            // Update failed login attempts
            userService.incrementFailedLoginAttempts(user.getId());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.notOk(null, "Invalid credentials", "/api/v1/auth/login"));
        }
        
        // Check if account is locked
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.notOk(null, "Account is temporarily locked. Please try again later.", "/api/v1/auth/login"));
        }
        
        // Check if account is active
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.notOk(null, "Account is not active. Please contact administrator.", "/api/v1/auth/login"));
        }
        
        // Reset failed login attempts on successful login
        userService.resetFailedLoginAttempts(user.getId());
        
        // Update last login timestamp
        userService.updateLastLogin(user.getId());
        
        // Generate token with user information and roles
        String token = JwtTokenProvider.generateTokenWithUserInfo(user);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token), "Login successful", "/api/v1/auth/login"));
        
        } catch (Exception e) {
            System.err.println("ERROR in login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiResponse.notOk(null, "Internal server error", "/api/v1/auth/login"));
        }
    }
}