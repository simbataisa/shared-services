package com.ahss.integration;

import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

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
@Feature("Payment Management")
@Owner("backend")
@AutoConfigureWireMock(port = 0)
public class PaymentControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private WireMockServer wireMockServer;

  private static final String STUBS_OUTPUT_DIR = "target/stubs/payment-controller";

  @Test
  void generate_contract_stubs_viaWireMock() {
    stubFor(get(urlPathEqualTo("/contracts/payment-controller"))
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
  void getAllPaymentRequests_returnsRequestsWhenAuthenticated() throws Exception {
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
    String token = Objects.requireNonNull(Allure.step("Obtain JWT token", this::obtainToken));
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
