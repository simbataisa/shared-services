package com.ahss.integration.paypal;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Payment Channel Integration")
@Feature("PayPal Integration")
class PayPalWebhookControllerTest extends BaseIntegrationTest {

        @MockBean
        private PaymentCallbackProducer producer;

        @Test
        @DisplayName("PayPal Payment Capture Completed")
        @Story("PayPal Payment Capture Completed")
        void handle_paypal_payment_capture_completed_sends_event() {
                String payload = Allure.step(
                                "Create PayPal Payment Capture Completed payload",
                                () -> "{"
                                                + "\"id\":\"WH-987\","
                                                + "\"event_type\":\"PAYMENT.CAPTURE.COMPLETED\","
                                                + "\"resource\": {"
                                                + "\"id\": \"sale_123\","
                                                + "\"custom_id\": \"corr-123\","
                                                + "\"invoice_id\": \"INV-1\","
                                                + "\"amount\": {\"value\": \"20.50\", \"currency_code\": \"USD\"},"
                                                + "\"create_time\": \"2024-10-20T12:00:00Z\""
                                                + "}"
                                                + "}");

                String url = Allure.step(
                                "Create PayPal Webhook URL",
                                () -> "http://localhost:" + port + "/api/integrations/webhooks/paypal");
                HttpHeaders headers = Allure.step("Create PayPal Webhook Headers", () -> new HttpHeaders());
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("PayPal-Transmission-Sig", "sig-abc");
                headers.add("PayPal-Transmission-Id", "tx-001");
                Allure.addAttachment("PayPal Request Payload", "application/json", payload);
                ResponseEntity<ApiResponse<Void>> response = Allure.step(
                                "Send PayPal Payment Capture Completed Webhook",
                                () -> restTemplate.postForEntity(
                                                url,
                                                new HttpEntity<>(payload, headers),
                                                (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class));

                Allure.step(
                                "Verify PayPal Payment Capture Completed Webhook Response",
                                () -> {
                                        assertEquals(200, response.getStatusCode().value());
                                        assertNotNull(response.getBody());
                                        assertTrue(response.getBody().isSuccess());
                                });

                ArgumentCaptor<PaymentCallbackEvent> captor = Allure.step(
                                "Capture PayPal Payment Capture Completed Webhook Event",
                                () -> ArgumentCaptor.forClass(PaymentCallbackEvent.class));
                verify(producer, times(1)).send(captor.capture());
                PaymentCallbackEvent evt = captor.getValue();
                try {
                        Allure.addAttachment("PayPal Event Payload", "application/json",
                                        new ObjectMapper().writeValueAsString(evt));
                } catch (Exception ignored) {
                }

                Allure.step(
                                "Verify PayPal Payment Capture Completed Webhook Event Type",
                                () -> {
                                        assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
                                        assertEquals("PayPal", evt.getGatewayName());
                                        assertEquals("corr-123", evt.getCorrelationId());
                                        assertEquals("INV-1", evt.getPaymentToken());
                                });
                Allure.step(
                                "Verify PayPal Payment Capture Completed Webhook Event Details",
                                () -> {
                                        assertEquals("INV-1", evt.getRequestCode());
                                        assertEquals("sale_123", evt.getExternalTransactionId());
                                        assertEquals(new BigDecimal("20.50"), evt.getAmount());
                                        assertEquals("USD", evt.getCurrency());
                                        assertNotNull(evt.getReceivedAt());
                                        assertNotNull(evt.getMetadata());
                                });
                Allure.step(
                                "Verify PayPal Payment Capture Completed Webhook Event Metadata",
                                () -> {
                                        assertEquals("sig-abc", evt.getMetadata().get("transmissionSig"));
                                        assertEquals("tx-001", evt.getMetadata().get("transmissionId"));
                                });
                Allure.step(
                                "Verify PayPal Payment Capture Completed Webhook Event Gateway Response",
                                () -> {
                                        assertNotNull(evt.getGatewayResponse());
                                        assertTrue(evt.getGatewayResponse().containsKey("event_type"));
                                });
        }

        @Test
        @DisplayName("PayPal Payment Capture Denied populates error message")
        @Story("PayPal Payment Capture Denied populates error message")
        void handle_paypal_payment_capture_denied_sets_error() {
                String payload = "{"
                                + "\"id\":\"WH-111\","
                                + "\"event_type\":\"PAYMENT.CAPTURE.DENIED\","
                                + "\"resource\": {"
                                + "\"id\": \"sale_denied\","
                                + "\"invoice_id\": \"INV-2\","
                                + "\"reason\": \"Insufficient funds\""
                                + "}"
                                + "}";

                String url = "http://localhost:" + port + "/api/integrations/webhooks/paypal";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("PayPal-Transmission-Sig", "sig-denied");
                headers.add("PayPal-Transmission-Id", "tx-002");
                Allure.addAttachment("PayPal Request Payload (Denied)", "application/json", payload);
                ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
                                url,
                                new HttpEntity<>(payload, headers),
                                (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class);

                assertEquals(200, response.getStatusCode().value());

                ArgumentCaptor<PaymentCallbackEvent> captor = ArgumentCaptor.forClass(PaymentCallbackEvent.class);
                verify(producer, times(1)).send(captor.capture());
                PaymentCallbackEvent evt = captor.getValue();
                try {
                        Allure.addAttachment("PayPal Event Payload (Denied)", "application/json",
                                        new ObjectMapper().writeValueAsString(evt));
                } catch (Exception ignored) {
                }

                assertEquals(PaymentCallbackType.PAYMENT_FAILED, evt.getType());
                assertEquals("PayPal", evt.getGatewayName());
                assertEquals("INV-2", evt.getPaymentToken());
                assertEquals("Insufficient funds", evt.getErrorMessage());
                assertEquals("sig-denied", evt.getMetadata().get("transmissionSig"));
                assertEquals("tx-002", evt.getMetadata().get("transmissionId"));
                assertNotNull(evt.getGatewayResponse());
        }

        @Test
        @DisplayName("PayPal Payment Capture Refunded without amount populates null amount")
        @Story("PayPal Payment Capture Refunded without amount populates null amount")
        void handle_paypal_payment_capture_refunded_no_amount_branch() {
                String payload = "{"
                                + "\"id\":\"WH-222\","
                                + "\"event_type\":\"PAYMENT.CAPTURE.REFUNDED\","
                                + "\"resource\": {"
                                + "\"id\": \"sale_ref\","
                                + "\"invoice_id\": \"INV-3\""
                                + "}"
                                + "}";

                String url = "http://localhost:" + port + "/api/integrations/webhooks/paypal";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("PayPal-Transmission-Sig", "sig-refund");
                headers.add("PayPal-Transmission-Id", "tx-003");
                Allure.addAttachment("PayPal Request Payload (Refunded)", "application/json", payload);
                ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
                                url,
                                new HttpEntity<>(payload, headers),
                                (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class);

                assertEquals(200, response.getStatusCode().value());

                ArgumentCaptor<PaymentCallbackEvent> captor = ArgumentCaptor.forClass(PaymentCallbackEvent.class);
                verify(producer, times(1)).send(captor.capture());
                PaymentCallbackEvent evt = captor.getValue();
                try {
                        Allure.addAttachment("PayPal Event Payload (Refunded)", "application/json",
                                        new ObjectMapper().writeValueAsString(evt));
                } catch (Exception ignored) {
                }

                assertEquals(PaymentCallbackType.REFUND_SUCCESS, evt.getType());
                assertEquals("PayPal", evt.getGatewayName());
                assertEquals("INV-3", evt.getPaymentToken());
                assertEquals("sale_ref", evt.getExternalTransactionId());
                assertNull(evt.getAmount());
                assertNull(evt.getCurrency());
                assertEquals("sig-refund", evt.getMetadata().get("transmissionSig"));
                assertEquals("tx-003", evt.getMetadata().get("transmissionId"));
                assertNotNull(evt.getGatewayResponse());
        }
}
