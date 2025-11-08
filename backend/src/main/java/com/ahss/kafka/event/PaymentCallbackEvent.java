package com.ahss.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class PaymentCallbackEvent {
    private PaymentCallbackType type;
    private String correlationId;

    private UUID paymentRequestId;
    private UUID paymentTransactionId;
    private UUID paymentRefundId;

    private String paymentToken;
    private String requestCode;
    private String externalTransactionId;
    private String externalRefundId;

    private BigDecimal amount;
    private String currency;
    private String gatewayName;
    private Map<String, Object> gatewayResponse;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> metadata;
    private LocalDateTime receivedAt;

    public PaymentCallbackEvent() {}

    public PaymentCallbackType getType() { return type; }
    public void setType(PaymentCallbackType type) { this.type = type; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public UUID getPaymentRequestId() { return paymentRequestId; }
    public void setPaymentRequestId(UUID paymentRequestId) { this.paymentRequestId = paymentRequestId; }

    public UUID getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(UUID paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public UUID getPaymentRefundId() { return paymentRefundId; }
    public void setPaymentRefundId(UUID paymentRefundId) { this.paymentRefundId = paymentRefundId; }

    public String getPaymentToken() { return paymentToken; }
    public void setPaymentToken(String paymentToken) { this.paymentToken = paymentToken; }

    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public String getExternalRefundId() { return externalRefundId; }
    public void setExternalRefundId(String externalRefundId) { this.externalRefundId = externalRefundId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getGatewayName() { return gatewayName; }
    public void setGatewayName(String gatewayName) { this.gatewayName = gatewayName; }

    public Map<String, Object> getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(Map<String, Object> gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
}