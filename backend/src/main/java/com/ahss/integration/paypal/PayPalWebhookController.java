package com.ahss.integration.paypal;

import com.ahss.dto.response.ApiResponse;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.producer.PaymentCallbackProducer;
import com.ahss.integration.mapper.PaymentChannelIntegrationEventTypeMapper;
import com.ahss.integration.webhook.BaseWebhookController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/integrations/webhooks/paypal")
public class PayPalWebhookController extends BaseWebhookController {

    public PayPalWebhookController(ObjectMapper objectMapper, PaymentCallbackProducer callbackProducer) {
        super(objectMapper, callbackProducer);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handlePayPalWebhook(
            @RequestBody String body,
            @RequestHeader(name = "PayPal-Transmission-Sig", required = false) String transmissionSig,
            @RequestHeader(name = "PayPal-Transmission-Id", required = false) String transmissionId
    ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("PayPal-Transmission-Sig", transmissionSig);
        headers.put("PayPal-Transmission-Id", transmissionId);
        return handleWebhook(body, headers, "/api/integrations/webhooks/paypal", "PayPal webhook accepted");
    }

    @Override
    protected String gatewayName() { return "PayPal"; }

    @Override
    protected String extractEventType(JsonNode root) { return text(root.get("event_type")); }

    @Override
    protected PaymentCallbackType mapEventType(String eventType) { return PaymentChannelIntegrationEventTypeMapper.mapPayPal(eventType); }

    @Override
    protected void populateEvent(PaymentCallbackEvent event, JsonNode root) {
        JsonNode resource = root.path("resource");

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
    }

    @Override
    protected Map<String, Object> metadata(JsonNode root, Map<String, String> headers) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("transmissionSig", headers.get("PayPal-Transmission-Sig"));
        meta.put("transmissionId", headers.get("PayPal-Transmission-Id"));
        return meta;
    }
}