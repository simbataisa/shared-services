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
@RequestMapping("/api/integrations/webhooks/paypal")
public class PayPalWebhookController {

    private final ObjectMapper objectMapper;
    private final PaymentCallbackProducer callbackProducer;

    public PayPalWebhookController(ObjectMapper objectMapper, PaymentCallbackProducer callbackProducer) {
        this.objectMapper = objectMapper;
        this.callbackProducer = callbackProducer;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handlePayPalWebhook(
            @RequestBody String body,
            @RequestHeader(name = "PayPal-Transmission-Sig", required = false) String transmissionSig,
            @RequestHeader(name = "PayPal-Transmission-Id", required = false) String transmissionId
    ) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String eventType = text(root.get("event_type"));
            JsonNode resource = root.path("resource");

            PaymentCallbackEvent event = new PaymentCallbackEvent();
            event.setType(mapPayPalType(eventType));
            event.setGatewayName("PayPal");
            event.setReceivedAt(LocalDateTime.now());

            String correlationId = text(resource.get("custom_id"));
            if (correlationId == null) correlationId = text(resource.get("invoice_id"));
            event.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());

            event.setPaymentToken(text(resource.get("invoice_id")));
            event.setRequestCode(text(resource.get("invoice_id")));

            event.setExternalTransactionId(text(resource.get("id")));

            BigDecimal amount = null;
            String currency = null;
            JsonNode amountNode = resource.path("amount");
            if (!amountNode.isMissingNode()) {
                String value = text(amountNode.get("value"));
                currency = text(amountNode.get("currency_code"));
                if (value != null) {
                    try { amount = new BigDecimal(value); } catch (Exception ignored) {}
                }
            }
            event.setAmount(amount);
            event.setCurrency(currency);

            if (event.getType() == PaymentCallbackType.PAYMENT_FAILED || event.getType() == PaymentCallbackType.REFUND_FAILED) {
                JsonNode reason = resource.path("reason");
                event.setErrorMessage(text(reason));
            }

            Map<String, Object> meta = new HashMap<>();
            meta.put("transmissionSig", transmissionSig);
            meta.put("transmissionId", transmissionId);
            event.setMetadata(meta);
            Map<String, Object> gatewayResp = objectMapper.convertValue(root, Map.class);
            event.setGatewayResponse(gatewayResp);

            callbackProducer.send(event);
            return ResponseEntity.ok(ApiResponse.ok(null, "PayPal webhook accepted", "/api/integrations/webhooks/paypal"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.notOk(null, "Failed to process PayPal webhook: " + e.getMessage(), "/api/integrations/webhooks/paypal"));
        }
    }

    private PaymentCallbackType mapPayPalType(String eventType) {
        if (eventType == null) return PaymentCallbackType.PAYMENT_FAILED;
        return switch (eventType) {
            case "PAYMENT.CAPTURE.COMPLETED" -> PaymentCallbackType.PAYMENT_SUCCESS;
            case "PAYMENT.CAPTURE.DENIED", "PAYMENT.CAPTURE.FAILED" -> PaymentCallbackType.PAYMENT_FAILED;
            case "PAYMENT.CAPTURE.REFUNDED", "PAYMENT.REFUND.COMPLETED" -> PaymentCallbackType.REFUND_SUCCESS;
            case "PAYMENT.REFUND.DENIED" -> PaymentCallbackType.REFUND_FAILED;
            default -> PaymentCallbackType.PAYMENT_SUCCESS;
        };
    }

    private String text(JsonNode node) {
        return node != null && !node.isNull() ? node.asText() : null;
    }
}