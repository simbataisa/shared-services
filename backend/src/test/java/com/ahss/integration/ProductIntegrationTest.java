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
@Feature("Product Management")
@Owner("backend")
public class ProductIntegrationTest extends BaseIntegrationTest {

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
            assertTrue(data.size() >= 1, "Expected at least one seeded product");
            // Validate minimal fields exist
            JsonNode first = data.get(0);
            assertTrue(first.has("id"));
            assertTrue(first.has("name"));
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
