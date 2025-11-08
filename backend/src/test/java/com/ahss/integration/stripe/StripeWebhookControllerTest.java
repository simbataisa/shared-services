package com.ahss.integration.stripe;

import com.ahss.dto.response.ApiResponse;
import com.ahss.integration.BaseIntegrationTest;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.producer.PaymentCallbackProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Payment Channel Integration")
@Feature("Stripe Integration")
class StripeWebhookControllerTest extends BaseIntegrationTest {

  @MockBean private PaymentCallbackProducer producer;

  @Test
  @DisplayName("Handle Stripe Payment Intent Succeeded Sends Event")
  @Story("Handle Stripe Payment Intent Succeeded Sends Event")
  void handle_stripe_payment_intent_succeeded_sends_event() {
    UUID requestId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    UUID refundId = UUID.randomUUID();

    String payload =
        Allure.step(
            "Create Stripe Payment Intent Succeeded Event Payload",
            () ->
                "{"
                    + "\"id\":\"evt_123\","
                    + "\"type\":\"payment_intent.succeeded\","
                    + "\"data\": { \"object\": {"
                    + "\"id\": \"pi_abc\","
                    + "\"currency\": \"usd\","
                    + "\"amount_received\": 12345,"
                    + "\"metadata\": {"
                    + "\"correlationId\": \"corr-789\","
                    + "\"paymentToken\": \"tok_001\","
                    + "\"requestCode\": \"REQ-42\","
                    + "\"paymentRequestId\": \""
                    + requestId
                    + "\","
                    + "\"paymentTransactionId\": \""
                    + transactionId
                    + "\","
                    + "\"paymentRefundId\": \""
                    + refundId
                    + "\""
                    + "}"
                    + "} }"
                    + "}");

    String url = "http://localhost:" + port + "/api/integrations/webhooks/stripe";
    HttpHeaders headers = Allure.step("Create Stripe Webhook Headers", () -> new HttpHeaders());
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", "sig_stripe");
    Allure.addAttachment("Stripe Request Payload", "application/json", payload);
    ResponseEntity<ApiResponse<Void>> response =
        Allure.step(
            "Send Stripe Payment Intent Succeeded Webhook",
            () ->
                restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(payload, headers),
                    (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class));

    Allure.step(
        "Verify Stripe Payment Intent Succeeded Webhook Response",
        () -> {
          assertEquals(200, response.getStatusCode().value());
          assertTrue(response.getBody().isSuccess());
        });

    ArgumentCaptor<PaymentCallbackEvent> captor =
        Allure.step(
            "Capture Payment Callback Event",
            () -> ArgumentCaptor.forClass(PaymentCallbackEvent.class));
    Allure.step(
        "Verify Payment Callback Event Sent to Kafka",
        () -> verify(producer, times(1)).send(captor.capture()));
    PaymentCallbackEvent evt = Allure.step("Get Captured Payment Callback Event", captor::getValue);
    try {
      String eventJson = new ObjectMapper().writeValueAsString(evt);
      Allure.addAttachment("Stripe Event Payload", "application/json", eventJson);
    } catch (Exception ignored) {
    }

    Allure.step(
        "Assert Stripe Payment Callback",
        () -> {
          assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
          assertEquals("Stripe", evt.getGatewayName());
          assertEquals("corr-789", evt.getCorrelationId());
          assertEquals("tok_001", evt.getPaymentToken());
          assertEquals("REQ-42", evt.getRequestCode());
          assertEquals(requestId, evt.getPaymentRequestId());
          assertEquals(transactionId, evt.getPaymentTransactionId());
          assertEquals(refundId, evt.getPaymentRefundId());
          assertEquals("pi_abc", evt.getExternalTransactionId());
          assertEquals(new BigDecimal("123.45"), evt.getAmount());
          assertEquals("usd", evt.getCurrency());
          assertNotNull(evt.getReceivedAt());
          assertNotNull(evt.getMetadata());
          assertEquals("sig_stripe", evt.getMetadata().get("stripeSignature"));
          assertNotNull(evt.getGatewayResponse());
          assertTrue(((Map<?, ?>) evt.getGatewayResponse()).containsKey("type"));
        });
  }

  @Test
  @DisplayName("Handle Stripe Payment Intent Failed with last_payment_error")
  @Story("Handle Stripe Payment Intent Failed with last_payment_error")
  void handle_stripe_payment_intent_failed_with_last_error() {
    String payload =
        "{"
            + "\"id\":\"evt_456\","
            + "\"type\":\"payment_intent.payment_failed\","
            + "\"data\": { \"object\": {"
            + "\"id\": \"pi_def\","
            + "\"currency\": \"usd\","
            + "\"amount\": 5000,"
            + "\"metadata\": { \"correlationId\": \"corr-999\" },"
            + "\"last_payment_error\": { \"code\": \"card_declined\", \"message\": \"Declined\" }"
            + "} }"
            + "}";

    String url = "http://localhost:" + port + "/api/integrations/webhooks/stripe";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", "sig_failed");
    Allure.addAttachment("Stripe Request Payload (Failed)", "application/json", payload);
    ResponseEntity<ApiResponse<Void>> response =
        restTemplate.postForEntity(
            url,
            new HttpEntity<>(payload, headers),
            (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class);

    assertEquals(200, response.getStatusCode().value());

    ArgumentCaptor<PaymentCallbackEvent> captor =
        ArgumentCaptor.forClass(PaymentCallbackEvent.class);
    verify(producer, times(1)).send(captor.capture());
    PaymentCallbackEvent evt = captor.getValue();
    try {
      Allure.addAttachment(
          "Stripe Event Payload (Failed)",
          "application/json",
          new ObjectMapper().writeValueAsString(evt));
    } catch (Exception ignored) {
    }

    assertEquals(PaymentCallbackType.PAYMENT_FAILED, evt.getType());
    assertEquals("Stripe", evt.getGatewayName());
    assertEquals("corr-999", evt.getCorrelationId());
    assertEquals(new BigDecimal("50.00"), evt.getAmount());
    assertEquals("usd", evt.getCurrency());
    assertEquals("card_declined", evt.getErrorCode());
    assertEquals("Declined", evt.getErrorMessage());
    assertEquals("sig_failed", evt.getMetadata().get("stripeSignature"));
    assertNotNull(evt.getGatewayResponse());
  }

  @Test
  @DisplayName("Handle Stripe Charge Refunded sets externalRefundId and currency via amount branch")
  @Story("Handle Stripe Charge Refunded sets externalRefundId and currency via amount branch")
  void handle_stripe_charge_refunded_sets_refund_id() {
    String payload =
        "{"
            + "\"id\":\"evt_789\","
            + "\"type\":\"charge.refunded\","
            + "\"data\": { \"object\": {"
            + "\"id\": \"ch_123\","
            + "\"currency\": \"usd\","
            + "\"amount\": 1234,"
            + "\"refunds\": { \"data\": [ { \"id\": \"re_456\" } ] }"
            + "} }"
            + "}";

    String url = "http://localhost:" + port + "/api/integrations/webhooks/stripe";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", "sig_refund");
    Allure.addAttachment("Stripe Request Payload (Refund)", "application/json", payload);
    ResponseEntity<ApiResponse<Void>> response =
        restTemplate.postForEntity(
            url,
            new HttpEntity<>(payload, headers),
            (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class);

    assertEquals(200, response.getStatusCode().value());

    ArgumentCaptor<PaymentCallbackEvent> captor =
        ArgumentCaptor.forClass(PaymentCallbackEvent.class);
    verify(producer, times(1)).send(captor.capture());
    PaymentCallbackEvent evt = captor.getValue();
    try {
      Allure.addAttachment(
          "Stripe Event Payload (Refund)",
          "application/json",
          new ObjectMapper().writeValueAsString(evt));
    } catch (Exception ignored) {
    }

    assertEquals(PaymentCallbackType.REFUND_SUCCESS, evt.getType());
    assertEquals("Stripe", evt.getGatewayName());
    assertEquals("ch_123", evt.getExternalTransactionId());
    assertEquals("re_456", evt.getExternalRefundId());
    assertEquals(new BigDecimal("12.34"), evt.getAmount());
    assertEquals("usd", evt.getCurrency());
    assertEquals("sig_refund", evt.getMetadata().get("stripeSignature"));
    assertNotNull(evt.getGatewayResponse());
  }
}
