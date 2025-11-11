package com.ahss.entity;

import com.ahss.enums.PaymentTransactionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment_refund")
public class PaymentRefund {

    @Id
    @GeneratedValue(generator = "uuid2")
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "payment_refund_id")
    private UUID id;

    @Column(name = "refund_code", unique = true, nullable = false, length = 50)
    private String refundCode;

    @Column(name = "payment_transaction_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID paymentTransactionId;

    @Column(name = "refund_transaction_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID refundTransactionId;

    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String reason;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "refund_status", nullable = false, columnDefinition = "payment_transaction_status")
    private PaymentTransactionStatus refundStatus = PaymentTransactionStatus.PENDING;

    @Column(name = "external_refund_id")
    private String externalRefundId;

    @Column(name = "gateway_name", length = 100)
    private String gatewayName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id", insertable = false, updatable = false)
    private PaymentTransaction paymentTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_transaction_id", insertable = false, updatable = false)
    private PaymentTransaction refundTransaction;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (refundCode == null) {
            refundCode = generateRefundCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateRefundCode() {
        return "REF-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    // Constructors
    public PaymentRefund() {}

    public PaymentRefund(UUID paymentTransactionId, BigDecimal refundAmount, String reason) {
        this.paymentTransactionId = paymentTransactionId;
        this.refundAmount = refundAmount;
        this.reason = reason;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRefundCode() {
        return refundCode;
    }

    public void setRefundCode(String refundCode) {
        this.refundCode = refundCode;
    }

    public UUID getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(UUID paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public UUID getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(UUID refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
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

    public PaymentTransactionStatus getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(PaymentTransactionStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getExternalRefundId() {
        return externalRefundId;
    }

    public void setExternalRefundId(String externalRefundId) {
        this.externalRefundId = externalRefundId;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Map<String, Object> getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(Map<String, Object> gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
    }

    public PaymentTransaction getRefundTransaction() {
        return refundTransaction;
    }

    public void setRefundTransaction(PaymentTransaction refundTransaction) {
        this.refundTransaction = refundTransaction;
    }

    // Business logic methods
    public boolean isSuccessful() {
        return refundStatus.isSuccessful();
    }

    public boolean isFailed() {
        return refundStatus == PaymentTransactionStatus.FAILED;
    }

    public boolean isPending() {
        return refundStatus.isProcessing();
    }

    public boolean canBeRetried() {
        return refundStatus.canBeRetried();
    }

    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
        this.refundStatus = PaymentTransactionStatus.SUCCESS;
    }

    public void markAsFailed(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.refundStatus = PaymentTransactionStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
}