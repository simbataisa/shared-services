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
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

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

    @Test
    void generate_contract_stubs_viaWireMock() throws IOException {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        try {
            server.stubFor(get(urlPathEqualTo("/contracts/parser-factory"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));
            Path stubsPath = Paths.get("target/stubs/parser-factory");
            Files.createDirectories(stubsPath);
            int idx = 0;
            for (StubMapping stub : server.getStubMappings()) {
                String filename = String.format("stub_%d_%s.json", idx++, System.currentTimeMillis());
                Path stubFile = stubsPath.resolve(filename);
                String stubJson = StubMapping.buildJsonStringFor(stub);
                Files.writeString(stubFile, stubJson);
            }
        } finally {
            server.stop();
        }
    }
}