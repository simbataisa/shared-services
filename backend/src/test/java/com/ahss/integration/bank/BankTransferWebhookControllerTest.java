package com.ahss.integration.bank;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Payment Channel Integration")
@Feature("Bank Transfer Integration")
    class BankTransferWebhookControllerTest extends BaseIntegrationTest {

    @MockBean
    private PaymentCallbackProducer producer;

    @Test
    @DisplayName("Handle Bank Transfer Completed sends event with metadata")
    @Story("Handle Bank Transfer Completed sends event with metadata")
    void handle_bank_transfer_completed_sends_event() {
        String payload = Allure.step(
                "Create Bank Transfer Completed payload",
                () -> "{" +
                        "\"id\":\"BT-001\"," +
                        "\"status\":\"TRANSFER.COMPLETED\"," +
                        "\"transaction_id\":\"ext-001\"," +
                        "\"amount\":{\"value\":\"100.00\",\"currency\":\"USD\"}," +
                        "\"reference\":\"INV-100\"" +
                        "}");

        String url = "http://localhost:" + port + "/api/integrations/webhooks/bank-transfer";
        HttpHeaders headers = Allure.step("Create Bank Transfer Headers", () -> new HttpHeaders());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Bank-Signature", "sig-bank");
        headers.add("X-Bank-Request-Id", "req-123");
        Allure.addAttachment("Bank Transfer Request Payload", "application/json", payload);
        ResponseEntity<ApiResponse<Void>> response = Allure.step(
                "Send Bank Transfer Completed Webhook",
                () -> restTemplate.postForEntity(
                        url,
                        new HttpEntity<>(payload, headers),
                        (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class));

        Allure.step("Verify Webhook Response is 200", () -> assertEquals(200, response.getStatusCode().value()));

        ArgumentCaptor<PaymentCallbackEvent> captor = Allure.step(
                "Capture Payment Callback Event",
                () -> ArgumentCaptor.forClass(PaymentCallbackEvent.class));
        verify(producer, times(1)).send(captor.capture());
        PaymentCallbackEvent evt = captor.getValue();
        try {
            Allure.addAttachment("Bank Transfer Event Payload", "application/json",
                    new ObjectMapper().writeValueAsString(evt));
        } catch (Exception ignored) {}

        Allure.step("Assert Bank Transfer Completed Event", () -> {
            assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
            assertEquals("BankTransfer", evt.getGatewayName());
            assertEquals("BT-001", evt.getCorrelationId());
            assertEquals("ext-001", evt.getExternalTransactionId());
            assertEquals(new BigDecimal("100.00"), evt.getAmount());
            assertEquals("USD", evt.getCurrency());
            assertNotNull(evt.getReceivedAt());
            assertNotNull(evt.getMetadata());
        });

        Allure.step("Assert Metadata contains headers and reference", () -> {
            Map<?, ?> meta = evt.getMetadata();
            assertTrue(meta.containsKey("headers"));
            Map<?, ?> headerMap = (Map<?, ?>) meta.get("headers");
            assertEquals("sig-bank", headerMap.get("X-Bank-Signature"));
            assertEquals("req-123", headerMap.get("X-Bank-Request-Id"));
            assertEquals("INV-100", meta.get("reference"));
            assertEquals("TRANSFER.COMPLETED", meta.get("rawEventType"));
        });

        Allure.step("Assert Gateway Response attached", () -> {
            assertNotNull(evt.getGatewayResponse());
            assertTrue(((Map<?, ?>) evt.getGatewayResponse()).containsKey("status"));
        });
    }

    @Test
    @DisplayName("Handle Bank Transfer Failed populates error fields")
    @Story("Handle Bank Transfer Failed populates error fields")
    void handle_bank_transfer_failed_sets_error() {
        String payload = "{" +
                "\"id\":\"BT-002\"," +
                "\"status\":\"TRANSFER.FAILED\"," +
                "\"error\":{\"code\":\"ERR42\",\"message\":\"Bank declined\"}" +
                "}";

        String url = "http://localhost:" + port + "/api/integrations/webhooks/bank-transfer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Bank-Signature", "sig-failed");
        headers.add("X-Bank-Request-Id", "req-456");
        Allure.addAttachment("Bank Transfer Request Payload (Failed)", "application/json", payload);
        ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, headers),
                (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<PaymentCallbackEvent> captor = ArgumentCaptor.forClass(PaymentCallbackEvent.class);
        verify(producer, times(1)).send(captor.capture());
        PaymentCallbackEvent evt = captor.getValue();
        try {
            Allure.addAttachment("Bank Transfer Event Payload (Failed)", "application/json",
                    new ObjectMapper().writeValueAsString(evt));
        } catch (Exception ignored) {}

        assertEquals(PaymentCallbackType.PAYMENT_FAILED, evt.getType());
        assertEquals("BankTransfer", evt.getGatewayName());
        assertEquals("BT-002", evt.getCorrelationId());
        assertEquals("ERR42", evt.getErrorCode());
        assertEquals("Bank declined", evt.getErrorMessage());
        assertEquals("sig-failed", ((Map<?, ?>) evt.getMetadata().get("headers")).get("X-Bank-Signature"));
        assertEquals("req-456", ((Map<?, ?>) evt.getMetadata().get("headers")).get("X-Bank-Request-Id"));
        assertNotNull(evt.getGatewayResponse());
    }

    @Test
    @DisplayName("Handle Bank Transfer Refund Completed maps to REFUND_SUCCESS")
    @Story("Handle Bank Transfer Refund Completed maps to REFUND_SUCCESS")
    void handle_bank_transfer_refund_completed_type_mapping() {
        String payload = "{" +
                "\"id\":\"BT-003\"," +
                "\"status\":\"TRANSFER.REFUND.COMPLETED\"" +
                "}";

        String url = "http://localhost:" + port + "/api/integrations/webhooks/bank-transfer";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Bank-Signature", "sig-refund");
        headers.add("X-Bank-Request-Id", "req-789");
        Allure.addAttachment("Bank Transfer Request Payload (Refund)", "application/json", payload);
        ResponseEntity<ApiResponse<Void>> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(payload, headers),
                (Class<ApiResponse<Void>>) (Class<?>) ApiResponse.class);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<PaymentCallbackEvent> captor = ArgumentCaptor.forClass(PaymentCallbackEvent.class);
        verify(producer, times(1)).send(captor.capture());
        PaymentCallbackEvent evt = captor.getValue();
        try {
            Allure.addAttachment("Bank Transfer Event Payload (Refund)", "application/json",
                    new ObjectMapper().writeValueAsString(evt));
        } catch (Exception ignored) {}

        assertEquals(PaymentCallbackType.REFUND_SUCCESS, evt.getType());
        assertEquals("BankTransfer", evt.getGatewayName());
        assertEquals("BT-003", evt.getCorrelationId());
        assertEquals("sig-refund", ((Map<?, ?>) evt.getMetadata().get("headers")).get("X-Bank-Signature"));
        assertEquals("req-789", ((Map<?, ?>) evt.getMetadata().get("headers")).get("X-Bank-Request-Id"));
        assertNotNull(evt.getGatewayResponse());
    }
}