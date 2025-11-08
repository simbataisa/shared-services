package com.ahss.integration.bank;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankTransferIntegratorTest {

    @Test
    void supports_onlyBankTransfer() {
        RestTemplate rt = mock(RestTemplate.class);
        BankTransferIntegrator integrator = new BankTransferIntegrator(rt);
        assertTrue(integrator.supports(PaymentMethodType.BANK_TRANSFER));
        assertFalse(integrator.supports(PaymentMethodType.PAYPAL));
        assertFalse(integrator.supports(PaymentMethodType.CREDIT_CARD));
    }

    @Test
    void initiatePayment_returnsInitiatedResponse() {
        RestTemplate rt = mock(RestTemplate.class);
        BankTransferIntegrator integrator = new BankTransferIntegrator(rt);

        PaymentRequestDto request = new PaymentRequestDto();
        request.setId(UUID.randomUUID());
        request.setAmount(new BigDecimal("55.00"));
        request.setCurrency("USD");

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(UUID.randomUUID());
        tx.setAmount(new BigDecimal("55.00"));
        tx.setCurrency("USD");

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
    void processRefund_returnsRefundCompletedResponse() {
        RestTemplate rt = mock(RestTemplate.class);
        BankTransferIntegrator integrator = new BankTransferIntegrator(rt);

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(UUID.randomUUID());
        tx.setPaymentRequestId(UUID.randomUUID());
        tx.setExternalTransactionId("ext-bank-001");
        tx.setAmount(new BigDecimal("55.00"));
        tx.setCurrency("USD");
        tx.setMetadata(Map.of("note", "refund"));

        BigDecimal refundAmount = new BigDecimal("20.00");
        PaymentResponseDto resp = integrator.processRefund(tx, refundAmount);

        assertTrue(resp.isSuccess());
        assertEquals(BankTransferWebhookEventType.TRANSFER_REFUND_COMPLETED.getValue(), resp.getStatus());
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
    void tokenizeCard_throwsUnsupportedOperation() {
        RestTemplate rt = mock(RestTemplate.class);
        BankTransferIntegrator integrator = new BankTransferIntegrator(rt);
        assertThrows(UnsupportedOperationException.class, () -> integrator.tokenizeCard(new Object()));
    }
}