package com.ahss.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_audit_log")
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_audit_log_id")
    private Long id;

    @Column(name = "payment_request_id", nullable = false)
    private Long paymentRequestId;

    @Column(name = "payment_transaction_id")
    private Long paymentTransactionId;

    @Column(name = "payment_refund_id")
    private Long paymentRefundId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes", columnDefinition = "jsonb")
    private Map<String, Object> changeDetails;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by")
    private Long userId;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_request_id", insertable = false, updatable = false)
    private PaymentRequest paymentRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id", insertable = false, updatable = false)
    private PaymentTransaction paymentTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_refund_id", insertable = false, updatable = false)
    private PaymentRefund paymentRefund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public PaymentAuditLog() {}

    public PaymentAuditLog(Long paymentRequestId, String action, String description) {
        this.paymentRequestId = paymentRequestId;
        this.action = action;
        this.description = description;
    }

    public PaymentAuditLog(Long paymentRequestId, String action, String oldStatus, 
                          String newStatus, String description) {
        this.paymentRequestId = paymentRequestId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.description = description;
    }

    // Static factory methods for common audit actions
    public static PaymentAuditLog createPaymentRequestAudit(Long paymentRequestId, String action, 
                                                           String oldStatus, String newStatus, 
                                                           String description, Long userId) {
        PaymentAuditLog audit = new PaymentAuditLog(paymentRequestId, action, oldStatus, newStatus, description);
        audit.setUserId(userId);
        return audit;
    }

    public static PaymentAuditLog createTransactionAudit(Long paymentRequestId, Long paymentTransactionId, 
                                                        String action, String description, Long userId) {
        PaymentAuditLog audit = new PaymentAuditLog(paymentRequestId, action, description);
        audit.setPaymentTransactionId(paymentTransactionId);
        audit.setUserId(userId);
        return audit;
    }

    public static PaymentAuditLog createRefundAudit(Long paymentRequestId, Long paymentRefundId, 
                                                   String action, String description, Long userId) {
        PaymentAuditLog audit = new PaymentAuditLog(paymentRequestId, action, description);
        audit.setPaymentRefundId(paymentRefundId);
        audit.setUserId(userId);
        return audit;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public Long getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(Long paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public Long getPaymentRefundId() {
        return paymentRefundId;
    }

    public void setPaymentRefundId(Long paymentRefundId) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
    }

    public PaymentRefund getPaymentRefund() {
        return paymentRefund;
    }

    public void setPaymentRefund(PaymentRefund paymentRefund) {
        this.paymentRefund = paymentRefund;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Business methods
    public boolean isStatusChange() {
        return oldStatus != null && newStatus != null && !oldStatus.equals(newStatus);
    }

    public boolean isSystemAction() {
        return userId == null;
    }

    public boolean isUserAction() {
        return userId != null;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}