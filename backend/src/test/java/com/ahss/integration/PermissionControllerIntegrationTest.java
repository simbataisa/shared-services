package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Integration Tests")
@Feature("Permission Management")
@Owner("backend")
public class PermissionControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  void getAllPermissions_returnsPermissionsWhenAuthenticated() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/permissions";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "Send GET request to list permissions",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));
    JsonNode root = objectMapper.readTree(resp.getBody());
    Allure.step(
        "Verify response body contains permissions",
        () -> {
          assertTrue(root.path("success").asBoolean());
          JsonNode data = root.path("data");
          assertTrue(data.isArray());
        });
  }

  @Test
  void getPermissionById_returnsPermissionWhenExists() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);

    // First get all permissions to find a valid ID
    String listUrl = "http://localhost:" + port + "/api/v1/permissions";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> listResp =
        Allure.step(
            "Send GET request to list permissions",
            () ->
                restTemplate.exchange(
                    listUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class));
    Allure.step(
        "Verify response status is 200", () -> assertEquals(200, listResp.getStatusCode().value()));
    JsonNode listRoot = objectMapper.readTree(listResp.getBody());
    JsonNode permissions =
        Allure.step("Get first permission from list", () -> listRoot.path("data").get(0));
    Allure.step(
        "Verify permission has ID and name",
        () -> {
          assertTrue(permissions.has("id"));
          assertTrue(permissions.has("name"));
        });

    if (permissions.isArray() && !permissions.isEmpty()) {
      Long permissionId = Allure.step("Get permission ID", () -> permissions.path("id").asLong());
      String url = "http://localhost:" + port + "/api/v1/permissions/" + permissionId;

      ResponseEntity<String> resp =
          Allure.step(
              "Send GET request to get permission by ID",
              () ->
                  restTemplate.exchange(
                      url, HttpMethod.GET, new HttpEntity<>(headers), String.class));
      Allure.step(
          "Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));
      JsonNode root = objectMapper.readTree(resp.getBody());
      Allure.step(
          "Verify response body contains permission details",
          () -> {
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.has("id"));
            assertTrue(data.has("name"));
          });
    }
  }

  @Test
  void getPermissionById_returns404WhenNotFound() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/permissions/99999";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "Send GET request to get non-existent permission by ID",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class));
    Allure.step(
        "Verify response status is 404", () -> assertEquals(404, resp.getStatusCode().value()));
    JsonNode root =
        Allure.step(
            "Parse and verify response structure", () -> objectMapper.readTree(resp.getBody()));
    Allure.step(
        "Verify response body indicates not found",
        () -> assertFalse(root.path("success").asBoolean()));
    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }


  @Test
  void updatePermission_returns404WhenNotFound() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/permissions/99999";

    Map<String, Object> payload = Map.of("name", "Updated Permission", "description", "Updated desc");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> resp =
        Allure.step(
            "Send PUT request to update non-existent permission",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.PUT, new HttpEntity<>(payload, headers), String.class));

    Allure.step(
        "Verify response status is 404", () -> assertEquals(404, resp.getStatusCode().value()));
    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }

  @Test
  void deletePermission_returns404WhenNotFound() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/permissions/99999";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp =
        Allure.step(
            "Send DELETE request to delete non-existent permission",
            () ->
                restTemplate.exchange(
                    url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class));

    Allure.step(
        "Verify response status is 404", () -> assertEquals(404, resp.getStatusCode().value()));
    Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
  }
}
