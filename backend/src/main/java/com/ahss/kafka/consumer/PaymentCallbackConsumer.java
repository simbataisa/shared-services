package com.ahss.kafka.consumer;

import com.ahss.integration.paypal.PayPalWebhookEventType;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.saga.PaymentSagaOrchestrator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentCallbackConsumer extends BaseJsonKafkaConsumer {

    private final PaymentSagaOrchestrator orchestrator;

    public PaymentCallbackConsumer(PaymentSagaOrchestrator orchestrator, ObjectMapper objectMapper) {
        super(objectMapper);
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-callbacks}", groupId = "${app.kafka.consumer.group}")
    public void onMessage(String message) {
        try {
            log.info("Payment callback received: {}", message);
            PaymentCallbackEvent event = parseMessage(message);
            log.info("Payment callback parsed: {}", event);
            orchestrator.handle(event);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private PaymentCallbackEvent parseMessage(String message) throws Exception {
        JsonNode root = readTree(message);
        // Stripe payloads typically have: { id, type, data: { object: {...} } }
        if (root.has("type") && root.has("data")) {
            return parseStripe(root);
        }
        // PayPal webhooks often have: { id, event_type, resource: { ... } }
        if (root.has("event_type") && root.has("resource")) {
            return parsePaypal(root);
        }
        // Fall back to internal format
        return readValue(message, PaymentCallbackEvent.class);
    }

    private PaymentCallbackEvent parseStripe(JsonNode root) {
        String stripeType = text(root, "type");
        JsonNode object = root.path("data").path("object");
        String correlationId = text(root, "id");
        String currency = text(object, "currency");
        // prefer payment_intent id as the externalTransactionId
        String externalTxId = text(object, "id");
        // amount_received or amount for different events (Stripe amounts are in cents)
        Long amountInMinor = longVal(object, "amount_received");
        if (amountInMinor == null) amountInMinor = longVal(object, "amount");

        PaymentCallbackEvent evt = new PaymentCallbackEvent();
        evt.setCorrelationId(correlationId);
        evt.setGatewayName("Stripe");
        evt.setExternalTransactionId(externalTxId);
        if (amountInMinor != null) evt.setAmount(java.math.BigDecimal.valueOf(amountInMinor).movePointLeft(2));
        evt.setCurrency(currency);
        evt.setGatewayResponse(toMap(root));
        // Map event type
        if ("payment_intent.succeeded".equals(stripeType)) {
            evt.setType(PaymentCallbackType.PAYMENT_SUCCESS);
        } else if ("payment_intent.payment_failed".equals(stripeType) || "charge.failed".equals(stripeType)) {
            evt.setType(PaymentCallbackType.PAYMENT_FAILED);
            // Attempt to extract error info
            String errCode = text(object.path("last_payment_error"), "code");
            String errMsg = text(object.path("last_payment_error"), "message");
            evt.setErrorCode(errCode);
            evt.setErrorMessage(errMsg);
        } else if ("charge.refunded".equals(stripeType) || "charge.refund.updated".equals(stripeType)) {
            evt.setType(PaymentCallbackType.REFUND_SUCCESS);
            String refundId = text(object.path("refunds").path("data").path(0), "id");
            evt.setExternalRefundId(refundId);
        } else {
            // Default to failure for unknown types to avoid false positives
            evt.setType(PaymentCallbackType.PAYMENT_FAILED);
        }
        // Received time
        Long createdEpoch = longVal(root, "created");
        if (createdEpoch != null) {
            evt.setReceivedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(createdEpoch), ZoneOffset.UTC));
        }
        return evt;
    }

    private PaymentCallbackEvent parsePaypal(JsonNode root) {
        String eventType = text(root, "event_type");
        JsonNode resource = root.path("resource");
        String correlationId = text(root, "id");
        String currency = text(resource.path("amount").path("currency_code"), null);
        String externalTxId = text(resource, "id");
        Long createEpoch = longVal(resource, "create_time"); // often ISO-8601; handle below

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
        // Map event type using canonical enum
        PayPalWebhookEventType t = PayPalWebhookEventType.fromValue(eventType);
        if (t == null) {
            evt.setType(PaymentCallbackType.PAYMENT_FAILED);
        } else {
            switch (t) {
                case PAYMENT_SALE_COMPLETED, CHECKOUT_ORDER_APPROVED -> evt.setType(PaymentCallbackType.PAYMENT_SUCCESS);
                case PAYMENT_SALE_DENIED, PAYMENT_CAPTURE_DENIED -> {
                    evt.setType(PaymentCallbackType.PAYMENT_FAILED);
                    evt.setErrorCode(text(resource.path("status"), null));
                    evt.setErrorMessage(text(resource.path("reason_code"), null));
                }
                case PAYMENT_SALE_REFUNDED, PAYMENT_CAPTURE_REFUNDED -> {
                    evt.setType(PaymentCallbackType.REFUND_SUCCESS);
                    evt.setExternalRefundId(text(resource, "id"));
                }
            }
        }
        // Received time: PayPal uses ISO timestamp, fallback to now
        String isoTime = text(resource, "create_time");
        if (isoTime != null) {
            try {
                evt.setReceivedAt(LocalDateTime.ofInstant(Instant.parse(isoTime), ZoneOffset.UTC));
            } catch (Exception ignored) {}
        }
        return evt;
    }

    
}