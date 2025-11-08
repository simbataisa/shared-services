package com.ahss.integration.stripe;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Payment Channel Integration")
@Feature("Stripe Integration")
class StripeMessageParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StripeMessageParser parser = new StripeMessageParser();

    @Test
    @DisplayName("supports() returns true for Stripe-shaped payload")
    @Story("Supports Stripe-shaped payload")
    void supports_true_for_stripe_shape() throws Exception {
        String json = "{\n" +
                "  \"id\": \"evt_123\",\n" +
                "  \"type\": \"payment_intent.succeeded\",\n" +
                "  \"data\": { \"object\": { \"id\": \"pi_abc\" } }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        assertTrue(parser.supports(root));
    }

    @Test
    @DisplayName("supports() returns false for non-Stripe payload")
    @Story("Supports non-Stripe-shaped payload")
    void supports_false_for_non_stripe() throws Exception {
        String json = "{ \"event_type\": \"PAYMENT.SALE.COMPLETED\", \"resource\": {} }";
        JsonNode root = objectMapper.readTree(json);
        assertFalse(parser.supports(root));
    }

    @Test
    @DisplayName("Parses payment success with amount, currency, and timestamps")
    @Story("Parses payment success")
    void parses_payment_success() throws Exception {
        String json = "{\n" +
                "  \"id\": \"evt_123\",\n" +
                "  \"type\": \"payment_intent.succeeded\",\n" +
                "  \"created\": 1700000000,\n" +
                "  \"data\": {\n" +
                "    \"object\": {\n" +
                "      \"id\": \"pi_abc\",\n" +
                "      \"currency\": \"usd\",\n" +
                "      \"amount_received\": 1234\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
        assertEquals("Stripe", evt.getGatewayName());
        assertEquals("pi_abc", evt.getExternalTransactionId());
        assertEquals(new BigDecimal("12.34"), evt.getAmount());
        assertEquals("usd", evt.getCurrency());
        assertNotNull(evt.getReceivedAt());
    }

    @Test
    @DisplayName("Parses payment failed and enriches error fields")
    @Story("Parses payment failed")
    void parses_payment_failed_with_error() throws Exception {
        String json = "{\n" +
                "  \"id\": \"evt_456\",\n" +
                "  \"type\": \"payment_intent.payment_failed\",\n" +
                "  \"data\": {\n" +
                "    \"object\": {\n" +
                "      \"id\": \"pi_fail\",\n" +
                "      \"last_payment_error\": {\n" +
                "        \"code\": \"card_declined\",\n" +
                "        \"message\": \"Your card was declined.\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_FAILED, evt.getType());
        assertEquals("pi_fail", evt.getExternalTransactionId());
        assertEquals("card_declined", evt.getErrorCode());
        assertEquals("Your card was declined.", evt.getErrorMessage());
    }

    @Test
    @DisplayName("Parses refund success and extracts external refund id")
    @Story("Parses refund success")
    void parses_refund_success_with_refund_id() throws Exception {
        String json = "{\n" +
                "  \"id\": \"evt_789\",\n" +
                "  \"type\": \"charge.refunded\",\n" +
                "  \"data\": {\n" +
                "    \"object\": {\n" +
                "      \"id\": \"ch_123\",\n" +
                "      \"refunds\": {\n" +
                "        \"data\": [ { \"id\": \"re_abc\" } ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonNode root = objectMapper.readTree(json);
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.REFUND_SUCCESS, evt.getType());
        assertEquals("re_abc", evt.getExternalRefundId());
    }
}