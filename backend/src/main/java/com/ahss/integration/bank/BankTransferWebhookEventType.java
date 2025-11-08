package com.ahss.integration.bank;

import com.ahss.kafka.event.PaymentCallbackType;

/**
 * Canonical Bank Transfer webhook event types.
 * Provides mapping to internal {@link PaymentCallbackType}.
 */
public enum BankTransferWebhookEventType {
    TRANSFER_INITIATED("TRANSFER.INITIATED"),
    TRANSFER_COMPLETED("TRANSFER.COMPLETED"),
    TRANSFER_FAILED("TRANSFER.FAILED"),
    TRANSFER_REFUND_COMPLETED("TRANSFER.REFUND.COMPLETED"),
    TRANSFER_REFUND_FAILED("TRANSFER.REFUND.FAILED");

    private final String value;

    BankTransferWebhookEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BankTransferWebhookEventType fromValue(String value) {
        if (value == null) return null;
        for (BankTransferWebhookEventType t : values()) {
            if (t.value.equals(value)) return t;
        }
        return null;
    }

    public PaymentCallbackType toCallbackType() {
        return switch (this) {
            case TRANSFER_INITIATED, TRANSFER_COMPLETED -> PaymentCallbackType.PAYMENT_SUCCESS;
            case TRANSFER_FAILED -> PaymentCallbackType.PAYMENT_FAILED;
            case TRANSFER_REFUND_COMPLETED -> PaymentCallbackType.REFUND_SUCCESS;
            case TRANSFER_REFUND_FAILED -> PaymentCallbackType.REFUND_FAILED;
        };
    }
}