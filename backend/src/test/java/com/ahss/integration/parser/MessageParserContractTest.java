package com.ahss.integration.parser;

import com.ahss.integration.MessageParser;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
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
class MessageParserContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    static class TestParser implements MessageParser {
        @Override
        public boolean supports(JsonNode root) {
            return root.has("ok");
        }

        @Override
        public PaymentCallbackEvent parse(JsonNode root) {
            PaymentCallbackEvent evt = new PaymentCallbackEvent();
            evt.setType(PaymentCallbackType.PAYMENT_SUCCESS);
            evt.setGatewayName("Test");
            evt.setCorrelationId(root.path("corr").asText());
            evt.setExternalTransactionId(root.path("txid").asText());
            return evt;
        }
    }

    @Test
    @DisplayName("Interface contract: supports() and parse() are callable and consistent")
    @Story("Interface contract")
    void interface_contract_works() throws Exception {
        String json = "{ \"ok\": true, \"corr\": \"c1\", \"txid\": \"t1\" }";
        JsonNode root = objectMapper.readTree(json);

        MessageParser parser = new TestParser();
        assertTrue(parser.supports(root));
        PaymentCallbackEvent evt = parser.parse(root);
        assertEquals(PaymentCallbackType.PAYMENT_SUCCESS, evt.getType());
        assertEquals("Test", evt.getGatewayName());
        assertEquals("c1", evt.getCorrelationId());
        assertEquals("t1", evt.getExternalTransactionId());
    }
}