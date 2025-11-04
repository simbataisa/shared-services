package com.ahss.enums;

/**
 * Enum representing the status of payment transactions
 */
public enum PaymentTransactionStatus {
    PENDING("Transaction is pending processing"),
    PROCESSING("Transaction is being processed"), // Added PROCESSING status
    SUCCESS("Transaction completed successfully"),
    COMPLETED("Transaction completed successfully"), // Alias for SUCCESS
    FAILED("Transaction failed"),
    CANCELLED("Transaction was cancelled");

    private final String description;

    PaymentTransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the transaction is in a final state
     */
    public boolean isFinalState() {
        return this == SUCCESS || this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * Check if the transaction was successful
     */
    public boolean isSuccessful() {
        return this == SUCCESS || this == COMPLETED;
    }

    /**
     * Check if the transaction can be retried
     */
    public boolean canBeRetried() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * Check if the transaction is still processing
     */
    public boolean isProcessing() {
        return this == PENDING || this == PROCESSING;
    }
}