package com.ahss.integration.mapper;

import com.ahss.integration.paypal.PayPalWebhookEventType;
import com.ahss.integration.stripe.StripeWebhookEventType;
import com.ahss.kafka.event.PaymentCallbackType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Shared mapper for webhook event types across gateways using EnumMap.
 */
public final class PaymentChannelIntegrationEventTypeMapper {

    private static final Map<PayPalWebhookEventType, PaymentCallbackType> PAYPAL_MAP =
            new EnumMap<>(PayPalWebhookEventType.class);
    private static final Map<StripeWebhookEventType, PaymentCallbackType> STRIPE_MAP =
            new EnumMap<>(StripeWebhookEventType.class);

    static {
        // PayPal mappings
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_SALE_COMPLETED, PaymentCallbackType.PAYMENT_SUCCESS);
        PAYPAL_MAP.put(PayPalWebhookEventType.CHECKOUT_ORDER_APPROVED, PaymentCallbackType.PAYMENT_SUCCESS);
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_CAPTURE_COMPLETED, PaymentCallbackType.PAYMENT_SUCCESS);

        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_SALE_DENIED, PaymentCallbackType.PAYMENT_FAILED);
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_CAPTURE_DENIED, PaymentCallbackType.PAYMENT_FAILED);
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_CAPTURE_FAILED, PaymentCallbackType.PAYMENT_FAILED);

        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_SALE_REFUNDED, PaymentCallbackType.REFUND_SUCCESS);
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_CAPTURE_REFUNDED, PaymentCallbackType.REFUND_SUCCESS);
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_REFUND_COMPLETED, PaymentCallbackType.REFUND_SUCCESS);
        PAYPAL_MAP.put(PayPalWebhookEventType.PAYMENT_REFUND_DENIED, PaymentCallbackType.REFUND_FAILED);

        // Stripe mappings
        STRIPE_MAP.put(StripeWebhookEventType.PAYMENT_INTENT_SUCCEEDED, PaymentCallbackType.PAYMENT_SUCCESS);
        STRIPE_MAP.put(StripeWebhookEventType.PAYMENT_INTENT_PAYMENT_FAILED, PaymentCallbackType.PAYMENT_FAILED);
        STRIPE_MAP.put(StripeWebhookEventType.PAYMENT_INTENT_CANCELED, PaymentCallbackType.PAYMENT_FAILED);
        STRIPE_MAP.put(StripeWebhookEventType.CHARGE_FAILED, PaymentCallbackType.PAYMENT_FAILED);
        STRIPE_MAP.put(StripeWebhookEventType.CHARGE_REFUNDED, PaymentCallbackType.REFUND_SUCCESS);
        STRIPE_MAP.put(StripeWebhookEventType.REFUND_CREATED, PaymentCallbackType.REFUND_SUCCESS);
        STRIPE_MAP.put(StripeWebhookEventType.REFUND_UPDATED, PaymentCallbackType.REFUND_FAILED);
    }

    private PaymentChannelIntegrationEventTypeMapper() {}

    public static PaymentCallbackType mapPayPal(String eventType) {
        PayPalWebhookEventType t = PayPalWebhookEventType.fromValue(eventType);
        if (t == null) return PaymentCallbackType.PAYMENT_SUCCESS; // keep controller default
        return PAYPAL_MAP.getOrDefault(t, PaymentCallbackType.PAYMENT_SUCCESS);
    }

    public static PaymentCallbackType mapStripe(String eventType) {
        StripeWebhookEventType t = StripeWebhookEventType.fromValue(eventType);
        if (t == null) return PaymentCallbackType.PAYMENT_SUCCESS; // keep controller default
        return STRIPE_MAP.getOrDefault(t, PaymentCallbackType.PAYMENT_SUCCESS);
    }

    /**
     * Maps generic outbound status strings to callback types,
     * reusing centralized mapping instead of duplicating logic.
     */
    public static PaymentCallbackType mapGenericStatus(String status) {
        if (status == null) return PaymentCallbackType.PAYMENT_SUCCESS;
        switch (status) {
            case "AUTHORIZED":
            case "CAPTURED":
            case "TOKENIZED":
                return PaymentCallbackType.PAYMENT_SUCCESS;
            case "REFUNDED":
                return PaymentCallbackType.REFUND_SUCCESS;
            default:
                return PaymentCallbackType.PAYMENT_SUCCESS;
        }
    }
}