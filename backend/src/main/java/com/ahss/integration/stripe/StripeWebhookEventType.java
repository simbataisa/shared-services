package com.ahss.integration.stripe;

/**
 * Canonical Stripe webhook event types.
 */
public enum StripeWebhookEventType {
    PAYMENT_INTENT_SUCCEEDED("payment_intent.succeeded"),
    PAYMENT_INTENT_PAYMENT_FAILED("payment_intent.payment_failed"),
    PAYMENT_INTENT_CANCELED("payment_intent.canceled"),
    CHARGE_FAILED("charge.failed"),
    CHARGE_REFUNDED("charge.refunded"),
    REFUND_CREATED("refund.created"),
    REFUND_UPDATED("refund.updated");

    private final String value;

    StripeWebhookEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static StripeWebhookEventType fromValue(String value) {
        if (value == null) return null;
        for (StripeWebhookEventType t : values()) {
            if (t.value.equals(value)) return t;
        }
        return null;
    }
}