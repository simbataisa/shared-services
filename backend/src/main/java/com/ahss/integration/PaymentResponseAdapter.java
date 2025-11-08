package com.ahss.integration;

import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.integration.mapper.PaymentChannelIntegrationEventTypeMapper;

import java.time.LocalDateTime;

/**
 * Adapter that converts outbound PaymentResponseDto to PaymentCallbackEvent,
 * aligning the outbound response shape with inbound webhook events.
 */
public final class PaymentResponseAdapter {

    private PaymentResponseAdapter() {}

    public static PaymentCallbackEvent toCallbackEvent(PaymentResponseDto resp) {
        PaymentCallbackEvent evt = new PaymentCallbackEvent();
        evt.setType(resolveCallbackType(resp));
        evt.setCorrelationId(null); // populate if correlation is available
        evt.setPaymentRequestId(resp.getPaymentRequestId());
        evt.setPaymentTransactionId(resp.getPaymentTransactionId());
        evt.setPaymentRefundId(null);
        evt.setPaymentToken(null);
        evt.setRequestCode(null);
        evt.setExternalTransactionId(resp.getExternalTransactionId());
        evt.setExternalRefundId(resp.getExternalRefundId());
        evt.setAmount(resp.getAmount());
        evt.setCurrency(resp.getCurrency());
        evt.setGatewayName(resp.getGatewayName());
        evt.setGatewayResponse(resp.getGatewayResponse());
        evt.setErrorCode(resp.getErrorCode());
        evt.setErrorMessage(resp.getErrorMessage());
        evt.setMetadata(resp.getMetadata());
        evt.setReceivedAt(LocalDateTime.now());
        return evt;
    }

    private static PaymentCallbackType resolveCallbackType(PaymentResponseDto resp) {
        if (!resp.isSuccess()) {
            return PaymentCallbackType.PAYMENT_FAILED;
        }
        return PaymentChannelIntegrationEventTypeMapper.mapGenericStatus(resp.getStatus());
    }
}