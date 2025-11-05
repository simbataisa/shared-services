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
@Feature("User Management")
@Owner("backend")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllUsers_returnsUsersWhenAuthenticated() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, resp.getStatusCode().value(), "Expected 200 OK for successful user retrieval")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(resp.getBody())
        );

        Allure.step("Verify response body contains user data", () -> {
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.isArray());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void getUserById_returnsUserWhenExists() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        // Assuming user ID 1 exists from seed data (admin user)
        String url = "http://localhost:" + port + "/api/v1/users/1";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/1", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, resp.getStatusCode().value(), "Expected 200 OK for successful user retrieval")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(resp.getBody())
        );

        Allure.step("Verify response body contains user data", () -> {
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.has("id"));
            assertTrue(data.has("username"));
            assertTrue(data.has("email"));
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void getUserById_returns404WhenNotFound() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = Allure.step("Prepare URL for user ID 99999", () ->
                "http://localhost:" + port + "/api/v1/users/99999"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/99999", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 404 Not Found", () ->
                assertEquals(404, resp.getStatusCode().value(), "Expected 404 Not Found for non-existent user")
        );

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void getUserByUsername_returnsUserWhenExists() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = Allure.step("Prepare URL for username admin", () ->
                "http://localhost:" + port + "/api/v1/users/username/admin"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/username/admin", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, resp.getStatusCode().value(), "Expected 200 OK for successful user retrieval")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(resp.getBody())
        );

        Allure.step("Verify response body contains user data", () -> {
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertEquals("admin", data.path("username").asText());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void searchUsers_returnsMatchingUsers() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = Allure.step("Prepare URL for search query admin", () ->
                "http://localhost:" + port + "/api/v1/users/search?query=admin"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/search?query=admin", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, resp.getStatusCode().value(), "Expected 200 OK for successful user retrieval")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(resp.getBody())
        );

        Allure.step("Verify response body contains user data", () -> {
            assertTrue(root.path("success").asBoolean());
            JsonNode data = root.path("data");
            assertTrue(data.isArray());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void checkUsernameExists_returnsTrueForExistingUsername() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = Allure.step("Prepare URL for username admin", () ->
                "http://localhost:" + port + "/api/v1/users/exists/username/admin"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/exists/username/admin", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, resp.getStatusCode().value(), "Expected 200 OK for successful username check")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(resp.getBody())
        );

        Allure.step("Verify response body contains username existence", () -> {
            assertTrue(root.path("success").asBoolean());
            assertTrue(root.path("data").asBoolean());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }

    @Test
    void checkUsernameExists_returnsFalseForNonExistingUsername() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = Allure.step("Prepare URL for non-existent username nonexistentuser99999", () ->
                "http://localhost:" + port + "/api/v1/users/exists/username/nonexistentuser99999"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/exists/username/nonexistentuser99999", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200 OK", () ->
                assertEquals(200, resp.getStatusCode().value(), "Expected 200 OK for successful username check")
        );

        JsonNode root = Allure.step("Parse response body", () ->
                objectMapper.readTree(resp.getBody())
        );

        Allure.step("Verify response body contains username non-existence", () -> {
            assertTrue(root.path("success").asBoolean());
            assertFalse(root.path("data").asBoolean());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }


    @Test
    void deleteUser_returns404WhenUserNotFound() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/users/99999";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("DELETE /api/v1/users/99999", () ->
                restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 404", () ->
                assertEquals(404, resp.getStatusCode().value())
        );

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }


    @Test
    void getUserByStatus_returnsUsersForActiveStatus() throws Exception {
        String token = Allure.step("Obtain JWT token", this::obtainToken);
        String url = "http://localhost:" + port + "/api/v1/users/status/ACTIVE";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> resp = Allure.step("GET /api/v1/users/status/ACTIVE", () ->
                restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class)
        );

        Allure.step("Verify response status is 200", () ->
                assertEquals(200, resp.getStatusCode().value())
        );

        Allure.step("Verify response contains users", () -> {
            JsonNode root = objectMapper.readTree(resp.getBody());
            assertTrue(root.path("success").asBoolean());
            assertTrue(root.path("data").isArray());
        });

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, resp.getBody());
    }
}
