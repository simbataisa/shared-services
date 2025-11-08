package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Integration Tests")
@Feature("Authentication")
@Owner("backend")
public class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    @Story("Login with valid admin credentials returns JWT token")
    @Severity(SeverityLevel.BLOCKER)
    void login_with_valid_admin_credentials_returns_token() throws Exception {
        String url = "http://localhost:" + port + "/api/v1/auth/login";

        Map<String, String> payload = Allure.step("Prepare login credentials", () ->
                Map.of("username", "admin", "password", "admin123")
        );

        ResponseEntity<String> response = Allure.step("POST /api/v1/auth/login", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            return restTemplate.postForEntity(url, request, String.class);
        });

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, response.getStatusCode().value(), "Expected 200 OK for successful login")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(response.getBody())
        );

        Allure.step("Verify JWT token is present in response", () -> {
            assertTrue(root.path("success").asBoolean(), "Response 'success' should be true");
            String token = root.path("data").path("token").asText();
            assertNotNull(token);
            assertFalse(token.isEmpty(), "JWT token should be present");
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, response.getBody());
    }
}