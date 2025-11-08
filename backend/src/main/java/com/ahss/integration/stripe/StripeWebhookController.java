package com.ahss.integration.stripe;

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
@RequestMapping("/api/integrations/webhooks/stripe")
public class StripeWebhookController extends BaseWebhookController {

  public StripeWebhookController(
      ObjectMapper objectMapper, PaymentCallbackProducer callbackProducer) {
    super(objectMapper, callbackProducer);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> handleStripeWebhook(
      @RequestBody String body,
      @RequestHeader(name = "Stripe-Signature", required = false) String stripeSignature) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Stripe-Signature", stripeSignature);
    return handleWebhook(
        body, headers, "/api/integrations/webhooks/stripe", "Stripe webhook accepted");
  }

  @Override
  protected String gatewayName() {
    return "Stripe";
  }

  @Override
  protected String extractEventType(JsonNode root) {
    return text(root.get("type"));
  }

  @Override
  protected PaymentCallbackType mapEventType(String eventType) {
    return PaymentChannelIntegrationEventTypeMapper.mapStripe(eventType);
  }

  @Override
  protected void populateEvent(PaymentCallbackEvent event, JsonNode root) {
    JsonNode dataObject = root.path("data").path("object");
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

    if (event.getType() == PaymentCallbackType.PAYMENT_FAILED
        || event.getType() == PaymentCallbackType.REFUND_FAILED) {
      JsonNode lastPaymentError = dataObject.path("last_payment_error");
      if (!lastPaymentError.isMissingNode()) {
        event.setErrorCode(text(lastPaymentError.get("code")));
        event.setErrorMessage(text(lastPaymentError.get("message")));
      } else {
        event.setErrorCode(text(dataObject.get("failure_code")));
        event.setErrorMessage(text(dataObject.get("failure_message")));
      }
    }
  }

  @Override
  protected Map<String, Object> metadata(JsonNode root, Map<String, String> headers) {
    Map<String, Object> meta = new HashMap<>();
    meta.put("stripeSignature", headers.get("Stripe-Signature"));
    return meta;
  }
}
