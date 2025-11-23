package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

@Epic("Integration Tests")
@Feature("Role Management")
@Owner("backend")
@AutoConfigureWireMock(port = 0)
public class RoleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WireMockServer wireMockServer;

    private static final String STUBS_OUTPUT_DIR = "target/stubs/role";

    @Test
    void generate_contract_stubs_viaWireMock() {
        stubFor(get(urlPathEqualTo("/contracts/role"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));
        assertFalse(wireMockServer.getStubMappings().isEmpty());
    }

    @AfterEach
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
    void getAllRoles_returnsRolesWhenAuthenticated() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = Allure.step("Prepare URL for GET /api/v1/roles", () ->
                "http://localhost:" + port + "/api/v1/roles"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/roles", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        JsonNode root = Allure.step("Parse and verify response structure", () -> {
            JsonNode node = objectMapper.readTree(resp.getBody());
            assertTrue(node.path("success").asBoolean());
            JsonNode data = node.path("data");
            assertTrue(data.isArray());
            return node;
        });

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void getRoleById_returnsRoleWhenExists() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        // Assuming role ID 1 exists from seed data
        String url = "http://localhost:" + port + "/api/v1/roles/1";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/roles/1", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        JsonNode root = Allure.step("Parse and verify response structure", () -> {
            JsonNode node = objectMapper.readTree(resp.getBody());
            assertTrue(node.path("success").asBoolean());
            JsonNode data = node.path("data");
            assertTrue(data.has("id"));
            assertTrue(data.has("name"));
            return node;
        });

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void getRoleById_returns404WhenNotFound() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/roles/99999";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/roles/99999", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 404", () ->
                assertEquals(404, resp.getStatusCode().value())
        );

        JsonNode root = Allure.step("Parse and verify response structure", () -> {
            JsonNode node = objectMapper.readTree(resp.getBody());
            assertFalse(node.path("success").asBoolean());
            return node;
        });

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
