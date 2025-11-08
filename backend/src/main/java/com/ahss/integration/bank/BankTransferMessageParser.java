package com.ahss.integration.bank;

import com.ahss.integration.MessageParser;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Parser for Bank Transfer webhook payloads.
 */
public class BankTransferMessageParser implements MessageParser {

    @Override
    public boolean supports(JsonNode root) {
        // Bank transfer payloads may have status/event and typically do not include PayPal/Stripe-specific fields
        boolean hasBankFields = root.has("status") || root.has("event") || (root.has("event_type") && !root.has("resource"));
        boolean notStripe = !(root.has("type") && root.has("data"));
        return hasBankFields && notStripe;
    }

    @Override
    public PaymentCallbackEvent parse(JsonNode root) {
        String eventType = text(root, "event_type");
        if (eventType == null) eventType = text(root, "status");
        if (eventType == null) eventType = text(root, "event");

        PaymentCallbackType mapped = PaymentCallbackType.PAYMENT_SUCCESS;
        BankTransferWebhookEventType t = BankTransferWebhookEventType.fromValue(eventType);
        if (t != null) mapped = t.toCallbackType();

        PaymentCallbackEvent evt = new PaymentCallbackEvent();
        evt.setType(mapped);
        evt.setGatewayName("BankTransfer");

        // Correlation and external IDs
        evt.setCorrelationId(text(root, "id"));
        String externalTxId = text(root, "transaction_id");
        if (externalTxId == null) externalTxId = text(root, "id");
        evt.setExternalTransactionId(externalTxId);

        // Amount and currency
        BigDecimal amount = null;
        JsonNode amtNode = root.path("amount");
        if (!amtNode.isMissingNode()) {
            if (amtNode.isNumber()) {
                amount = amtNode.decimalValue();
            } else if (amtNode.isObject()) {
                String val = text(amtNode, "value");
                if (val != null) {
                    try { amount = new BigDecimal(val); } catch (Exception ignored) {}
                }
            }
        }
        if (amount != null) evt.setAmount(amount);
        String currency = text(root, "currency");
        if (currency == null) currency = text(amtNode, "currency");
        evt.setCurrency(currency);

        // Errors
        JsonNode err = root.path("error");
        if (!err.isMissingNode() && !err.isNull()) {
            evt.setErrorCode(text(err, "code"));
            evt.setErrorMessage(text(err, "message"));
        }

        // Gateway response map
        evt.setGatewayResponse(toMap(root));
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