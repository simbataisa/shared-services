package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Integration Tests")
@Feature("Module Management")
@Owner("backend")
public class ModuleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Story("Get all modules returns list when authenticated")
    @Severity(SeverityLevel.CRITICAL)
    void getAllModules_returnsModulesWhenAuthenticated() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/modules";

        ResponseEntity<String> resp = Allure.step("GET /api/v1/modules", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        });

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

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    @Story("Get module by ID returns module when it exists")
    @Severity(SeverityLevel.NORMAL)
    void getModuleById_returnsModuleWhenExists() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        // Assuming module ID 1 exists from seed data
        String url = "http://localhost:" + port + "/api/v1/modules/1";

        ResponseEntity<String> resp = Allure.step("GET /api/v1/modules/1", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        });

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        Allure.step("Verify module data structure", () -> {
            JsonNode root = objectMapper.readTree(resp.getBody());
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.has("id"));
            assertTrue(data.has("name"));
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    @Story("Get module by ID returns 404 when module not found")
    @Severity(SeverityLevel.NORMAL)
    void getModuleById_returns404WhenNotFound() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/modules/99999";

        ResponseEntity<String> resp = Allure.step("GET /api/v1/modules/99999", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        });

        Allure.step("Verify response status is 404", () ->
                assertEquals(404, resp.getStatusCode().value())
        );

        Allure.step("Verify error response structure", () -> {
            JsonNode root = objectMapper.readTree(resp.getBody());
            assertFalse(root.path("success").asBoolean());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
