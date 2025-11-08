package com.ahss.integration.paypal;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Payment Channel Integration")
@Feature("PayPal Integration")
class PayPalMessageParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PayPalMessageParser parser = new PayPalMessageParser();

    @Test
    @DisplayName("supports() returns true for PayPal-shaped payload")
    @Story("Supports PayPal-shaped payload")
    void supports_true_for_paypal_shape() throws Exception {
        String json = "{\n" +
                "  \"id\": \"WH-123\",\n" +
                "  \"event_type\": \"PAYMENT.SALE.COMPLETED\",\n" +
                "  \"resource\": { \"id\": \"9XY\" }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        assertTrue(parser.supports(root));
    }

    @Test
    @DisplayName("supports() returns false for non-PayPal payload")
    @Story("Supports non-PayPal-shaped payload")
    void supports_false_for_non_paypal() throws Exception {
        String json = "{ \"type\": \"payment_intent.succeeded\", \"data\": {} }";
        JsonNode root = objectMapper.readTree(json);
        assertFalse(parser.supports(root));
    }

    @Test
    @DisplayName("Parses payment success with amount, currency, and timestamp")
    @Story("Parses payment success")
    void parses_payment_success() throws Exception {
        String json = "{\n" +
                "  \"id\": \"WH-123\",\n" +
                "  \"event_type\": \"PAYMENT.SALE.COMPLETED\",\n" +
                "  \"resource\": {\n" +
                "    \"id\": \"SALE-1\",\n" +
                "    \"amount\": { \"value\": \"45.67\", \"currency_code\": \"USD\" },\n" +
                "    \"create_time\": \"2021-04-10T17:20:40Z\"\n" +
                "  }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
        assertEquals("PayPal", evt.getGatewayName());
        assertEquals("SALE-1", evt.getExternalTransactionId());
        assertEquals(new BigDecimal("45.67"), evt.getAmount());
        assertEquals("USD", evt.getCurrency());
        assertNotNull(evt.getReceivedAt());
    }

    @Test
    @DisplayName("Parses payment failed and enriches error fields")
    @Story("Parses payment failed")
    void parses_payment_failed_with_error() throws Exception {
        String json = "{\n" +
                "  \"id\": \"WH-456\",\n" +
                "  \"event_type\": \"PAYMENT.SALE.DENIED\",\n" +
                "  \"resource\": {\n" +
                "    \"id\": \"SALE-2\",\n" +
                "    \"status\": \"DENIED\",\n" +
                "    \"reason_code\": \"INVALID_PAYMENT\"\n" +
                "  }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_FAILED, evt.getType());
        assertEquals("SALE-2", evt.getExternalTransactionId());
        assertEquals("DENIED", evt.getErrorCode());
        assertEquals("INVALID_PAYMENT", evt.getErrorMessage());
    }

    @Test
    @DisplayName("Parses refund success and extracts external refund id")
    @Story("Parses refund success")
    void parses_refund_success_with_refund_id() throws Exception {
        String json = "{\n" +
                "  \"id\": \"WH-789\",\n" +
                "  \"event_type\": \"PAYMENT.REFUND.COMPLETED\",\n" +
                "  \"resource\": {\n" +
                "    \"id\": \"RFND-123\"\n" +
                "  }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.REFUND_SUCCESS, evt.getType());
        assertEquals("RFND-123", evt.getExternalRefundId());
    }
}