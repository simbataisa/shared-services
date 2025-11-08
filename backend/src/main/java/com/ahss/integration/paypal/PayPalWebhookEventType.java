package com.ahss.integration.paypal;

import com.ahss.kafka.event.PaymentCallbackType;

/**
 * Canonical PayPal webhook event types.
 * Provides mapping to internal {@link PaymentCallbackType}.
 */
public enum PayPalWebhookEventType {
    PAYMENT_SALE_COMPLETED("PAYMENT.SALE.COMPLETED"),
    CHECKOUT_ORDER_APPROVED("CHECKOUT.ORDER.APPROVED"),
    PAYMENT_SALE_DENIED("PAYMENT.SALE.DENIED"),
    PAYMENT_CAPTURE_COMPLETED("PAYMENT.CAPTURE.COMPLETED"),
    PAYMENT_CAPTURE_DENIED("PAYMENT.CAPTURE.DENIED"),
    PAYMENT_CAPTURE_FAILED("PAYMENT.CAPTURE.FAILED"),
    PAYMENT_SALE_REFUNDED("PAYMENT.SALE.REFUNDED"),
    PAYMENT_CAPTURE_REFUNDED("PAYMENT.CAPTURE.REFUNDED"),
    PAYMENT_REFUND_COMPLETED("PAYMENT.REFUND.COMPLETED"),
    PAYMENT_REFUND_DENIED("PAYMENT.REFUND.DENIED");

    private final String value;

    PayPalWebhookEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PayPalWebhookEventType fromValue(String value) {
        if (value == null) return null;
        for (PayPalWebhookEventType t : values()) {
            if (t.value.equals(value)) return t;
        }
        return null;
    }

    public PaymentCallbackType toCallbackType() {
        return switch (this) {
            case PAYMENT_SALE_COMPLETED, CHECKOUT_ORDER_APPROVED, PAYMENT_CAPTURE_COMPLETED -> PaymentCallbackType.PAYMENT_SUCCESS;
            case PAYMENT_SALE_DENIED, PAYMENT_CAPTURE_DENIED, PAYMENT_CAPTURE_FAILED -> PaymentCallbackType.PAYMENT_FAILED;
            case PAYMENT_SALE_REFUNDED, PAYMENT_CAPTURE_REFUNDED, PAYMENT_REFUND_COMPLETED -> PaymentCallbackType.REFUND_SUCCESS;
            case PAYMENT_REFUND_DENIED -> PaymentCallbackType.REFUND_FAILED;
        };
    }
}