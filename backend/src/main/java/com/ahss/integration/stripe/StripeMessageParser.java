package com.ahss.integration.stripe;

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
 * Parser for Stripe webhook payloads.
 */
public class StripeMessageParser implements MessageParser {

    @Override
    public boolean supports(JsonNode root) {
        return root.has("type") && root.has("data");
    }

    @Override
    public PaymentCallbackEvent parse(JsonNode root) {
        String stripeType = text(root, "type");
        JsonNode object = root.path("data").path("object");
        String correlationId = text(root, "id");
        String currency = text(object, "currency");
        String externalTxId = text(object, "id");

        Long amountInMinor = longVal(object, "amount_received");
        if (amountInMinor == null)
            amountInMinor = longVal(object, "amount");

        PaymentCallbackEvent evt = new PaymentCallbackEvent();
        evt.setCorrelationId(correlationId);
        evt.setGatewayName("Stripe");
        evt.setExternalTransactionId(externalTxId);
        if (amountInMinor != null)
            evt.setAmount(java.math.BigDecimal.valueOf(amountInMinor).movePointLeft(2));
        evt.setCurrency(currency);
        evt.setGatewayResponse(toMap(root));

        PaymentCallbackType mapped = PaymentChannelIntegrationEventTypeMapper.mapStripe(stripeType);
        if (mapped != null) {
            evt.setType(mapped);
        } else {
            evt.setType(PaymentCallbackType.PAYMENT_FAILED);
        }

        if (PaymentCallbackType.PAYMENT_FAILED.equals(evt.getType())) {
            String errCode = text(object.path("last_payment_error"), "code");
            String errMsg = text(object.path("last_payment_error"), "message");
            evt.setErrorCode(errCode);
            evt.setErrorMessage(errMsg);
        } else if (PaymentCallbackType.REFUND_SUCCESS.equals(evt.getType())) {
            String refundId = text(object.path("refunds").path("data").path(0), "id");
            evt.setExternalRefundId(refundId);
        }

        Long createdEpoch = longVal(root, "created");
        if (createdEpoch != null) {
            evt.setReceivedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(createdEpoch), ZoneOffset.UTC));
        }

        return evt;
    }

    private String text(JsonNode node, String field) {
        if (node == null || field == null)
            return null;
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private Long longVal(JsonNode node, String field) {
        if (node == null || field == null)
            return null;
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asLong();
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
        if (node == null || node.isNull())
            return null;
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
            for (JsonNode n : node)
                list.add(jsonNodeToJava(n));
            return list;
        }
        if (node.isNumber())
            return node.numberValue();
        if (node.isBoolean())
            return node.asBoolean();
        return node.asText();
    }
}