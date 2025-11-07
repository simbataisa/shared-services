package com.ahss.integration.webhook;

import com.ahss.dto.response.ApiResponse;
import com.ahss.integration.BaseIntegrationTest;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.producer.PaymentCallbackProducer;
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

@Epic("Saga")
@Feature("Webhook Integration")
class StripeWebhookControllerTest extends BaseIntegrationTest {

    @MockBean
    private PaymentCallbackProducer producer;

    @Test
    @DisplayName("Handle Stripe Payment Intent Succeeded Sends Event")
    @Story("Handle Stripe Payment Intent Succeeded Sends Event")
    void handle_stripe_payment_intent_succeeded_sends_event() {
        UUID requestId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();

        String payload = Allure.step(
                "Create Stripe Payment Intent Succeeded Event Payload",
                () -> "{"
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
        ResponseEntity<ApiResponse<Void>> response = Allure.step(
                "Send Stripe Payment Intent Succeeded Webhook",
                () -> restTemplate.postForEntity(
                        url,
                        new HttpEntity<>(payload, headers),
                        (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class));

        Allure.step(
                "Verify Stripe Payment Intent Succeeded Webhook Response",
                () -> {
                    assertEquals(200, response.getStatusCode().value());
                    assertTrue(response.getBody().isSuccess());
                });

        ArgumentCaptor<PaymentCallbackEvent> captor = Allure.step(
                "Capture Payment Callback Event",
                () -> ArgumentCaptor.forClass(PaymentCallbackEvent.class));
        Allure.step(
                "Verify Payment Callback Event Sent to Kafka",
                () -> verify(producer, times(1)).send(captor.capture()));
        PaymentCallbackEvent evt = Allure.step("Get Captured Payment Callback Event", captor::getValue);

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
}
