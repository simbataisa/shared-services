package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Integration Tests")
@Feature("Authentication")
@Owner("backend")
@AutoConfigureWireMock(port = 0)
public class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WireMockServer wireMockServer;

    private static final String STUBS_OUTPUT_DIR = "target/stubs/auth";

    @Test
    void generate_contract_stubs_viaWireMock() {
        stubFor(get(urlPathEqualTo("/contracts/auth"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));
        assertFalse(wireMockServer.getStubMappings().isEmpty());
    }

    @org.junit.jupiter.api.AfterEach
    void saveContractStubs() throws IOException {
        Path stubsPath = Paths.get(STUBS_OUTPUT_DIR);
        Files.createDirectories(stubsPath);
        int idx = 0;
        for (StubMapping stub : wireMockServer.getStubMappings()) {
            String filename = String.format("stub_%d_%s.json", idx++, System.currentTimeMillis());
            Path stubFile = stubsPath.resolve(filename);
            String stubJson = StubMapping.buildJsonStringFor(stub);
            Files.writeString(stubFile, stubJson);
        }
        wireMockServer.resetAll();
    }

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