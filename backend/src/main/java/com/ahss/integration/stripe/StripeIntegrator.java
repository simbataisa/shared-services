package com.ahss.integration.stripe;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.integration.PaymentIntegrator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // Assuming RestTemplate for HTTP requests
import java.math.BigDecimal;

/**
 * Implementation of PaymentIntegrator for Credit Card channel with tokenization support. Handles
 * conversion of internal payment format to credit card specific format, assembles the request,
 * tokenizes card details, and sends it to the credit card API (e.g., Stripe).
 */
@Component
public class StripeIntegrator implements PaymentIntegrator {

  private static final Logger log = LoggerFactory.getLogger(StripeIntegrator.class);

  private final RestTemplate restTemplate;
  private final String tokenizationApiUrl; // Configurable for tests
  private final String paymentApiUrl; // Configurable for tests
  private final String refundApiUrl;
  private final String apiKey; // Stripe API Key
  private ObjectMapper objectMapper;

  @org.springframework.beans.factory.annotation.Autowired
  public StripeIntegrator(
      RestTemplate restTemplate,
      @Value("${stripe.tokenizationApiUrl:https://api.stripe.com/v1/tokens}")
          String tokenizationApiUrl,
      @Value("${stripe.paymentApiUrl:https://api.stripe.com/v1/charges}") String paymentApiUrl,
      @Value("${stripe.refundApiUrl:https://api.stripe.com/v1/refunds}") String refundApiUrl,
      @Value("${stripe.apiKey:}") String apiKey,
      ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.tokenizationApiUrl = tokenizationApiUrl;
    this.paymentApiUrl = paymentApiUrl;
    this.refundApiUrl = refundApiUrl;
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getGatewayName() {
    return "Stripe";
  }

  @Override
  public boolean supports(PaymentMethodType type) {
    return type == PaymentMethodType.STRIPE
        || type == PaymentMethodType.CREDIT_CARD
        || type == PaymentMethodType.DEBIT_CARD;
  }

  @Override
  public PaymentResponseDto initiatePayment(
      PaymentRequestDto request, PaymentTransactionDto transaction) {
    try {
      log.info(
          "Initiating payment for transaction: {}", objectMapper.writeValueAsString(transaction));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    // Assuming token is already available or tokenize first
    CreditCardPaymentRequest externalRequest = convertToCreditCardRequest(request, transaction);
    log.info("Sending payment request to Stripe: {}", externalRequest);

    // Create headers with authorization
    HttpHeaders headers = createAuthHeaders();
    HttpEntity<CreditCardPaymentRequest> requestEntity = new HttpEntity<>(externalRequest, headers);

    // Send HTTP request to payment API
    CreditCardResponse externalResponse =
        restTemplate.postForObject(paymentApiUrl, requestEntity, CreditCardResponse.class);
    log.info("Received response from Stripe: {}", externalResponse);

    // Convert external response to internal PaymentResponseDto
    return convertToPaymentResponse(externalResponse, request, transaction);
  }

  @Override
  public PaymentResponseDto processRefund(
      PaymentTransactionDto transaction, BigDecimal refundAmount) {
    log.info("Processing refund for transaction: {} with amount: {}",
        transaction.getId(), refundAmount);

    // Build refund request
    StripeRefundRequest refundRequest = new StripeRefundRequest();
    refundRequest.setCharge(transaction.getExternalTransactionId());
    refundRequest.setAmount(refundAmount);
    refundRequest.setCurrency(transaction.getCurrency());
    refundRequest.setReason("requested_by_customer");

    log.info("Sending refund request to Stripe: {}", refundRequest);

    // Create headers with authorization
    HttpHeaders headers = createAuthHeaders();
    HttpEntity<StripeRefundRequest> requestEntity = new HttpEntity<>(refundRequest, headers);

    try {
      // Send HTTP request to refund API
      StripeRefundResponse externalResponse =
          restTemplate.postForObject(refundApiUrl, requestEntity, StripeRefundResponse.class);
      log.info("Received refund response from Stripe: {}", externalResponse);

      // Convert external response to internal PaymentResponseDto
      return convertRefundToPaymentResponse(externalResponse, transaction, refundAmount);
    } catch (Exception e) {
      log.error("Error processing Stripe refund: {}", e.getMessage(), e);
      PaymentResponseDto errorResponse = new PaymentResponseDto();
      errorResponse.setSuccess(false);
      errorResponse.setStatus("FAILED");
      errorResponse.setMessage("Refund failed: " + e.getMessage());
      errorResponse.setGatewayName("Stripe");
      errorResponse.setProcessedAt(java.time.LocalDateTime.now());
      return errorResponse;
    }
  }

  @Override
  public PaymentResponseDto tokenizeCard(Object cardDetails) {
    // Convert card details to external format
    CreditCardTokenRequest tokenRequest = convertToTokenRequest(cardDetails);

    // Create headers with authorization
    HttpHeaders headers = createAuthHeaders();
    HttpEntity<CreditCardTokenRequest> requestEntity = new HttpEntity<>(tokenRequest, headers);

    // Send HTTP request to tokenization API
    CreditCardTokenResponse tokenResponse =
        restTemplate.postForObject(
            tokenizationApiUrl, requestEntity, CreditCardTokenResponse.class);

    // Convert to PaymentResponseDto
    return convertTokenToPaymentResponse(tokenResponse);
  }

  // Helper methods
  private HttpHeaders createAuthHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (apiKey != null && !apiKey.isEmpty()) {
      headers.set("Authorization", "Bearer " + apiKey);
    }
    return headers;
  }

  private CreditCardPaymentRequest convertToCreditCardRequest(
      PaymentRequestDto request, PaymentTransactionDto transaction) {
    CreditCardPaymentRequest ccRequest = new CreditCardPaymentRequest();
    ccRequest.setAmount(transaction.getAmount());
    ccRequest.setCurrency(transaction.getCurrency());
    ccRequest.setDescription(request.getTitle());
    ccRequest.setPaymentRequestId(request.getId().toString());
    // Token should be obtained from transaction metadata or payment method details
    // For now, using a placeholder - in production, this would come from tokenization
    ccRequest.setToken("tok_placeholder");
    return ccRequest;
  }

  private CreditCardTokenRequest convertToTokenRequest(Object cardDetails) {
    // Implementation for token request conversion
    return new CreditCardTokenRequest(); // Placeholder
  }

  private PaymentResponseDto convertToPaymentResponse(
      CreditCardResponse externalResponse,
      PaymentRequestDto request,
      PaymentTransactionDto transaction) {
    log.info("Mapping Stripe response to internal PaymentResponseDto: {}", externalResponse);
    PaymentResponseDto resp = new PaymentResponseDto();

    if (externalResponse != null) {
      Boolean isSuccess =
          externalResponse.getSuccess() != null
              ? externalResponse.getSuccess()
              : (externalResponse.getId() != null);
      resp.setSuccess(isSuccess);
      resp.setStatus(
          externalResponse.getStatus() != null ? externalResponse.getStatus() : "AUTHORIZED");
      resp.setMessage(
          isSuccess ? "Credit card payment authorized" : externalResponse.getErrorMessage());
      resp.setExternalTransactionId(externalResponse.getId());
      resp.setAmount(externalResponse.getAmount());
      resp.setCurrency(externalResponse.getCurrency());
    } else {
      // Handle null response (network error, etc.)
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("No response received from payment gateway");
    }

    resp.setGatewayName("Stripe");
    resp.setExternalRefundId(null);
    resp.setPaymentRequestId(request.getId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null); // Could serialize externalResponse to Map if needed
    resp.setMetadata(request.getMetadata());
    return resp;
  }

  private PaymentResponseDto convertTokenToPaymentResponse(CreditCardTokenResponse tokenResponse) {
    PaymentResponseDto resp = new PaymentResponseDto();
    resp.setSuccess(true); // Placeholder
    resp.setStatus("TOKENIZED");
    resp.setMessage("Card tokenized");
    resp.setGatewayName("Stripe");
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null); // Map token details when available
    return resp;
  }

  private PaymentResponseDto convertRefundToPaymentResponse(
      StripeRefundResponse externalResponse,
      PaymentTransactionDto transaction,
      BigDecimal refundAmount) {
    log.info("Mapping Stripe refund response to internal PaymentResponseDto: {}", externalResponse);
    PaymentResponseDto resp = new PaymentResponseDto();

    if (externalResponse != null) {
      Boolean isSuccess =
          externalResponse.getSuccess() != null
              ? externalResponse.getSuccess()
              : (externalResponse.getId() != null && "succeeded".equals(externalResponse.getStatus()));
      resp.setSuccess(isSuccess);
      resp.setStatus(
          externalResponse.getStatus() != null ? externalResponse.getStatus().toUpperCase() : "REFUNDED");
      resp.setMessage(
          isSuccess ? "Refund processed successfully" : "Refund failed");
      resp.setExternalRefundId(externalResponse.getId());
      resp.setAmount(externalResponse.getAmount() != null ? externalResponse.getAmount() : refundAmount);
      resp.setCurrency(externalResponse.getCurrency() != null ? externalResponse.getCurrency() : transaction.getCurrency());
    } else {
      // Handle null response (network error, etc.)
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("No response received from payment gateway");
    }

    resp.setGatewayName("Stripe");
    resp.setExternalTransactionId(transaction.getExternalTransactionId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null); // Could serialize externalResponse to Map if needed
    return resp;
  }

  // Define inner classes for external request/response if needed
  @Data
  @NoArgsConstructor
  static class CreditCardPaymentRequest {
    private String token;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String paymentRequestId;
  }

  @Data
  @NoArgsConstructor
  static class CreditCardResponse {
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Boolean success;
    private String errorMessage;
  }

  @Data
  @NoArgsConstructor
  static class CreditCardTokenRequest {
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String cardHolderName;
  }

  @Data
  @NoArgsConstructor
  static class CreditCardTokenResponse {
    private String token;
    private String tokenType;
    private Boolean success;
    private String errorMessage;
  }

  @Data
  @NoArgsConstructor
  static class StripeRefundRequest {
    private String charge;
    private BigDecimal amount;
    private String currency;
    private String reason;
  }

  @Data
  @NoArgsConstructor
  static class StripeRefundResponse {
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Boolean success;
    private String errorMessage;
  }
}
