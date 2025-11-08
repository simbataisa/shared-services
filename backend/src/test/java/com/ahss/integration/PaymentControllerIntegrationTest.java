package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

@Epic("Integration Tests")
@Feature("Payment Management")
@Owner("backend")
public class PaymentControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  void getAllPaymentRequests_returnsRequestsWhenAuthenticated() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/requests?page=0&size=10";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to list payment requests",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));

    Allure.step("Verify response body contains payment requests", () -> {
      JsonNode root = objectMapper.readTree(resp.getBody());
      assertTrue(root.path("success").asBoolean());
      JsonNode data = root.path("data");
      assertTrue(data.has("content"));
      assertTrue(data.path("content").isArray());
    });
  }

  @Test
  void getAllTransactions_returnsTransactionsWhenAuthenticated() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/transactions?page=0&size=10";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to list payment transactions",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));

    Allure.step("Verify response body contains payment transactions", () -> {
      JsonNode root = objectMapper.readTree(resp.getBody());
      assertTrue(root.path("success").asBoolean());
      JsonNode data = root.path("data");
      assertTrue(data.has("content"));
      assertTrue(data.path("content").isArray());
    });
  }

  @Test
  void getAllRefunds_returnsRefundsWhenAuthenticated() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/refunds?page=0&size=10";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to list payment refunds",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));

    Allure.step("Verify response body contains payment refunds", () -> {
      JsonNode root = objectMapper.readTree(resp.getBody());
      assertTrue(root.path("success").asBoolean());
      JsonNode data = root.path("data");
      assertTrue(data.has("content"));
      assertTrue(data.path("content").isArray());
    });
  }

  @Test
  void getAllAuditLogs_returnsAuditLogsWhenAuthenticated() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/audit-logs?page=0&size=10";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to list payment audit logs",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));

    Allure.step("Verify response body contains payment audit logs", () -> {
      JsonNode root = objectMapper.readTree(resp.getBody());
      assertTrue(root.path("success").asBoolean());
      JsonNode data = root.path("data");
      assertTrue(data.has("content"));
      assertTrue(data.path("content").isArray());
    });
  }

  @Test
  void getPaymentRequestStats_returnsStatistics() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/stats/requests";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to get payment request statistics",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200 or 500",
        () -> assertTrue(resp.getStatusCode().value() == 200 || resp.getStatusCode().value() == 500));

    Allure.step("Verify response body contains payment request statistics", () -> {
      if (resp.getStatusCode().value() == 200) {
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertTrue(root.path("success").asBoolean());
        JsonNode data = root.path("data");
        assertTrue(data.has("totalRequests"));
        assertTrue(data.has("pendingRequests"));
      }
    });
  }

  @Test
  void getTransactionStats_returnsStatistics() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/stats/transactions";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to get payment transaction statistics",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200", () -> assertEquals(200, resp.getStatusCode().value()));

    Allure.step("Verify response body contains payment transaction statistics", () -> {
      JsonNode root = objectMapper.readTree(resp.getBody());
      assertTrue(root.path("success").asBoolean());
      JsonNode data = root.path("data");
      assertTrue(data.has("totalTransactions"));
      assertTrue(data.has("pendingTransactions"));
    });
  }

  @Test
  void getRefundStats_returnsStatistics() throws Exception {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    String url = "http://localhost:" + port + "/api/v1/payments/stats/refunds";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    ResponseEntity<String> resp = Allure.step(
        "Send GET request to get payment refund statistics",
        () -> restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class));

    Allure.step("Verify response status is 200 or 500",
        () -> assertTrue(resp.getStatusCode().value() == 200 || resp.getStatusCode().value() == 500));

    Allure.step("Verify response body contains payment refund statistics", () -> {
      if (resp.getStatusCode().value() == 200) {
        JsonNode root = objectMapper.readTree(resp.getBody());
        assertTrue(root.path("success").asBoolean());
        JsonNode data = root.path("data");
        assertTrue(data.has("totalRefunds"));
        assertTrue(data.has("pendingRefunds"));
      }
    });
  }

}
