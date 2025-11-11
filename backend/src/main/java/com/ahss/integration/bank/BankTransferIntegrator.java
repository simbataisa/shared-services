package com.ahss.integration.bank;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.integration.PaymentIntegrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // Assuming RestTemplate for HTTP requests
import java.math.BigDecimal;

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
  private final String apiKey;// Configurable for tests

  @org.springframework.beans.factory.annotation.Autowired
  public BankTransferIntegrator(
          RestTemplate restTemplate,
          @Value("${bankTransfer.transferApiUrl:https://api.banktransfer.example.com/v1/transfers}")
          String transferApiUrl,
          @Value("${bankTransfer.verifyApiUrl:https://api.banktransfer.example.com/v1/transfers/verify}")
          String verifyApiUrl,
          @Value("${bankTransfer.apiKey:defaultApiKey}") String apiKey) {
    this.restTemplate = restTemplate;
    this.transferApiUrl = transferApiUrl;
      this.verifyApiUrl = verifyApiUrl;
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
    BankTransferResponse externalResponse =
        restTemplate.postForObject(transferApiUrl, externalRequest, BankTransferResponse.class);
    log.info("Bank Transfer response: {}", externalResponse);
    // Convert external response to internal PaymentResponseDto
    return convertToPaymentResponse(externalResponse, request, transaction);
  }

  @Override
  public PaymentResponseDto processRefund(
      PaymentTransactionDto transaction, BigDecimal refundAmount) {
    // Implement refund logic for bank transfer
    // Placeholder response utilizing canonical event type
    PaymentResponseDto resp = new PaymentResponseDto();
    resp.setSuccess(true);
    resp.setStatus(BankTransferWebhookEventType.TRANSFER_REFUND_COMPLETED.getValue());
    resp.setMessage("Bank transfer refund processed");
    resp.setGatewayName("BankTransfer");
    resp.setExternalTransactionId(transaction.getExternalTransactionId());
    resp.setExternalRefundId(null);
    resp.setPaymentRequestId(transaction.getPaymentRequestId());
    resp.setPaymentTransactionId(transaction.getId());
    resp.setAmount(refundAmount);
    resp.setCurrency(transaction.getCurrency());
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null);
    resp.setMetadata(transaction.getMetadata());
    return resp;
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
    resp.setProcessedAt(java.time.LocalDateTime.now());
    resp.setGatewayResponse(null);
    resp.setMetadata(request.getMetadata());

    log.info(
        "Bank transfer response mapped - ID: {}, Status: {}",
        resp.getExternalTransactionId(),
        resp.getStatus());

    return resp;
  }

  // External request/response classes with Lombok annotations
  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  static class BankTransferRequest {
    private BigDecimal amount;
    private String currency;
    private String accountNumber;
    private String routingNumber;
    private String accountHolderName;
    private String bankName;
    private String fromAccount;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  static class BankTransferResponse {
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Boolean success;
    private String externalTransactionId;
    private String message;
  }
}
