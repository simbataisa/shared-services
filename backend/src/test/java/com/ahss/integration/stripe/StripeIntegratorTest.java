package com.ahss.integration.stripe;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Payment Channel Integration")
@Feature("Stripe Integration")
class StripeIntegratorTest {

    @Test
    @DisplayName("supports() returns true for supported payment methods")
    @Story("Supports supported payment methods")
    void supports_correctPaymentMethods() {
        RestTemplate rt = mock(RestTemplate.class);
        StripeIntegrator integrator = new StripeIntegrator(rt);
        assertTrue(integrator.supports(PaymentMethodType.CREDIT_CARD));
        assertTrue(integrator.supports(PaymentMethodType.DEBIT_CARD));
        assertFalse(integrator.supports(PaymentMethodType.PAYPAL));
        assertFalse(integrator.supports(PaymentMethodType.BANK_TRANSFER));
    }

    @Test
    @DisplayName("initiatePayment() returns authorized response for valid request")
    @Story("Initiates payment for valid request")
    void initiatePayment_returnsAuthorizedResponse() {
        RestTemplate rt = mock(RestTemplate.class);
        StripeIntegrator integrator = new StripeIntegrator(rt);

        PaymentRequestDto request = new PaymentRequestDto();
        request.setId(UUID.randomUUID());
        request.setAmount(new BigDecimal("25.00"));
        request.setCurrency("USD");

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(UUID.randomUUID());
        tx.setAmount(new BigDecimal("25.00"));
        tx.setCurrency("USD");

        PaymentResponseDto resp = integrator.initiatePayment(request, tx);

        assertTrue(resp.isSuccess());
        assertEquals("AUTHORIZED", resp.getStatus());
        assertEquals("Credit card payment authorized", resp.getMessage());
        assertEquals("Stripe", resp.getGatewayName());
        assertEquals(request.getId(), resp.getPaymentRequestId());
        assertEquals(tx.getId(), resp.getPaymentTransactionId());
        assertEquals(tx.getAmount(), resp.getAmount());
        assertEquals(tx.getCurrency(), resp.getCurrency());
        assertNotNull(resp.getProcessedAt());
        verify(rt, times(1)).postForObject(anyString(), any(), any());
    }

    @Test
    @DisplayName("tokenizeCard() returns tokenized response for valid request")
    @Story("Tokenizes card for valid request")
    void tokenizeCard_returnsTokenizedResponse() {
        RestTemplate rt = mock(RestTemplate.class);
        StripeIntegrator integrator = new StripeIntegrator(rt);

        PaymentResponseDto resp = integrator.tokenizeCard(new Object());
        assertTrue(resp.isSuccess());
        assertEquals("TOKENIZED", resp.getStatus());
        assertEquals("Stripe", resp.getGatewayName());
        assertNotNull(resp.getProcessedAt());
        verify(rt, times(1)).postForObject(anyString(), any(), any());
    }
}