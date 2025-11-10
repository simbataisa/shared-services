package com.ahss.entity;

import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentRequestStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "payment_request")
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "payment_request_id")
    private UUID id;

    @Column(name = "request_code", unique = true, nullable = false, length = 50)
    private String requestCode;

    @Column(name = "payment_token", unique = true, nullable = false)
    private String paymentToken;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "payer_phone", length = 50)
    private String payerPhone;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_payment_methods", columnDefinition = "text[]", nullable = false)
    private List<String> allowedPaymentMethodsRaw = Arrays.asList("CREDIT_CARD", "DEBIT_CARD");

    @Transient
    private PaymentMethodType[] allowedPaymentMethods;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "pre_selected_payment_method", columnDefinition = "payment_method_type")
    private PaymentMethodType preSelectedPaymentMethod;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "payment_request_status")
    private PaymentRequestStatus status = PaymentRequestStatus.DRAFT;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    // Relationships
    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentAuditLog> auditLogs = new ArrayList<>();

    @PostLoad
    protected void onLoad() {
        // Convert raw string list to enum array after loading from database
        if (allowedPaymentMethodsRaw != null) {
            allowedPaymentMethods = allowedPaymentMethodsRaw.stream()
                    .map(PaymentMethodType::valueOf)
                    .toArray(PaymentMethodType[]::new);
        }
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (requestCode == null) {
            requestCode = generateRequestCode();
        }
        if (paymentToken == null) {
            paymentToken = generatePaymentToken();
        }
        // Convert enum array to raw string list before persisting
        if (allowedPaymentMethods != null) {
            allowedPaymentMethodsRaw = Arrays.stream(allowedPaymentMethods)
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Convert enum array to raw string list before updating
        if (allowedPaymentMethods != null) {
            allowedPaymentMethodsRaw = Arrays.stream(allowedPaymentMethods)
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
    }

    private String generateRequestCode() {
        return "PR-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    private String generatePaymentToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(String title, BigDecimal amount, Long tenantId) {
        this.title = title;
        this.amount = amount;
        this.tenantId = tenantId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getPayerPhone() {
        return payerPhone;
    }

    public void setPayerPhone(String payerPhone) {
        this.payerPhone = payerPhone;
    }

    public PaymentMethodType[] getAllowedPaymentMethods() {
        // Lazy initialization in case @PostLoad hasn't been called (e.g., in unit tests)
        if (allowedPaymentMethods == null && allowedPaymentMethodsRaw != null && !allowedPaymentMethodsRaw.isEmpty()) {
            allowedPaymentMethods = allowedPaymentMethodsRaw.stream()
                    .map(PaymentMethodType::valueOf)
                    .toArray(PaymentMethodType[]::new);
        }
        return allowedPaymentMethods;
    }

    public void setAllowedPaymentMethods(PaymentMethodType[] allowedPaymentMethods) {
        this.allowedPaymentMethods = allowedPaymentMethods;
        if (allowedPaymentMethods != null) {
            this.allowedPaymentMethodsRaw = Arrays.stream(allowedPaymentMethods)
                    .map(Enum::name)
                    .collect(Collectors.toList());
        } else {
            this.allowedPaymentMethodsRaw = new ArrayList<>();
        }
    }

    public PaymentMethodType getPreSelectedPaymentMethod() {
        return preSelectedPaymentMethod;
    }

    public void setPreSelectedPaymentMethod(PaymentMethodType preSelectedPaymentMethod) {
        this.preSelectedPaymentMethod = preSelectedPaymentMethod;
    }

    public PaymentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentRequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Business methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canAcceptPayment() {
        return status.canAcceptPayment() && !isExpired();
    }

    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }

    public boolean canBeVoided() {
        return status.canBeVoided();
    }

    public boolean canBeRefunded() {
        return status.canBeRefunded();
    }

    public String getPaymentLink(String baseUrl) {
        return baseUrl + "/payment/" + paymentToken;
    }
}