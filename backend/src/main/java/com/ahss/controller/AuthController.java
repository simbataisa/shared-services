package com.ahss.controller;

import com.ahss.dto.response.ApiResponse;
import com.ahss.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        // NOTE: For dev, accept any username/password and issue JWT
        String token = JwtTokenProvider.generateToken(username.isEmpty() ? "guest" : username);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token), "Login successful", "/api/v1/auth/login"));
    }
}