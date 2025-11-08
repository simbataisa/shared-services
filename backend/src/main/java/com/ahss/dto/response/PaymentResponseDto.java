package com.ahss.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Generic payment response DTO used internally to normalize
 * responses across different payment channels.
 */
public class PaymentResponseDto {

    private boolean success;
    private String status; // e.g., "AUTHORIZED", "CAPTURED", "FAILED"
    private String message;
    private String errorCode;
    private String errorMessage;
    private String gatewayName;
    private String externalTransactionId;
    private String externalRefundId;
    private UUID paymentRequestId;
    private UUID paymentTransactionId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
    private Map<String, Object> gatewayResponse;
    private Map<String, Object> metadata;

    public PaymentResponseDto() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getGatewayName() { return gatewayName; }
    public void setGatewayName(String gatewayName) { this.gatewayName = gatewayName; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public String getExternalRefundId() { return externalRefundId; }
    public void setExternalRefundId(String externalRefundId) { this.externalRefundId = externalRefundId; }

    public UUID getPaymentRequestId() { return paymentRequestId; }
    public void setPaymentRequestId(UUID paymentRequestId) { this.paymentRequestId = paymentRequestId; }

    public UUID getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(UUID paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Map<String, Object> getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(Map<String, Object> gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}