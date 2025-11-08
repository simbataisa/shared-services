package com.ahss.integration.parser;

import com.ahss.integration.MessageParser;
import com.ahss.integration.MessageParserFactory;
import com.ahss.integration.paypal.PayPalMessageParser;
import com.ahss.integration.stripe.StripeMessageParser;
import com.ahss.integration.bank.BankTransferMessageParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Payment Channel Integration")
@Feature("Message Parser")
class MessageParserFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Selects Stripe parser for Stripe-shaped payload")
    @Story("Selects Stripe parser")
    void selects_stripe_parser() throws Exception {
        String json = "{ \"type\": \"payment_intent.succeeded\", \"data\": { \"object\": {} } }";
        JsonNode root = objectMapper.readTree(json);
        MessageParser parser = MessageParserFactory.forPayload(root);
        assertNotNull(parser);
        assertEquals(StripeMessageParser.class, parser.getClass());
    }

    @Test
    @DisplayName("Selects PayPal parser for PayPal-shaped payload")
    @Story("Selects PayPal parser")
    void selects_paypal_parser() throws Exception {
        String json = "{ \"event_type\": \"PAYMENT.SALE.COMPLETED\", \"resource\": {} }";
        JsonNode root = objectMapper.readTree(json);
        MessageParser parser = MessageParserFactory.forPayload(root);
        assertNotNull(parser);
        assertEquals(PayPalMessageParser.class, parser.getClass());
    }

    @Test
    @DisplayName("Selects Bank Transfer parser for bank-shaped payload")
    @Story("Selects Bank Transfer parser")
    void selects_bank_transfer_parser() throws Exception {
        String json = "{ \"status\": \"TRANSFER.INITIATED\", \"amount\": 1000, \"currency\": \"USD\" }";
        JsonNode root = objectMapper.readTree(json);
        MessageParser parser = MessageParserFactory.forPayload(root);
        assertNotNull(parser);
        assertEquals(BankTransferMessageParser.class, parser.getClass());
    }

    @Test
    @DisplayName("Returns null for unknown payload shape")
    @Story("Returns null for unknown shape")
    void returns_null_for_unknown_shape() throws Exception {
        String json = "{ \"foo\": \"bar\" }";
        JsonNode root = objectMapper.readTree(json);
        MessageParser parser = MessageParserFactory.forPayload(root);
        assertNull(parser);
    }
}