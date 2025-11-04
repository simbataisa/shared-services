package com.ahss.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class CreateRefundDto {

    @NotNull(message = "Payment transaction ID is required")
    private UUID paymentTransactionId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Refund amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal refundAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @Size(max = 100, message = "Gateway name must not exceed 100 characters")
    private String gatewayName;

    private Map<String, Object> metadata;

    // Constructors
    public CreateRefundDto() {}

    public CreateRefundDto(UUID paymentTransactionId, BigDecimal refundAmount, 
                          String currency, String reason) {
        this.paymentTransactionId = paymentTransactionId;
        this.refundAmount = refundAmount;
        this.currency = currency;
        this.reason = reason;
    }

    // Getters and Setters
    public UUID getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(UUID paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}