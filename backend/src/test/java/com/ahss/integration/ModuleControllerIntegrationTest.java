package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
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
@Feature("Module Management")
@Owner("backend")
@AutoConfigureWireMock(port = 0)
public class ModuleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    private static final String STUBS_OUTPUT_DIR = "target/stubs/module";

    @Test
    void generate_contract_stubs_viaWireMock() {
        stubFor(get(urlPathEqualTo("/contracts/module"))
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

        assertNotNull(resp.getBody());
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

        assertNotNull(resp.getBody());
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

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
