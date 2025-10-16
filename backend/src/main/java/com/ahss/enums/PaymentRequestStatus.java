package com.ahss.enums;

/**
 * Enum representing the status of a payment request throughout its lifecycle
 */
public enum PaymentRequestStatus {
    DRAFT("Initial state, not yet active"),
    PENDING("Active, awaiting payment"),
    PROCESSING("Payment received, being processed"),
    COMPLETED("Successfully completed"),
    FAILED("Payment failed"),
    CANCELLED("Cancelled by user/admin before payment"),
    VOIDED("Voided after successful payment"),
    REFUNDED("Full refund processed"),
    PARTIAL_REFUND("Partial refund processed"),
    APPROVED("Payment request has been approved"),
    REJECTED("Payment request has been rejected");

    private final String description;

    PaymentRequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the status allows for payment processing
     */
    public boolean canAcceptPayment() {
        return this == PENDING;
    }

    /**
     * Check if the status allows for cancellation
     */
    public boolean canBeCancelled() {
        return this == DRAFT || this == PENDING;
    }

    /**
     * Check if the status allows for voiding
     */
    public boolean canBeVoided() {
        return this == COMPLETED;
    }

    /**
     * Check if the status allows for refunding
     */
    public boolean canBeRefunded() {
        return this == COMPLETED || this == PARTIAL_REFUND;
    }

    /**
     * Check if the payment request is in a final state
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || 
               this == VOIDED || this == REFUNDED || this == REJECTED;
    }
}