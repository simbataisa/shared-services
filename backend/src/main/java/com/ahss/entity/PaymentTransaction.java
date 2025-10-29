package com.ahss.entity;

import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.enums.PaymentTransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "payment_transaction_id")
    private UUID id;

    @Column(name = "transaction_code", unique = true, nullable = false, length = 50)
    private String transactionCode;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "payment_request_id", nullable = false)
    private UUID paymentRequestId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "payment_transaction_type")
    private PaymentTransactionType transactionType;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_status", nullable = false, columnDefinition = "payment_transaction_status")
    private PaymentTransactionStatus transactionStatus = PaymentTransactionStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_method", nullable = false, columnDefinition = "payment_method_type")
    private PaymentMethodType paymentMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_method_details", columnDefinition = "jsonb")
    private Map<String, Object> paymentMethodDetails;

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

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_request_id", insertable = false, updatable = false)
    private PaymentRequest paymentRequest;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (transactionCode == null) {
            transactionCode = generateTransactionCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateTransactionCode() {
        return "TXN-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    // Constructors
    public PaymentTransaction() {}

    public PaymentTransaction(UUID paymentRequestId, PaymentTransactionType transactionType, 
                            BigDecimal amount, PaymentMethodType paymentMethod) {
        this.paymentRequestId = paymentRequestId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public UUID getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(UUID paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public PaymentTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(PaymentTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public PaymentTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(PaymentTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethodType getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethodType paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Map<String, Object> getPaymentMethodDetails() {
        return paymentMethodDetails;
    }

    public void setPaymentMethodDetails(Map<String, Object> paymentMethodDetails) {
        this.paymentMethodDetails = paymentMethodDetails;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
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

    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    // Business methods
    public boolean isSuccessful() {
        return transactionStatus.isSuccessful();
    }

    public boolean isFailed() {
        return transactionStatus == PaymentTransactionStatus.FAILED;
    }

    public boolean isPending() {
        return transactionStatus.isProcessing();
    }

    public boolean canBeRetried() {
        return transactionStatus.canBeRetried() && retryCount < maxRetries;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markAsProcessed() {
        this.processedAt = LocalDateTime.now();
        this.transactionStatus = PaymentTransactionStatus.SUCCESS;
    }

    public void markAsFailed(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.transactionStatus = PaymentTransactionStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
}