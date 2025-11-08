package com.ahss.integration.bank;

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
@Feature("Bank Transfer Integration")
class BankTransferMessageParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BankTransferMessageParser parser = new BankTransferMessageParser();

    @Test
    @DisplayName("supports() returns true for Bank Transfer-shaped payload")
    @Story("Supports Bank Transfer-shaped payload")
    void supports_true_for_bank_shape() throws Exception {
        String json = "{\n" +
                "  \"id\": \"BT-123\",\n" +
                "  \"status\": \"TRANSFER.COMPLETED\",\n" +
                "  \"amount\": { \"value\": \"100.00\", \"currency\": \"USD\" }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        assertTrue(parser.supports(root));
    }

    @Test
    @DisplayName("supports() returns false for Stripe-shaped payload")
    @Story("Supports non-Bank Transfer-shaped payload")
    void supports_false_for_stripe_shape() throws Exception {
        String json = "{ \"type\": \"payment_intent.succeeded\", \"data\": {} }";
        JsonNode root = objectMapper.readTree(json);
        assertFalse(parser.supports(root));
    }

    @Test
    @DisplayName("Parses payment success with amount object and currency")
    @Story("Parses payment success")
    void parses_payment_success_with_amount_object() throws Exception {
        String json = "{\n" +
                "  \"id\": \"BT-200\",\n" +
                "  \"status\": \"TRANSFER.COMPLETED\",\n" +
                "  \"transaction_id\": \"ext-200\",\n" +
                "  \"amount\": { \"value\": \"45.67\", \"currency\": \"USD\" }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
        assertEquals("BankTransfer", evt.getGatewayName());
        assertEquals("BT-200", evt.getCorrelationId());
        assertEquals("ext-200", evt.getExternalTransactionId());
        assertEquals(new BigDecimal("45.67"), evt.getAmount());
        assertEquals("USD", evt.getCurrency());
        assertNotNull(evt.getGatewayResponse());
    }

    @Test
    @DisplayName("Parses payment failed and enriches error fields")
    @Story("Parses payment failed")
    void parses_payment_failed_with_error() throws Exception {
        String json = "{\n" +
                "  \"id\": \"BT-300\",\n" +
                "  \"status\": \"TRANSFER.FAILED\",\n" +
                "  \"error\": { \"code\": \"ERR42\", \"message\": \"Bank declined\" }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_FAILED, evt.getType());
        assertEquals("BankTransfer", evt.getGatewayName());
        assertEquals("BT-300", evt.getCorrelationId());
        assertEquals("ERR42", evt.getErrorCode());
        assertEquals("Bank declined", evt.getErrorMessage());
        assertNotNull(evt.getGatewayResponse());
    }

    @Test
    @DisplayName("Parses refund success and maps event type correctly")
    @Story("Parses refund success")
    void parses_refund_success_type_mapping() throws Exception {
        String json = "{\n" +
                "  \"id\": \"BT-400\",\n" +
                "  \"status\": \"TRANSFER.REFUND.COMPLETED\"\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.REFUND_SUCCESS, evt.getType());
        assertEquals("BankTransfer", evt.getGatewayName());
    }
}