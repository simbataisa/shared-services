package com.ahss.integration.bank;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Epic("Payment Channel Integration")
@Feature("Bank Transfer Integration")
class BankTransferIntegratorTest {

  private BankTransferIntegrator integrator;
  private final String transferApiUrl = "https://api.banktransfer.example.com/v1/transfers";
  private final String verifyApiUrl = "https://api.banktransfer.example.com/v1/transfers/verify";
  private final String refundApiUrl = "https://api.banktransfer.example.com/v1/transfers/refund";
  private final String apiKey = "testApiKey";

  private RestTemplate rt;

  @BeforeEach
  void setUp() {
    rt = mock(RestTemplate.class);
    integrator = new BankTransferIntegrator(rt, transferApiUrl, verifyApiUrl, refundApiUrl, apiKey);
  }

  @Test
  @DisplayName("supports() returns true for BANK_TRANSFER and false for other methods")
  @Story("Supports BANK_TRANSFER and false for other methods")
  void supports_onlyBankTransfer() {
    assertTrue(integrator.supports(PaymentMethodType.BANK_TRANSFER));
    assertFalse(integrator.supports(PaymentMethodType.PAYPAL));
    assertFalse(integrator.supports(PaymentMethodType.CREDIT_CARD));
  }

  @Test
  @DisplayName("initiatePayment() returns initiated response for valid request")
  @Story("Initiate Payment for BANK_TRANSFER")
  void initiatePayment_returnsInitiatedResponse() {

    PaymentRequestDto request = new PaymentRequestDto();
    request.setId(UUID.randomUUID());
    request.setAmount(new BigDecimal("55.00"));
    request.setCurrency("USD");

    PaymentTransactionDto tx = new PaymentTransactionDto();
    tx.setId(UUID.randomUUID());
    tx.setAmount(new BigDecimal("55.00"));
    tx.setCurrency("USD");

    BankTransferIntegrator.BankTransferResponse bankTransferResponse =
        new BankTransferIntegrator.BankTransferResponse();
    bankTransferResponse.setSuccess(true);
    bankTransferResponse.setStatus(BankTransferWebhookEventType.TRANSFER_INITIATED.getValue());
    bankTransferResponse.setMessage("Bank transfer initiated");
    bankTransferResponse.setExternalTransactionId("ext-bank-001");

    when(rt.postForObject(anyString(), any(), any())).thenReturn(bankTransferResponse);

    PaymentResponseDto resp = integrator.initiatePayment(request, tx);
    assertTrue(resp.isSuccess());
    assertEquals(BankTransferWebhookEventType.TRANSFER_INITIATED.getValue(), resp.getStatus());
    assertEquals("Bank transfer initiated", resp.getMessage());
    assertEquals("BankTransfer", resp.getGatewayName());
    assertEquals(request.getId(), resp.getPaymentRequestId());
    assertEquals(tx.getId(), resp.getPaymentTransactionId());
    assertEquals(tx.getAmount(), resp.getAmount());
    assertEquals(tx.getCurrency(), resp.getCurrency());
    assertNotNull(resp.getProcessedAt());
    verify(rt, times(1)).postForObject(anyString(), any(), any());
  }

  @Test
  @DisplayName("processRefund() returns refund completed response for valid request")
  @Story("Process Refund for BANK_TRANSFER")
  void processRefund_returnsRefundCompletedResponse() {
    PaymentTransactionDto tx = new PaymentTransactionDto();
    tx.setId(UUID.randomUUID());
    tx.setPaymentRequestId(UUID.randomUUID());
    tx.setExternalTransactionId("ext-bank-001");
    tx.setAmount(new BigDecimal("55.00"));
    tx.setCurrency("USD");
    tx.setMetadata(Map.of("note", "refund"));

    BankTransferIntegrator.BankTransferRefundResponse bankTransferRefundResponse =
        new BankTransferIntegrator.BankTransferRefundResponse();
    bankTransferRefundResponse.setSuccess(true);
    bankTransferRefundResponse.setStatus(
        BankTransferWebhookEventType.TRANSFER_REFUND_COMPLETED.getValue());
    bankTransferRefundResponse.setMessage("Bank transfer refund processed");
    bankTransferRefundResponse.setExternalRefundId("ext-bank-001");

    when(rt.postForObject(anyString(), any(), any())).thenReturn(bankTransferRefundResponse);

    BigDecimal refundAmount = new BigDecimal("20.00");
    when(rt.postForObject(anyString(), any(), any())).thenReturn(bankTransferRefundResponse);
    PaymentResponseDto resp = integrator.processRefund(tx, refundAmount);

    assertTrue(resp.isSuccess());
    assertEquals(
        BankTransferWebhookEventType.TRANSFER_REFUND_COMPLETED.getValue(), resp.getStatus());
    assertEquals("Bank transfer refund processed", resp.getMessage());
    assertEquals("BankTransfer", resp.getGatewayName());
    assertEquals(tx.getExternalTransactionId(), resp.getExternalTransactionId());
    assertEquals(tx.getPaymentRequestId(), resp.getPaymentRequestId());
    assertEquals(tx.getId(), resp.getPaymentTransactionId());
    assertEquals(refundAmount, resp.getAmount());
    assertEquals(tx.getCurrency(), resp.getCurrency());
    assertEquals(tx.getMetadata(), resp.getMetadata());
    assertNotNull(resp.getProcessedAt());
  }

  @Test
  @DisplayName("tokenizeCard() throws UnsupportedOperationException")
  @Story("Tokenize Card for BANK_TRANSFER")
  void tokenizeCard_throwsUnsupportedOperation() {
    assertThrows(UnsupportedOperationException.class, () -> integrator.tokenizeCard(new Object()));
  }
}
