package com.ahss.enums;

/**
 * Enum representing different payment method types supported by the system
 */
public enum PaymentMethodType {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    DIGITAL_WALLET("Digital Wallet"),
    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    MANUAL("Manual Payment");

    private final String displayName;

    PaymentMethodType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the payment method requires online processing
     */
    public boolean requiresOnlineProcessing() {
        return this == CREDIT_CARD || this == DEBIT_CARD || 
               this == DIGITAL_WALLET || this == PAYPAL || this == STRIPE;
    }

    /**
     * Check if the payment method supports instant processing
     */
    public boolean supportsInstantProcessing() {
        return this == CREDIT_CARD || this == DEBIT_CARD || 
               this == DIGITAL_WALLET || this == PAYPAL || this == STRIPE;
    }

    /**
     * Check if the payment method requires manual verification
     */
    public boolean requiresManualVerification() {
        return this == BANK_TRANSFER || this == MANUAL;
    }
}