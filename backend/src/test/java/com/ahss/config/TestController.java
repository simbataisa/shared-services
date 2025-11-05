package com.ahss.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TestController {
    @GetMapping("/protected-test")
    public org.springframework.http.ResponseEntity<String> protectedEndpoint() {
        return (SecurityContextHolder.getContext().getAuthentication() != null)
                ? org.springframework.http.ResponseEntity.ok("ok")
                : org.springframework.http.ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
    }
}