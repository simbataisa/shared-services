package com.ahss.integration.bank;

import com.ahss.dto.response.ApiResponse;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.producer.PaymentCallbackProducer;
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

@RestController
@RequestMapping("/api/integrations/webhooks/bank-transfer")
public class BankTransferWebhookController extends BaseWebhookController {

    public BankTransferWebhookController(ObjectMapper objectMapper, PaymentCallbackProducer callbackProducer) {
        super(objectMapper, callbackProducer);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleBankTransferWebhook(
            @RequestBody String body,
            @RequestHeader(name = "X-Bank-Signature", required = false) String signature,
            @RequestHeader(name = "X-Bank-Request-Id", required = false) String requestId
    ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Bank-Signature", signature);
        headers.put("X-Bank-Request-Id", requestId);
        return handleWebhook(body, headers, "/api/integrations/webhooks/bank-transfer", "Bank Transfer webhook accepted");
    }

    @Override
    protected String gatewayName() { return "BankTransfer"; }

    @Override
    protected String extractEventType(JsonNode root) {
        String v = text(root.get("event_type"));
        if (v == null) v = text(root.get("status"));
        if (v == null) v = text(root.get("event"));
        return v;
    }

    @Override
    protected PaymentCallbackType mapEventType(String eventType) {
        BankTransferWebhookEventType t = BankTransferWebhookEventType.fromValue(eventType);
        return t != null ? t.toCallbackType() : PaymentCallbackType.PAYMENT_SUCCESS;
    }

    @Override
    protected void populateEvent(PaymentCallbackEvent event, JsonNode root) {
        // Correlation and external IDs
        event.setCorrelationId(text(root.get("id")));
        String externalTxId = text(root.get("transaction_id"));
        if (externalTxId == null) externalTxId = text(root.get("id"));
        event.setExternalTransactionId(externalTxId);

        // Amount and currency
        BigDecimal amount = null;
        JsonNode amtNode = root.path("amount");
        if (amtNode.isNumber()) {
            amount = BigDecimal.valueOf(amtNode.decimalValue().doubleValue());
        } else if (amtNode.isObject()) {
            String val = text(amtNode.get("value"));
            if (val != null) {
                try { amount = new BigDecimal(val); } catch (Exception ignored) {}
            }
        }
        if (amount != null) event.setAmount(amount);
        String currency = text(root.get("currency"));
        if (currency == null) currency = text(amtNode.get("currency"));
        event.setCurrency(currency);

        // Errors
        JsonNode err = root.path("error");
        if (!err.isMissingNode() && !err.isNull()) {
            event.setErrorCode(text(err.get("code")));
            event.setErrorMessage(text(err.get("message")));
        }
    }

    @Override
    protected Map<String, Object> metadata(JsonNode root, Map<String, String> headers) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("headers", headers);
        meta.put("reference", text(root.get("reference")));
        meta.put("rawEventType", extractEventType(root));
        return meta;
    }
}