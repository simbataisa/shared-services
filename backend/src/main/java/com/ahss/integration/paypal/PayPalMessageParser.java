package com.ahss.integration.paypal;

import com.ahss.integration.mapper.PaymentChannelIntegrationEventTypeMapper;
import com.ahss.integration.MessageParser;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Parser for PayPal webhook payloads.
 */
public class PayPalMessageParser implements MessageParser {

    @Override
    public boolean supports(JsonNode root) {
        return root.has("event_type") && root.has("resource");
    }

    @Override
    public PaymentCallbackEvent parse(JsonNode root) {
        String eventType = text(root, "event_type");
        JsonNode resource = root.path("resource");
        String correlationId = text(root, "id");
        String currency = text(resource.path("amount"), "currency_code");
        String externalTxId = text(resource, "id");

        PaymentCallbackEvent evt = new PaymentCallbackEvent();
        evt.setCorrelationId(correlationId);
        evt.setGatewayName("PayPal");
        evt.setExternalTransactionId(externalTxId);

        java.math.BigDecimal amount = null;
        String valueStr = text(resource.path("amount"), "value");
        if (valueStr != null) {
            try { amount = new java.math.BigDecimal(valueStr); } catch (Exception ignored) {}
        }
        if (amount != null) evt.setAmount(amount);
        evt.setCurrency(currency);
        evt.setGatewayResponse(toMap(root));

        PaymentCallbackType mapped = PaymentChannelIntegrationEventTypeMapper.mapPayPal(eventType);
        if (mapped != null) {
            evt.setType(mapped);
        } else {
            evt.setType(PaymentCallbackType.PAYMENT_FAILED);
        }

        if (PaymentCallbackType.PAYMENT_FAILED.equals(evt.getType())) {
            evt.setErrorCode(text(resource, "status"));
            evt.setErrorMessage(text(resource, "reason_code"));
        } else if (PaymentCallbackType.REFUND_SUCCESS.equals(evt.getType())) {
            evt.setExternalRefundId(text(resource, "id"));
        }

        String isoTime = text(resource, "create_time");
        if (isoTime != null) {
            try {
                evt.setReceivedAt(LocalDateTime.ofInstant(Instant.parse(isoTime), ZoneOffset.UTC));
            } catch (Exception ignored) {}
        }

        return evt;
    }

    private String text(JsonNode node, String field) {
        if (node == null || field == null) return null;
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            map.put(e.getKey(), jsonNodeToJava(e.getValue()));
        }
        return map;
    }

    private Object jsonNodeToJava(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isObject()) {
            Map<String, Object> child = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                child.put(e.getKey(), jsonNodeToJava(e.getValue()));
            }
            return child;
        }
        if (node.isArray()) {
            java.util.List<Object> list = new java.util.ArrayList<>();
            for (JsonNode n : node) list.add(jsonNodeToJava(n));
            return list;
        }
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.asBoolean();
        return node.asText();
    }
}