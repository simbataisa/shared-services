package com.ahss.integration.webhook;

import com.ahss.dto.response.ApiResponse;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.producer.PaymentCallbackProducer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/webhooks/stripe")
public class StripeWebhookController {

    private final ObjectMapper objectMapper;
    private final PaymentCallbackProducer callbackProducer;

    public StripeWebhookController(ObjectMapper objectMapper, PaymentCallbackProducer callbackProducer) {
        this.objectMapper = objectMapper;
        this.callbackProducer = callbackProducer;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleStripeWebhook(
            @RequestBody String body,
            @RequestHeader(name = "Stripe-Signature", required = false) String stripeSignature
    ) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String eventType = text(root.get("type"));
            JsonNode dataObject = root.path("data").path("object");

            PaymentCallbackEvent event = new PaymentCallbackEvent();
            event.setType(mapStripeType(eventType));
            event.setGatewayName("Stripe");
            event.setReceivedAt(LocalDateTime.now());

            JsonNode metadata = dataObject.path("metadata");
            String correlationId = text(metadata.get("correlationId"));
            event.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());

            event.setPaymentToken(text(metadata.get("paymentToken")));
            event.setRequestCode(text(metadata.get("requestCode")));
            event.setPaymentRequestId(parseUuid(metadata.get("paymentRequestId")));
            event.setPaymentTransactionId(parseUuid(metadata.get("paymentTransactionId")));
            event.setPaymentRefundId(parseUuid(metadata.get("paymentRefundId")));

            event.setExternalTransactionId(text(dataObject.get("id")));
            JsonNode refunds = dataObject.path("refunds").path("data");
            if (refunds.isArray() && refunds.size() > 0) {
                event.setExternalRefundId(text(refunds.get(0).get("id")));
            }

            BigDecimal amount = null;
            String currency = null;
            if (dataObject.has("amount_received")) {
                amount = minorUnitsToMajor(dataObject.path("amount_received").asLong());
                currency = text(dataObject.get("currency"));
            } else if (dataObject.has("amount")) {
                amount = minorUnitsToMajor(dataObject.path("amount").asLong());
                currency = text(dataObject.get("currency"));
            }
            event.setAmount(amount);
            event.setCurrency(currency);

            if (event.getType() == PaymentCallbackType.PAYMENT_FAILED || event.getType() == PaymentCallbackType.REFUND_FAILED) {
                JsonNode lastPaymentError = dataObject.path("last_payment_error");
                if (!lastPaymentError.isMissingNode()) {
                    event.setErrorCode(text(lastPaymentError.get("code")));
                    event.setErrorMessage(text(lastPaymentError.get("message")));
                } else {
                    event.setErrorCode(text(dataObject.get("failure_code")));
                    event.setErrorMessage(text(dataObject.get("failure_message")));
                }
            }

            Map<String, Object> meta = new HashMap<>();
            meta.put("stripeSignature", stripeSignature);
            event.setMetadata(meta);
            Map<String, Object> gatewayResp = objectMapper.convertValue(root, Map.class);
            event.setGatewayResponse(gatewayResp);

            callbackProducer.send(event);
            return ResponseEntity.ok(ApiResponse.ok(null, "Stripe webhook accepted", "/api/integrations/webhooks/stripe"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.notOk(null, "Failed to process Stripe webhook: " + e.getMessage(), "/api/integrations/webhooks/stripe"));
        }
    }

    private PaymentCallbackType mapStripeType(String stripeType) {
        if (stripeType == null) return PaymentCallbackType.PAYMENT_FAILED;
        return switch (stripeType) {
            case "payment_intent.succeeded" -> PaymentCallbackType.PAYMENT_SUCCESS;
            case "payment_intent.payment_failed", "charge.failed", "payment_intent.canceled" -> PaymentCallbackType.PAYMENT_FAILED;
            case "charge.refunded", "refund.created" -> PaymentCallbackType.REFUND_SUCCESS;
            case "refund.updated" -> PaymentCallbackType.REFUND_FAILED;
            default -> PaymentCallbackType.PAYMENT_SUCCESS;
        };
    }

    private String text(JsonNode node) {
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private UUID parseUuid(JsonNode node) {
        String val = text(node);
        try { return val != null ? UUID.fromString(val) : null; } catch (Exception e) { return null; }
    }

    private BigDecimal minorUnitsToMajor(long amountMinor) {
        return BigDecimal.valueOf(amountMinor).movePointLeft(2);
    }
}