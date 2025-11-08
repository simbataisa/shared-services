package com.ahss.integration.stripe;

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
 * Implementation of PaymentIntegrator for Credit Card channel with tokenization support.
 * Handles conversion of internal payment format to credit card specific format,
 * assembles the request, tokenizes card details, and sends it to the credit card API (e.g., Stripe).
 */
@Component
public class StripeIntegrator implements PaymentIntegrator {

    private final RestTemplate restTemplate;
    private final String tokenizationApiUrl; // Configurable for tests
    private final String paymentApiUrl; // Configurable for tests

    @org.springframework.beans.factory.annotation.Autowired
    public StripeIntegrator(
            RestTemplate restTemplate,
            @Value("${stripe.tokenizationApiUrl:https://api.stripe.com/v1/tokens}") String tokenizationApiUrl,
            @Value("${stripe.paymentApiUrl:https://api.stripe.com/v1/charges}") String paymentApiUrl
    ) {
        this.restTemplate = restTemplate;
        this.tokenizationApiUrl = tokenizationApiUrl;
        this.paymentApiUrl = paymentApiUrl;
    }

    // Backwards-compatible constructor for existing unit tests
    public StripeIntegrator(RestTemplate restTemplate) {
        this(restTemplate,
                "https://api.stripe.com/v1/tokens",
                "https://api.stripe.com/v1/charges");
    }

    @Override
    public boolean supports(PaymentMethodType type) {
        return type == PaymentMethodType.CREDIT_CARD || type == PaymentMethodType.DEBIT_CARD;
    }

    @Override
    public PaymentResponseDto initiatePayment(PaymentRequestDto request, PaymentTransactionDto transaction) {
        // Assuming token is already available or tokenize first
        CreditCardPaymentRequest externalRequest = convertToCreditCardRequest(request, transaction);

        // Send HTTP request to payment API
        CreditCardResponse externalResponse = restTemplate.postForObject(paymentApiUrl, externalRequest, CreditCardResponse.class);

        // Convert external response to internal PaymentResponseDto
        return convertToPaymentResponse(externalResponse, request, transaction);
    }

    @Override
    public PaymentResponseDto processRefund(PaymentTransactionDto transaction, BigDecimal refundAmount) {
        // Implement refund logic for credit card
        // Similar to initiatePayment but for refunds
        return new PaymentResponseDto(); // Placeholder
    }

    @Override
    public PaymentResponseDto tokenizeCard(Object cardDetails) {
        // Convert card details to external format
        CreditCardTokenRequest tokenRequest = convertToTokenRequest(cardDetails);

        // Send HTTP request to tokenization API
        CreditCardTokenResponse tokenResponse = restTemplate.postForObject(tokenizationApiUrl, tokenRequest, CreditCardTokenResponse.class);

        // Convert to PaymentResponseDto
        return convertTokenToPaymentResponse(tokenResponse);
    }

    // Helper methods
    private CreditCardPaymentRequest convertToCreditCardRequest(PaymentRequestDto request, PaymentTransactionDto transaction) {
        // Implementation for conversion
        return new CreditCardPaymentRequest(); // Placeholder
    }

    private CreditCardTokenRequest convertToTokenRequest(Object cardDetails) {
        // Implementation for token request conversion
        return new CreditCardTokenRequest(); // Placeholder
    }

    private PaymentResponseDto convertToPaymentResponse(CreditCardResponse externalResponse, PaymentRequestDto request, PaymentTransactionDto transaction) {
        PaymentResponseDto resp = new PaymentResponseDto();
        resp.setSuccess(true); // Placeholder until actual response mapping
        resp.setStatus("AUTHORIZED");
        resp.setMessage("Credit card payment authorized");
        resp.setGatewayName("Stripe");
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

    // Define inner classes for external request/response if needed
    private static class CreditCardPaymentRequest {
        // Fields for payment request
    }

    private static class CreditCardResponse {
        // Fields for payment response
    }

    private static class CreditCardTokenRequest {
        // Fields for token request
    }

    private static class CreditCardTokenResponse {
        // Fields for token response
    }
}