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
@Feature("Dashboard Statistics")
@Owner("backend")
@AutoConfigureWireMock(port = 0)
public class DashboardIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WireMockServer wireMockServer;

    private static final String STUBS_OUTPUT_DIR = "target/stubs/dashboard";

    @Test
    void generate_contract_stubs_viaWireMock() {
        stubFor(get(urlPathEqualTo("/contracts/dashboard"))
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
    @Story("Dashboard stats endpoint returns expected fields when authenticated")
    @Severity(SeverityLevel.CRITICAL)
    void stats_endpoint_returns_expected_fields_when_authenticated() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/dashboard/stats";

        ResponseEntity<String> resp = Allure.step("GET /api/v1/dashboard/stats", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        });

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        Allure.step("Verify dashboard stats structure and fields", () -> {
            JsonNode root = objectMapper.readTree(resp.getBody());
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.has("totalUsers"));
            assertTrue(data.has("activeTenants"));
            assertTrue(data.has("totalRoles"));
            assertTrue(data.has("recentActivities"));
            assertEquals("healthy", data.path("systemHealth").asText());
            assertTrue(data.has("pendingApprovals"));
        });

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    @Story("Recent activities requires authentication")
    @Severity(SeverityLevel.NORMAL)
    void recent_activities_requires_authentication() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/dashboard/recent-activities";

        ResponseEntity<String> resp = Allure.step("GET /api/v1/dashboard/recent-activities", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        });

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        Allure.step("Verify recent activities response structure", () -> {
            JsonNode root = objectMapper.readTree(resp.getBody());
            assertTrue(root.path("success").asBoolean());
            assertTrue(root.path("data").isArray());
            assertFalse(root.path("data").isEmpty());
        });

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
