package com.ahss.integration.bank;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.integration.PaymentIntegrator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // Assuming RestTemplate for HTTP requests
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implementation of PaymentIntegrator for Bank Transfer channel. Handles conversion of internal
 * payment format to bank transfer specific format, assembles the request, and sends it to the bank
 * transfer API.
 */
@Slf4j
@Component
public class BankTransferIntegrator implements PaymentIntegrator {

  private final RestTemplate restTemplate;
  private final String transferApiUrl;
  private final String verifyApiUrl;
  private final String refundApiUrl;
  private final String apiKey; // Configurable for tests

  @Autowired
  public BankTransferIntegrator(
      RestTemplate restTemplate,
      @Value("${bankTransfer.transferApiUrl:https://api.banktransfer.example.com/v1/transfers}")
          String transferApiUrl,
      @Value(
              "${bankTransfer.verifyApiUrl:https://api.banktransfer.example.com/v1/transfers/verify}")
          String verifyApiUrl,
      @Value(
              "${bankTransfer.refundApiUrl:https://api.banktransfer.example.com/v1/transfers/{id}/refund}")
          String refundApiUrl,
      @Value("${bankTransfer.apiKey:defaultApiKey}") String apiKey) {
    this.restTemplate = restTemplate;
    this.transferApiUrl = transferApiUrl;
    this.verifyApiUrl = verifyApiUrl;
    this.refundApiUrl = refundApiUrl;
    this.apiKey = apiKey;
  }

  @Override
  public String getGatewayName() {
    return "BankTransfer";
  }

  @Override
  public boolean supports(PaymentMethodType type) {
    return type == PaymentMethodType.BANK_TRANSFER;
  }

  @Override
  public PaymentResponseDto initiatePayment(
      PaymentRequestDto request, PaymentTransactionDto transaction) {
    log.info("Initiating Bank Transfer payment for transaction: {}", transaction);
    // Convert internal format to external bank transfer format
    BankTransferRequest externalRequest = convertToBankTransferRequest(request, transaction);
    log.info("Bank Transfer request: {}", externalRequest);
    // Send HTTP request to bank transfer API
    BankTransferResponse externalResponse = null;
    try {
      externalResponse =
          restTemplate.postForObject(transferApiUrl, externalRequest, BankTransferResponse.class);
      log.info("Bank Transfer response: {}", externalResponse);
    } catch (Exception e) {
      log.error("Error occurred while processing Bank Transfer payment: {}", e.getMessage(), e);
    }
    // Convert external response to internal PaymentResponseDto
    return convertToPaymentResponse(externalResponse, request, transaction);
  }

  @Override
  public PaymentResponseDto processRefund(
      PaymentTransactionDto transaction, BigDecimal refundAmount) {
    log.info(
        "Processing refund for transaction: {} with amount: {}", transaction.getId(), refundAmount);

    // Build refund request
    BankTransferRefundRequest refundRequest = new BankTransferRefundRequest();
    refundRequest.setAmount(refundAmount);
    refundRequest.setCurrency(transaction.getCurrency());
    refundRequest.setReason("Customer requested refund");
    refundRequest.setOriginalTransferId(transaction.getExternalTransactionId());

    log.info(
        "Sending refund request to Bank Transfer API for transfer: {}",
        transaction.getExternalTransactionId());

    try {
      // Replace {id} placeholder with actual transaction ID
      String refundUrl = refundApiUrl.replace("{id}", transaction.getExternalTransactionId());

      // Create headers with API key
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      if (apiKey != null && !apiKey.isEmpty()) {
        headers.set("Authorization", "Bearer " + apiKey);
      }

      HttpEntity<BankTransferRefundRequest> requestEntity =
          new HttpEntity<>(refundRequest, headers);

      // Send HTTP request to refund API
      BankTransferRefundResponse externalResponse =
          restTemplate.postForObject(refundUrl, requestEntity, BankTransferRefundResponse.class);
      log.info("Received refund response from Bank Transfer API: {}", externalResponse);

      // Convert external response to internal PaymentResponseDto
      return convertRefundToPaymentResponse(externalResponse, transaction, refundAmount);
    } catch (Exception e) {
      log.error("Error processing Bank Transfer refund: {}", e.getMessage(), e);
      PaymentResponseDto errorResponse = new PaymentResponseDto();
      errorResponse.setSuccess(false);
      errorResponse.setStatus("FAILED");
      errorResponse.setMessage("Refund failed: " + e.getMessage());
      errorResponse.setGatewayName("BankTransfer");
      errorResponse.setExternalTransactionId(transaction.getExternalTransactionId());
      errorResponse.setPaymentTransactionId(transaction.getId());
      errorResponse.setAmount(refundAmount);
      errorResponse.setCurrency(transaction.getCurrency());
      errorResponse.setProcessedAt(LocalDateTime.now());
      return errorResponse;
    }
  }

  @Override
  public PaymentResponseDto tokenizeCard(Object cardDetails) {
    throw new UnsupportedOperationException("Tokenization not supported for Bank Transfer");
  }

  // Helper methods
  private BankTransferRequest convertToBankTransferRequest(
      PaymentRequestDto request, PaymentTransactionDto transaction) {
    BankTransferRequest btRequest = new BankTransferRequest();
    btRequest.setAmount(transaction.getAmount());
    btRequest.setCurrency(transaction.getCurrency());

    // Extract bank details from payment method details if available
    if (transaction.getPaymentMethodDetails() != null) {
      btRequest.setAccountNumber(
          (String) transaction.getPaymentMethodDetails().get("accountNumber"));
      btRequest.setRoutingNumber(
          (String) transaction.getPaymentMethodDetails().get("routingNumber"));
      btRequest.setAccountHolderName(
          (String) transaction.getPaymentMethodDetails().get("accountHolderName"));
      btRequest.setBankName((String) transaction.getPaymentMethodDetails().get("bankName"));
    }

    btRequest.setFromAccount("MERCHANT-ACCOUNT");

    log.info(
        "Created bank transfer request for amount: {} {}",
        transaction.getAmount(),
        transaction.getCurrency());

    return btRequest;
  }

  private PaymentResponseDto convertToPaymentResponse(
      BankTransferResponse externalResponse,
      PaymentRequestDto request,
      PaymentTransactionDto transaction) {
    PaymentResponseDto resp = new PaymentResponseDto();

    if (externalResponse != null
        && externalResponse.getSuccess() != null
        && externalResponse.getSuccess()) {
      resp.setSuccess(true);
      resp.setStatus(
          externalResponse.getStatus() != null ? externalResponse.getStatus() : "PENDING");
      resp.setMessage(
          externalResponse.getMessage() != null
              ? externalResponse.getMessage()
              : "Bank transfer initiated");
      resp.setExternalTransactionId(externalResponse.getExternalTransactionId());
    } else {
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("Failed to initiate bank transfer");
    }

    resp.setGatewayName("BankTransfer");
    resp.setExternalRefundId(null);
    resp.setPaymentRequestId(request.getId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setAmount(transaction.getAmount());
    resp.setCurrency(transaction.getCurrency());
    resp.setProcessedAt(LocalDateTime.now());
    resp.setGatewayResponse(null);
    resp.setMetadata(request.getMetadata());

    log.info(
        "Bank transfer response mapped - ID: {}, Status: {}",
        resp.getExternalTransactionId(),
        resp.getStatus());

    return resp;
  }

  private PaymentResponseDto convertRefundToPaymentResponse(
      BankTransferRefundResponse externalResponse,
      PaymentTransactionDto transaction,
      BigDecimal refundAmount) {
    if (null == externalResponse) {
      throw new IllegalArgumentException("External response is null");
    }
    log.info(
        "Mapping Bank Transfer refund response to internal PaymentResponseDto: {}",
        externalResponse);
    PaymentResponseDto resp = new PaymentResponseDto();

    if (externalResponse.getSuccess() != null && externalResponse.getSuccess()) {
      resp.setSuccess(true);
      resp.setStatus(
          externalResponse.getStatus() != null
              ? externalResponse.getStatus().toUpperCase()
              : "REFUNDED");
      resp.setMessage(
          externalResponse.getMessage() != null
              ? externalResponse.getMessage()
              : "Bank transfer refund processed successfully");
      resp.setExternalRefundId(externalResponse.getId());
      resp.setAmount(
          externalResponse.getAmount() != null ? externalResponse.getAmount() : refundAmount);
      resp.setCurrency(
          externalResponse.getCurrency() != null
              ? externalResponse.getCurrency()
              : transaction.getCurrency());
    } else {
      resp.setSuccess(false);
      resp.setStatus("FAILED");
      resp.setMessage("Failed to process bank transfer refund");
      resp.setAmount(refundAmount);
      resp.setCurrency(transaction.getCurrency());
    }

    resp.setGatewayName("BankTransfer");
    resp.setExternalTransactionId(transaction.getExternalTransactionId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setPaymentRequestId(transaction.getPaymentRequestId());
    resp.setMetadata(transaction.getMetadata());
    resp.setProcessedAt(LocalDateTime.now());
    resp.setGatewayResponse(
        externalResponse.getMessage() != null
            ? Map.of("message", externalResponse.getMessage())
            : null);

    log.info(
        "Bank transfer refund response mapped - ID: {}, Status: {}, Success: {}",
        resp.getExternalRefundId(),
        resp.getStatus(),
        resp.isSuccess());

    return resp;
  }

  // External request/response classes with Lombok annotations
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class BankTransferRequest {
    private BigDecimal amount;
    private String currency;
    private String accountNumber;
    private String routingNumber;
    private String accountHolderName;
    private String bankName;
    private String fromAccount;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class BankTransferResponse {
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Boolean success;
    private String externalTransactionId;
    private String message;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class BankTransferRefundRequest {
    private BigDecimal amount;
    private String currency;
    private String reason;
    private String originalTransferId;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class BankTransferRefundResponse {
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Boolean success;
    private String externalRefundId;
    private String message;
  }
}
