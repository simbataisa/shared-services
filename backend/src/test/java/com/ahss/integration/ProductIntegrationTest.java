package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Integration Tests")
@Feature("Product Management")
@Owner("backend")
@AutoConfigureWireMock(port = 0)
public class ProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WireMockServer wireMockServer;

    private static final String STUBS_OUTPUT_DIR = "target/stubs/product";

    @Test
    void generate_contract_stubs_viaWireMock() {
        stubFor(get(urlPathEqualTo("/contracts/product"))
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
    @Story("List products returns items when authenticated")
    @Severity(SeverityLevel.CRITICAL)
    void list_products_returns_items_when_authenticated() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/products";

        ResponseEntity<String> resp = Allure.step("GET /api/v1/products", () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        });

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        Allure.step("Verify product list structure and content", () -> {
            JsonNode root = objectMapper.readTree(resp.getBody());
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.isArray());
            assertFalse(data.isEmpty(), "Expected at least one seeded product");
            // Validate minimal fields exist
            JsonNode first = data.get(0);
            assertTrue(first.has("id"));
            assertTrue(first.has("name"));
        });

        assertNotNull(resp.getBody());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
