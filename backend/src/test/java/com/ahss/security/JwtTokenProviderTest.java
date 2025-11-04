package com.ahss.security;

import com.ahss.dto.PermissionDto;
import com.ahss.dto.RoleDto;
import com.ahss.dto.UserDto;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Epic("Security")
@Feature("JWT")
@Owner("backend")
class JwtTokenProviderTest {

    @BeforeEach
    void applyLabels() {
        // Using annotations; no runtime labels needed
    }

    private UserDto sampleUser() {
        UserDto user = new UserDto();
        user.setId(42L);
        user.setEmail("admin@ahss.com");
        user.setUsername("admin");
        user.setFirstName("Admin");
        user.setLastName("User");

        RoleDto admin = new RoleDto();
        admin.setName("System Administrator");
        PermissionDto p1 = new PermissionDto();
        p1.setName("payments:read");
        PermissionDto p2 = new PermissionDto();
        p2.setName("users:write");
        admin.setPermissions(List.of(p1, p2));

        user.setRoles(List.of(admin));
        return user;
    }

    @Test
    @Story("Basic JWT generation and parsing")
    @Severity(SeverityLevel.CRITICAL)
    void generateToken_sets_subject_and_parses_back() {
        String token = Allure.step("Generate JWT for subject", () -> JwtTokenProvider.generateToken("subject-123"));
        Allure.addAttachment("JWT Token", MediaType.TEXT_PLAIN_VALUE, token);
        Claims claims = Allure.step("Parse JWT", () -> JwtTokenProvider.parse(token));
        Allure.addAttachment("Subject", MediaType.TEXT_PLAIN_VALUE, claims.getSubject());
        assertEquals("subject-123", claims.getSubject());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @Story("JWT includes user info, roles, and permissions")
    @Severity(SeverityLevel.CRITICAL)
    void generateTokenWithUserInfo_includes_user_claims_roles_permissions() {
        UserDto user = sampleUser();
        String token = Allure.step("Generate JWT with user info", () -> JwtTokenProvider.generateTokenWithUserInfo(user));
        assertNotNull(token);
        Allure.addAttachment("JWT Token", MediaType.TEXT_PLAIN_VALUE, token);
        Claims claims = Allure.step("Parse JWT", () -> JwtTokenProvider.parse(token));

        assertEquals("admin@ahss.com", claims.getSubject());
        assertEquals(42L, ((Number) claims.get("userId")).longValue());
        assertEquals("admin", claims.get("username"));
        assertEquals("Admin", claims.get("firstName"));
        assertEquals("User", claims.get("lastName"));

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("System Administrator"));

        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) claims.get("permissions");
        assertTrue(permissions.contains("payments:read"));
        assertTrue(permissions.contains("users:write"));

        assertEquals(Boolean.TRUE, claims.get("isAdmin", Boolean.class));
        assertEquals(Boolean.FALSE, claims.get("isSuperAdmin", Boolean.class));
        Allure.addAttachment("JWT Claims", MediaType.APPLICATION_JSON_VALUE, new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(claims).toString());
    }
}