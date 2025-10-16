package com.ahss.enums;

/**
 * Enum representing different types of payment transactions
 */
public enum PaymentTransactionType {
    PAYMENT("Payment transaction"),
    REFUND("Refund transaction"),
    VOID("Void transaction"),
    CHARGEBACK("Chargeback transaction");

    private final String description;

    PaymentTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the transaction type represents money coming in
     */
    public boolean isIncoming() {
        return this == PAYMENT || this == CHARGEBACK;
    }

    /**
     * Check if the transaction type represents money going out
     */
    public boolean isOutgoing() {
        return this == REFUND || this == VOID;
    }

    /**
     * Check if the transaction type affects the payment request balance
     */
    public boolean affectsBalance() {
        return true; // All transaction types affect balance
    }
}