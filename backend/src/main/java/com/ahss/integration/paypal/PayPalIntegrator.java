package com.ahss.integration.paypal;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.integration.PaymentIntegrator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Implementation of PaymentIntegrator for PayPal processor. Handles conversion and outbound calls
 * to PayPal REST APIs.
 */
@Slf4j
@Component
public class PayPalIntegrator implements PaymentIntegrator {

  private final RestTemplate restTemplate;
  private final String orderApiUrl; // Configurable for tests
  private final String refundApiUrl;
  private final String tokenApiUrl;
  private final String clientId;
  private final String clientSecret; // Configurable for tests
  private final ObjectMapper objectMapper;

  @org.springframework.beans.factory.annotation.Autowired
  public PayPalIntegrator(
      RestTemplate restTemplate,
      @Value("${paypal.orderApiUrl:https://api-m.paypal.com/v2/checkout/orders}")
          String orderApiUrl,
      @Value(
              "${paypal.refundApiUrl:https://api-m.paypal.com/v2/payments/captures/{capture_id}/refund}")
          String refundApiUrl,
      @Value("${paypal.tokenApiUrl:https://api-m.sandbox.paypal.com/v2/checkout/orders}")
          String tokenApiUrl,
      @Value("${paypal.clientId:}") String clientId,
      @Value("${paypal.clientSecret:}") String clientSecret,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.orderApiUrl = orderApiUrl;
    this.refundApiUrl = refundApiUrl;
    this.tokenApiUrl = tokenApiUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getGatewayName() {
    return "PayPal";
  }

  @Override
  public boolean supports(PaymentMethodType type) {
    return type == PaymentMethodType.PAYPAL
        || type == PaymentMethodType.CREDIT_CARD
        || type == PaymentMethodType.DEBIT_CARD;
  }

  @Override
  public PaymentResponseDto initiatePayment(
      PaymentRequestDto request, PaymentTransactionDto transaction) {
    try {
      log.info("Initiate Payment Transaction: {}", objectMapper.writeValueAsString(transaction));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    PayPalOrderRequest externalRequest = convertToPayPalOrderRequest(request, transaction);
    log.info("Sending payment request to PayPal: {}", externalRequest);
    PayPalOrderResponse externalResponse =
        restTemplate.postForObject(orderApiUrl, externalRequest, PayPalOrderResponse.class);
      assert externalResponse != null;
      log.info("Received response from PayPal: {}", externalResponse);

    return convertToPaymentResponse(externalResponse, request, transaction);
  }

  @Override
  public PaymentResponseDto processRefund(
      PaymentTransactionDto transaction, BigDecimal refundAmount) {
    // Placeholder refund flow
    PayPalRefundRequest refundRequest = convertToRefundRequest(transaction, refundAmount);
    // Expand capture_id from transaction external ID when present
    PayPalRefundResponse refundResponse =
        restTemplate.postForObject(
            refundApiUrl,
            refundRequest,
            PayPalRefundResponse.class,
            transaction.getExternalTransactionId());
    return convertRefundToPaymentResponse(refundResponse, transaction);
  }

  @Override
  public PaymentResponseDto tokenizeCard(Object cardDetails) {
    throw new UnsupportedOperationException("Tokenization not supported for PayPal");
  }

  private PayPalOrderRequest convertToPayPalOrderRequest(
      PaymentRequestDto request, PaymentTransactionDto transaction) {
    PayPalOrderRequest orderRequest = new PayPalOrderRequest();
    orderRequest.setIntent("CAPTURE");

    // Create purchase unit
    PayPalPurchaseUnit purchaseUnit = new PayPalPurchaseUnit();
    purchaseUnit.setReferenceId(request.getId().toString());

    // Set amount
    PayPalAmount amount = new PayPalAmount();
    amount.setCurrencyCode(transaction.getCurrency());
    amount.setValue(transaction.getAmount().toPlainString());
    purchaseUnit.setAmount(amount);

    orderRequest.setPurchaseUnits(java.util.Collections.singletonList(purchaseUnit));

    return orderRequest;
  }

  private PaymentResponseDto convertToPaymentResponse(
      PayPalOrderResponse externalResponse,
      PaymentRequestDto request,
      PaymentTransactionDto transaction) {
    PaymentResponseDto resp = new PaymentResponseDto();

    if (externalResponse != null && externalResponse.getId() != null) {
      resp.setSuccess(true);
      resp.setStatus("CREATED");
      resp.setMessage("PayPal order created successfully");
      resp.setExternalTransactionId(externalResponse.getId());
    } else {
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("Failed to create PayPal order");
    }

    resp.setGatewayName("PayPal");
    resp.setExternalRefundId(null);
    resp.setPaymentRequestId(request.getId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setAmount(transaction.getAmount());
    resp.setCurrency(transaction.getCurrency());
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null);
    resp.setMetadata(request.getMetadata());

    log.info(
        "PayPal order response mapped - ID: {}, Status: {}",
        resp.getExternalTransactionId(),
        resp.getStatus());

    return resp;
  }

  private PayPalRefundRequest convertToRefundRequest(
      PaymentTransactionDto transaction, BigDecimal refundAmount) {
    PayPalRefundRequest refundRequest = new PayPalRefundRequest();

    PayPalAmount amount = new PayPalAmount();
    amount.setCurrencyCode(transaction.getCurrency());
    amount.setValue(refundAmount.toPlainString());
    refundRequest.setAmount(amount);

    return refundRequest;
  }

  private PaymentResponseDto convertRefundToPaymentResponse(
      PayPalRefundResponse refundResponse, PaymentTransactionDto transaction) {
    PaymentResponseDto resp = new PaymentResponseDto();

    if (refundResponse != null && refundResponse.getId() != null) {
      resp.setSuccess(true);
      resp.setStatus("REFUNDED");
      resp.setMessage("PayPal refund processed successfully");
      resp.setExternalRefundId(refundResponse.getId());
    } else {
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("Failed to process PayPal refund");
    }

    resp.setGatewayName("PayPal");
    resp.setExternalTransactionId(transaction.getExternalTransactionId());
    resp.setPaymentRequestId(transaction.getPaymentRequestId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setAmount(transaction.getAmount());
    resp.setCurrency(transaction.getCurrency());
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null);

    return resp;
  }

  // External request/response classes with Jackson annotations for snake_case
  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayPalOrderRequest {
    private String intent;

    @com.fasterxml.jackson.annotation.JsonProperty("purchase_units")
    private java.util.List<PayPalPurchaseUnit> purchaseUnits;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayPalPurchaseUnit {
    @com.fasterxml.jackson.annotation.JsonProperty("reference_id")
    private String referenceId;

    private PayPalAmount amount;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayPalAmount {
    @com.fasterxml.jackson.annotation.JsonProperty("currency_code")
    private String currencyCode;

    private String value;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayPalOrderResponse {
    private String id;
    private String status;
    private String intent;

    @com.fasterxml.jackson.annotation.JsonProperty("purchase_units")
    private java.util.List<PayPalPurchaseUnit> purchaseUnits;

    @com.fasterxml.jackson.annotation.JsonProperty("create_time")
    private String createTime;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayPalRefundRequest {
    private PayPalAmount amount;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PayPalRefundResponse {
    private String id;
    private String status;
    private PayPalAmount amount;
  }
}
