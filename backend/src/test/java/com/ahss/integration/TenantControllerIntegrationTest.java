package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Integration Tests")
@Feature("Tenant Management")
@Owner("backend")
public class TenantControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  void getAllTenants_returnsTenantsWhenAuthenticated() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url =
        Allure.step(
            "Prepare URL for GET /api/v1/tenants",
            () -> "http://localhost:" + port + "/api/v1/tenants");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "GET /api/v1/tenants",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 200 OK",
        () ->
            assertEquals(
                200,
                resp.getStatusCode().value(),
                "Expected 200 OK for successful tenant retrieval"));

    JsonNode root =
        Allure.step(
            "Parse and verify response structure", () -> objectMapper.readTree(resp.getBody()));

    Allure.step(
        "Verify response contains tenant data",
        () -> {
          assertTrue(root.path("success").asBoolean(), "Response 'success' should be true");
          JsonNode data = root.path("data");
          assertTrue(data.isArray(), "Response 'data' should be an array");
        });

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }

  @Test
  void getTenantById_returnsTenantWhenExists() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    // Assuming tenant ID 1 exists from seed data
    String url = "http://localhost:" + port + "/api/v1/tenants/1";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "GET /api/v1/tenants/1",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 200 OK",
        () ->
            assertEquals(
                200,
                resp.getStatusCode().value(),
                "Expected 200 OK for successful tenant retrieval"));

    JsonNode root =
        Allure.step(
            "Parse and verify response structure", () -> objectMapper.readTree(resp.getBody()));

    Allure.step(
        "Verify response contains tenant data",
        () -> {
          assertTrue(root.path("success").asBoolean(), "Response 'success' should be true");
          JsonNode data = root.path("data");
          assertTrue(data.has("id"), "Response 'data' should contain 'id' field");
          assertTrue(data.has("name"), "Response 'data' should contain 'name' field");
          assertTrue(data.has("code"), "Response 'data' should contain 'code' field");
        });

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }

  @Test
  void getTenantById_returns404WhenNotFound() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/tenants/99999";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "GET /api/v1/tenants/99999",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 404 Not Found",
        () ->
            assertEquals(
                404,
                resp.getStatusCode().value(),
                "Expected 404 Not Found for non-existent tenant"));

    JsonNode root =
        Allure.step(
            "Parse and verify response structure", () -> objectMapper.readTree(resp.getBody()));

    Allure.step(
        "Verify response indicates not found",
        () -> {
          // For 404, the response structure might vary, so just check it's parseable
          assertNotNull(root, "Response should be parseable JSON");
        });

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }

  @Test
  void searchTenants_returnsMatchingTenants() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url =
        Allure.step(
            "Prepare URL for GET /api/v1/tenants/search?query=test",
            () -> "http://localhost:" + port + "/api/v1/tenants/search?query=test");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "GET /api/v1/tenants/search?query=test",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 200 OK",
        () ->
            assertEquals(
                200, resp.getStatusCode().value(), "Expected 200 OK for successful tenant search"));

    JsonNode root =
        Allure.step(
            "Parse and verify response structure", () -> objectMapper.readTree(resp.getBody()));

    Allure.step(
        "Verify response contains matching tenant data",
        () -> {
          assertTrue(root.path("success").asBoolean(), "Response 'success' should be true");
          JsonNode data = root.path("data");
          assertTrue(data.isArray(), "Response 'data' should be an array");
        });

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }

  @Test
  void getTenantByCode_returns404WhenNotFound() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/tenants/code/NONEXISTENT";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "GET /api/v1/tenants/code/NONEXISTENT",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 404 Not Found",
        () ->
            assertEquals(
                404,
                resp.getStatusCode().value(),
                "Expected 404 Not Found for non-existent tenant code"));

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }


  @Test
  void deleteTenant_returns404WhenNotFound() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/tenants/99999";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "DELETE /api/v1/tenants/99999",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 404 Not Found",
        () ->
            assertEquals(
                404,
                resp.getStatusCode().value(),
                "Expected 404 Not Found for non-existent tenant"));

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }

  @Test
  void checkTenantCodeExists_returnsFalseForNonexistentCode() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url =
        "http://localhost:" + port + "/api/v1/tenants/exists/code/NONEXISTENT99999";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "GET /api/v1/tenants/exists/code/NONEXISTENT99999",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 200 OK",
        () ->
            assertEquals(
                200, resp.getStatusCode().value(), "Expected 200 OK for tenant code check"));

    Allure.step(
        "Verify response indicates code does not exist",
        () -> {
          JsonNode root = objectMapper.readTree(resp.getBody());
          assertTrue(root.path("success").asBoolean(), "Response 'success' should be true");
          assertFalse(root.path("data").asBoolean(), "Tenant code should not exist");
        });

    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }
}
