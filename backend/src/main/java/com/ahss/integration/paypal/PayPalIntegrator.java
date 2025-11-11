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
    log.info("Processing refund for transaction: {} with amount: {}",
        transaction.getId(), refundAmount);

    // Build refund request
    PayPalRefundRequest refundRequest = convertToRefundRequest(transaction, refundAmount);
    log.info("Sending refund request to PayPal for capture: {}",
        transaction.getExternalTransactionId());

    try {
      // Expand capture_id from transaction external ID when present
      // PayPal refund endpoint: /v2/payments/captures/{capture_id}/refund
      String refundUrl = refundApiUrl.replace("{capture_id}", transaction.getExternalTransactionId());
      PayPalRefundResponse refundResponse =
          restTemplate.postForObject(
              refundUrl,
              refundRequest,
              PayPalRefundResponse.class);
      log.info("Received refund response from PayPal: {}", refundResponse);

      return convertRefundToPaymentResponse(refundResponse, transaction, refundAmount);
    } catch (Exception e) {
      log.error("Error processing PayPal refund: {}", e.getMessage(), e);
      PaymentResponseDto errorResponse = new PaymentResponseDto();
      errorResponse.setSuccess(false);
      errorResponse.setStatus("FAILED");
      errorResponse.setMessage("Refund failed: " + e.getMessage());
      errorResponse.setGatewayName("PayPal");
      errorResponse.setExternalTransactionId(transaction.getExternalTransactionId());
      errorResponse.setPaymentTransactionId(transaction.getId());
      errorResponse.setProcessedAt(java.time.LocalDateTime.now());
      return errorResponse;
    }
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
      PayPalRefundResponse refundResponse, PaymentTransactionDto transaction, BigDecimal refundAmount) {
    log.info("Mapping PayPal refund response to internal PaymentResponseDto: {}", refundResponse);
    PaymentResponseDto resp = new PaymentResponseDto();

    if (refundResponse != null && refundResponse.getId() != null) {
      String statusUpper = refundResponse.getStatus() != null ? refundResponse.getStatus().toUpperCase() : "REFUNDED";
      boolean isSuccess = "COMPLETED".equalsIgnoreCase(statusUpper) || "REFUNDED".equalsIgnoreCase(statusUpper);
      resp.setSuccess(isSuccess);
      resp.setStatus(statusUpper);
      resp.setMessage(isSuccess ? "PayPal refund processed successfully" : "PayPal refund pending");
      resp.setExternalRefundId(refundResponse.getId());

      // Use amount from response if available, otherwise use requested refundAmount
      if (refundResponse.getAmount() != null && refundResponse.getAmount().getValue() != null) {
        resp.setAmount(new BigDecimal(refundResponse.getAmount().getValue()));
        resp.setCurrency(refundResponse.getAmount().getCurrencyCode());
      } else {
        resp.setAmount(refundAmount);
        resp.setCurrency(transaction.getCurrency());
      }
    } else {
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("Failed to process PayPal refund");
      resp.setAmount(refundAmount);
      resp.setCurrency(transaction.getCurrency());
    }

    resp.setGatewayName("PayPal");
    resp.setExternalTransactionId(transaction.getExternalTransactionId());
    resp.setPaymentRequestId(transaction.getPaymentRequestId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null);

    log.info("PayPal refund response mapped - ID: {}, Status: {}, Success: {}",
        resp.getExternalRefundId(), resp.getStatus(), resp.isSuccess());

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
