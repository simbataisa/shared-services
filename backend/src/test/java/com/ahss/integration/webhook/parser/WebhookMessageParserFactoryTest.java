package com.ahss.integration.webhook.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Integration")
@Feature("Webhook Message Parser Factory")
class WebhookMessageParserFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Selects Stripe parser for Stripe-shaped payload")
    @Story("Selects Stripe parser")
    void selects_stripe_parser() throws Exception {
        String json = "{ \"type\": \"payment_intent.succeeded\", \"data\": { \"object\": {} } }";
        JsonNode root = objectMapper.readTree(json);
        WebhookMessageParser parser = WebhookMessageParserFactory.forPayload(root);
        assertNotNull(parser);
        assertEquals(StripeWebhookMessageParser.class, parser.getClass());
    }

    @Test
    @DisplayName("Selects PayPal parser for PayPal-shaped payload")
    @Story("Selects PayPal parser")
    void selects_paypal_parser() throws Exception {
        String json = "{ \"event_type\": \"PAYMENT.SALE.COMPLETED\", \"resource\": {} }";
        JsonNode root = objectMapper.readTree(json);
        WebhookMessageParser parser = WebhookMessageParserFactory.forPayload(root);
        assertNotNull(parser);
        assertEquals(PayPalWebhookMessageParser.class, parser.getClass());
    }

    @Test
    @DisplayName("Returns null for unknown payload shape")
    @Story("Returns null for unknown shape")
    void returns_null_for_unknown_shape() throws Exception {
        String json = "{ \"foo\": \"bar\" }";
        JsonNode root = objectMapper.readTree(json);
        WebhookMessageParser parser = WebhookMessageParserFactory.forPayload(root);
        assertNull(parser);
    }
}