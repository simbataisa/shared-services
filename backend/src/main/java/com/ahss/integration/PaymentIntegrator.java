package com.ahss.integration;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;

/**
 * Generic interface for payment integrators handling different payment channels.
 * Implementations will convert internal payment formats to channel-specific formats,
 * assemble requests, and send them to the external payment providers.
 */
public interface PaymentIntegrator {

    /**
     * Gets the gateway name for this integrator.
     *
     * @return the gateway name (e.g., "Stripe", "PayPal", "BankTransfer")
     */
    String getGatewayName();

    /**
     * Checks if this integrator supports the given payment method type.
     *
     * @param type the payment method type
     * @return true if supported, false otherwise
     */
    boolean supports(PaymentMethodType type);

    /**
     * Initiates a payment request for the given payment details.
     *
     * @param request the payment request DTO
     * @param transaction the payment transaction DTO
     * @return the payment response from the channel
     */
    PaymentResponseDto initiatePayment(PaymentRequestDto request, PaymentTransactionDto transaction);

    /**
     * Processes a refund for a completed payment.
     *
     * @param transaction the original payment transaction
     * @param refundAmount the amount to refund
     * @return the refund response
     */
    PaymentResponseDto processRefund(PaymentTransactionDto transaction, java.math.BigDecimal refundAmount);

    /**
     * Tokenizes credit card information for secure storage and future use.
     *
     * @param cardDetails the card information to tokenize
     * @return the tokenization response
     */
    PaymentResponseDto tokenizeCard(Object cardDetails); // Adjust Object to specific DTO if needed

    // Additional methods for void, cancel, etc., can be added as per PRD requirements
}