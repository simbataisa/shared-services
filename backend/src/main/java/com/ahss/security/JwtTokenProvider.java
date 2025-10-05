package com.ahss.security;

import com.ahss.dto.UserDto;
import com.ahss.dto.RoleDto;
import com.ahss.dto.PermissionDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtTokenProvider {
    private static final Key KEY = Keys.hmacShaKeyFor("dev-secret-key-please-change-in-prod-1234567890".getBytes());
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 1 day

    public static String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String generateTokenWithUserInfo(UserDto user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);
        
        // Extract roles and permissions
        List<String> roles = user.getRoles() != null ? 
            user.getRoles().stream()
                .map(RoleDto::getName)
                .collect(Collectors.toList()) : 
            List.of();
        
        List<String> permissions = user.getRoles() != null ? 
            user.getRoles().stream()
                .flatMap(role -> role.getPermissions() != null ? 
                    role.getPermissions().stream() : 
                    java.util.stream.Stream.<PermissionDto>empty())
                .map(PermissionDto::getName)
                .distinct()
                .collect(Collectors.toList()) : 
            List.of();
        
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("isAdmin", roles.contains("System Administrator") || roles.contains("Super Administrator"))
                .claim("isSuperAdmin", roles.contains("Super Administrator"))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}