package com.ahss.integration.bank;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.integration.PaymentIntegrator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // Assuming RestTemplate for HTTP requests
import java.math.BigDecimal;

/**
 * Implementation of PaymentIntegrator for Bank Transfer channel.
 * Handles conversion of internal payment format to bank transfer specific format,
 * assembles the request, and sends it to the bank transfer API.
 */
@Component
public class BankTransferIntegrator implements PaymentIntegrator {

    private final RestTemplate restTemplate;
    private final String bankTransferApiUrl; // Configurable for tests

    @org.springframework.beans.factory.annotation.Autowired
    public BankTransferIntegrator(
            RestTemplate restTemplate,
            @Value("${bank.paymentApiUrl:https://api.banktransfer.example.com/v1/transfers}") String bankTransferApiUrl
    ) {
        this.restTemplate = restTemplate;
        this.bankTransferApiUrl = bankTransferApiUrl;
    }

    // Backwards-compatible constructor for existing unit tests
    public BankTransferIntegrator(RestTemplate restTemplate) {
        this(restTemplate, "https://api.banktransfer.example.com/v1/transfers");
    }

    @Override
    public boolean supports(PaymentMethodType type) {
        return type == PaymentMethodType.BANK_TRANSFER;
    }

    @Override
    public PaymentResponseDto initiatePayment(PaymentRequestDto request, PaymentTransactionDto transaction) {
        // Convert internal format to external bank transfer format
        BankTransferRequest externalRequest = convertToBankTransferRequest(request, transaction);

        // Send HTTP request to bank transfer API
        BankTransferResponse externalResponse = restTemplate.postForObject(bankTransferApiUrl, externalRequest, BankTransferResponse.class);

        // Convert external response to internal PaymentResponseDto
        return convertToPaymentResponse(externalResponse, request, transaction);
    }

    @Override
    public PaymentResponseDto processRefund(PaymentTransactionDto transaction, BigDecimal refundAmount) {
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
    private BankTransferRequest convertToBankTransferRequest(PaymentRequestDto request, PaymentTransactionDto transaction) {
        // Implementation for conversion
        return new BankTransferRequest(); // Placeholder
    }

    private PaymentResponseDto convertToPaymentResponse(BankTransferResponse externalResponse, PaymentRequestDto request, PaymentTransactionDto transaction) {
        PaymentResponseDto resp = new PaymentResponseDto();
        resp.setSuccess(true); // Placeholder until actual response mapping
        // Use canonical bank transfer event type for status
        resp.setStatus(BankTransferWebhookEventType.TRANSFER_INITIATED.getValue());
        resp.setMessage("Bank transfer initiated");
        resp.setGatewayName("BankTransfer");
        // External IDs would come from externalResponse once defined
        resp.setExternalTransactionId(null);
        resp.setExternalRefundId(null);
        resp.setPaymentRequestId(request.getId());
        resp.setPaymentTransactionId(transaction.getId());
        resp.setAmount(transaction.getAmount());
        resp.setCurrency(transaction.getCurrency());
        resp.setProcessedAt(java.time.LocalDateTime.now());
        resp.setGatewayResponse(null); // Fill with mapped details from externalResponse when available
        resp.setMetadata(request.getMetadata());
        return resp;
    }

    // Define inner classes for external request/response if needed
    private static class BankTransferRequest {
        // Fields for bank transfer request
    }

    private static class BankTransferResponse {
        // Fields for bank transfer response
    }
}