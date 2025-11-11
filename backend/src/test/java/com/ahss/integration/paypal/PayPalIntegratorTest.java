package com.ahss.integration.paypal;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Slf4j
@Epic("Payment Channel Integration")
@Feature("PayPal Integration")
class PayPalIntegratorTest {
  private PayPalIntegrator integrator;
  private RestTemplate restTemplate;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    restTemplate = mock(RestTemplate.class);
    objectMapper.registerModule(new JavaTimeModule());
    integrator =
        new PayPalIntegrator(
            restTemplate,
            "orderApiUrl",
            "refundApiUrl",
            "tokenApiUrl",
            "clientId",
            "clientSecret",
            objectMapper);
  }

  @Test
  @DisplayName("supports() returns true for supported payment methods")
  @Story("Supports supported payment methods")
  void supports_onlyPayPal() {
    assertTrue(integrator.supports(PaymentMethodType.PAYPAL));
    assertTrue(integrator.supports(PaymentMethodType.CREDIT_CARD));
    assertTrue(integrator.supports(PaymentMethodType.DEBIT_CARD));
    assertFalse(integrator.supports(PaymentMethodType.BANK_TRANSFER));
  }

  @Test
  @DisplayName("initiatePayment() returns created response for valid request")
  @Story("Initiates payment for valid request")
  void initiatePayment_returnsCreatedResponse() throws JsonProcessingException {

    PaymentRequestDto request = new PaymentRequestDto();
    request.setId(UUID.randomUUID());
    request.setAmount(new BigDecimal("42.00"));
    request.setCurrency("USD");
    request.setMetadata(Map.of("order", "A1"));

    PaymentTransactionDto tx = new PaymentTransactionDto();
    tx.setId(UUID.randomUUID());
    tx.setAmount(new BigDecimal("42.00"));
    tx.setCurrency("USD");

    PayPalIntegrator.PayPalOrderResponse payPalOrderResponse =
        new PayPalIntegrator.PayPalOrderResponse();
    payPalOrderResponse.setId("paypal-order-001");
    payPalOrderResponse.setStatus("CREATED");
    when(restTemplate.postForObject(anyString(), any(), any()))
        .thenReturn(payPalOrderResponse)
        .thenReturn(payPalOrderResponse);
    PaymentResponseDto resp = integrator.initiatePayment(request, tx);
    log.info(objectMapper.writeValueAsString(resp));
    assertTrue(resp.isSuccess());
    assertEquals("CREATED", resp.getStatus());
    assertEquals("PayPal order created successfully", resp.getMessage());
    assertEquals("PayPal", resp.getGatewayName());
    assertEquals(request.getId(), resp.getPaymentRequestId());
    assertEquals(tx.getId(), resp.getPaymentTransactionId());
    assertEquals(tx.getAmount(), resp.getAmount());
    assertEquals(tx.getCurrency(), resp.getCurrency());
    assertEquals(request.getMetadata(), resp.getMetadata());
    assertNotNull(resp.getProcessedAt());
    verify(restTemplate, times(1)).postForObject(anyString(), any(), any());
  }

  @Test
  @DisplayName("processRefund() returns refunded response for valid request")
  @Story("Processes refund for valid request")
  void processRefund_returnsRefundedResponse() {
    PaymentTransactionDto tx = new PaymentTransactionDto();
    tx.setId(UUID.randomUUID());
    tx.setPaymentRequestId(UUID.randomUUID());
    tx.setExternalTransactionId("ext-paypal-001");
    tx.setAmount(new BigDecimal("42.00"));
    tx.setCurrency("USD");

    BigDecimal refundAmount = new BigDecimal("10.00");
    PayPalIntegrator.PayPalRefundResponse payPalRefundResponse =
        new PayPalIntegrator.PayPalRefundResponse();
    payPalRefundResponse.setId("paypal-refund-001");
    payPalRefundResponse.setStatus("COMPLETED"); // PayPal uses COMPLETED for successful refunds
    // Set amount as PayPalAmount object
    PayPalIntegrator.PayPalAmount amount = new PayPalIntegrator.PayPalAmount();
    amount.setCurrencyCode("USD");
    amount.setValue(refundAmount.toPlainString());
    payPalRefundResponse.setAmount(amount);

    when(restTemplate.postForObject(
            anyString(),
            any(),
            ArgumentMatchers.<Class<PayPalIntegrator.PayPalRefundResponse>>any()))
        .thenReturn(payPalRefundResponse);

    PaymentResponseDto resp = integrator.processRefund(tx, refundAmount);

    assertTrue(resp.isSuccess());
    assertEquals("COMPLETED", resp.getStatus()); // Status is returned as-is from PayPal
    assertEquals("PayPal refund processed successfully", resp.getMessage());
    assertEquals("PayPal", resp.getGatewayName());
    assertEquals(tx.getExternalTransactionId(), resp.getExternalTransactionId());
    assertEquals(tx.getPaymentRequestId(), resp.getPaymentRequestId());
    assertEquals(tx.getId(), resp.getPaymentTransactionId());
    assertEquals(refundAmount, resp.getAmount()); // Response should contain the refund amount
    assertEquals(tx.getCurrency(), resp.getCurrency());
    assertNotNull(resp.getProcessedAt());
    // Verify the postForObject method was called
    verify(restTemplate, times(1))
        .postForObject(anyString(), any(), ArgumentMatchers.<Class<?>>any());
  }

  @Test
  @DisplayName("tokenizeCard() throws UnsupportedOperationException")
  @Story("Tokenizes card for valid request")
  void tokenizeCard_throwsUnsupportedOperation() {
    assertThrows(UnsupportedOperationException.class, () -> integrator.tokenizeCard(new Object()));
  }
}
