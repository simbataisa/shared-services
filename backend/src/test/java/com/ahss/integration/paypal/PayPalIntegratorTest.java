package com.ahss.integration.paypal;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayPalIntegratorTest {

  @Test
  void supports_onlyPayPal() {
    RestTemplate rt = mock(RestTemplate.class);
    PayPalIntegrator integrator = new PayPalIntegrator(rt);
    assertTrue(integrator.supports(PaymentMethodType.PAYPAL));
    assertFalse(integrator.supports(PaymentMethodType.CREDIT_CARD));
    assertFalse(integrator.supports(PaymentMethodType.BANK_TRANSFER));
  }

  @Test
  void initiatePayment_returnsCreatedResponse() {
    RestTemplate rt = mock(RestTemplate.class);
    PayPalIntegrator integrator = new PayPalIntegrator(rt);

    PaymentRequestDto request = new PaymentRequestDto();
    request.setId(UUID.randomUUID());
    request.setAmount(new BigDecimal("42.00"));
    request.setCurrency("USD");
    request.setMetadata(Map.of("order", "A1"));

    PaymentTransactionDto tx = new PaymentTransactionDto();
    tx.setId(UUID.randomUUID());
    tx.setAmount(new BigDecimal("42.00"));
    tx.setCurrency("USD");

    PaymentResponseDto resp = integrator.initiatePayment(request, tx);
    assertTrue(resp.isSuccess());
    assertEquals("CREATED", resp.getStatus());
    assertEquals("PayPal order created", resp.getMessage());
    assertEquals("PayPal", resp.getGatewayName());
    assertEquals(request.getId(), resp.getPaymentRequestId());
    assertEquals(tx.getId(), resp.getPaymentTransactionId());
    assertEquals(tx.getAmount(), resp.getAmount());
    assertEquals(tx.getCurrency(), resp.getCurrency());
    assertEquals(request.getMetadata(), resp.getMetadata());
    assertNotNull(resp.getProcessedAt());
    verify(rt, times(1)).postForObject(anyString(), any(), any());
  }

  @Test
  void processRefund_returnsRefundedResponse() {
    RestTemplate rt = mock(RestTemplate.class);
    PayPalIntegrator integrator = new PayPalIntegrator(rt);

    PaymentTransactionDto tx = new PaymentTransactionDto();
    tx.setId(UUID.randomUUID());
    tx.setPaymentRequestId(UUID.randomUUID());
    tx.setExternalTransactionId("ext-paypal-001");
    tx.setAmount(new BigDecimal("42.00"));
    tx.setCurrency("USD");

    BigDecimal refundAmount = new BigDecimal("10.00");
    PaymentResponseDto resp = integrator.processRefund(tx, refundAmount);

    assertTrue(resp.isSuccess());
    assertEquals("REFUNDED", resp.getStatus());
    assertEquals("PayPal refund processed", resp.getMessage());
    assertEquals("PayPal", resp.getGatewayName());
    assertEquals(tx.getExternalTransactionId(), resp.getExternalTransactionId());
    assertEquals(tx.getPaymentRequestId(), resp.getPaymentRequestId());
    assertEquals(tx.getId(), resp.getPaymentTransactionId());
    assertEquals(tx.getAmount(), resp.getAmount());
    assertEquals(tx.getCurrency(), resp.getCurrency());
    assertNotNull(resp.getProcessedAt());
    // Refund call uses URI template expansion; verify four-arg overload unambiguously
    verify(rt, times(1))
        .postForObject(anyString(), any(), ArgumentMatchers.<Class<?>>any(), anyString());
  }

  @Test
  void tokenizeCard_throwsUnsupportedOperation() {
    RestTemplate rt = mock(RestTemplate.class);
    PayPalIntegrator integrator = new PayPalIntegrator(rt);
    assertThrows(UnsupportedOperationException.class, () -> integrator.tokenizeCard(new Object()));
  }
}
