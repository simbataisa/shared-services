package com.ahss.integration.webhook;

import com.ahss.dto.response.ApiResponse;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.producer.PaymentCallbackProducer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base controller providing shared helpers for webhook handling across payment
 * channels.
 * Subclasses define request mappings and channel-specific parsing logic.
 */
public abstract class BaseWebhookController {

    protected final ObjectMapper objectMapper;
    protected final PaymentCallbackProducer callbackProducer;

    protected BaseWebhookController(ObjectMapper objectMapper, PaymentCallbackProducer callbackProducer) {
        this.objectMapper = objectMapper;
        this.callbackProducer = callbackProducer;
    }

    protected String text(JsonNode node) {
        return node != null && !node.isNull() ? node.asText() : null;
    }

    protected UUID parseUuid(JsonNode node) {
        String val = text(node);
        try {
            return val != null ? UUID.fromString(val) : null;
        } catch (Exception e) {
            return null;
        }
    }

    protected BigDecimal minorUnitsToMajor(long amountMinor) {
        return BigDecimal.valueOf(amountMinor).movePointLeft(2);
    }

    protected Map<String, Object> toMap(JsonNode root) {
        return objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {});
    }

    protected void attachGatewayResponse(PaymentCallbackEvent event, JsonNode root) {
        event.setGatewayResponse(toMap(root));
    }

    protected void sendCallback(PaymentCallbackEvent event) {
        callbackProducer.send(event);
    }

    protected ResponseEntity<ApiResponse<Void>> ok(String path, String message) {
        return ResponseEntity.ok(ApiResponse.ok(null, message, path));
    }

    protected ResponseEntity<ApiResponse<Void>> badRequest(String path, String messagePrefix, Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.notOk(null, messagePrefix + e.getMessage(), path));
    }

    // Abstract hooks for channel-specific behavior
    protected abstract String gatewayName();

    protected abstract String extractEventType(JsonNode root);

    protected abstract PaymentCallbackType mapEventType(String eventType);

    protected abstract void populateEvent(PaymentCallbackEvent event, JsonNode root);

    protected abstract Map<String, Object> metadata(JsonNode root, Map<String, String> headers);

    // Common processing pipeline for webhook handlers
    protected ResponseEntity<ApiResponse<Void>> handleWebhook(
            String body,
            Map<String, String> headers,
            String path,
            String successMessage) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String eventType = extractEventType(root);

            PaymentCallbackEvent event = new PaymentCallbackEvent();
            event.setType(mapEventType(eventType));
            event.setGatewayName(gatewayName());
            event.setReceivedAt(LocalDateTime.now());

            populateEvent(event, root);
            Map<String, Object> meta = metadata(root, headers);
            if (meta != null) {
                event.setMetadata(meta);
            }

            attachGatewayResponse(event, root);
            sendCallback(event);
            return ok(path, successMessage);
        } catch (Exception e) {
            return badRequest(path, "Failed to process " + gatewayName() + " webhook: ", e);
        }
    }
}