package com.ahss.kafka.event;

public enum PaymentCallbackType {
    REQUEST_APPROVED,
    REQUEST_REJECTED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REFUND_SUCCESS,
    REFUND_FAILED
}