package com.ahss.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class PaymentAuditLogDto {

    private UUID id;
    private UUID paymentRequestId;
    private UUID paymentTransactionId;
    private UUID paymentRefundId;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String description;
    private Map<String, Object> changeDetails;
    private Long userId;
    private String userAgent;
    private String ipAddress;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;

    // Constructors
    public PaymentAuditLogDto() {}

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(UUID paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public UUID getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(UUID paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public UUID getPaymentRefundId() {
        return paymentRefundId;
    }

    public void setPaymentRefundId(UUID paymentRefundId) {
        this.paymentRefundId = paymentRefundId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getChangeDetails() {
        return changeDetails;
    }

    public void setChangeDetails(Map<String, Object> changeDetails) {
        this.changeDetails = changeDetails;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}