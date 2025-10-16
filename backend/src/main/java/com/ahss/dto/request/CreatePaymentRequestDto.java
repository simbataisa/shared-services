package com.ahss.dto.request;

import com.ahss.enums.PaymentMethodType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CreatePaymentRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency = "USD";

    @NotBlank(message = "Payer name is required")
    @Size(max = 255, message = "Payer name must not exceed 255 characters")
    private String payerName;

    @NotBlank(message = "Payer email is required")
    @Email(message = "Payer email should be valid")
    private String payerEmail;

    @Size(max = 20, message = "Payer phone must not exceed 20 characters")
    private String payerPhone;

    @NotEmpty(message = "At least one payment method must be allowed")
    private List<PaymentMethodType> allowedPaymentMethods;

    private PaymentMethodType preSelectedPaymentMethod;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    private Map<String, Object> metadata;

    // Constructors
    public CreatePaymentRequestDto() {}

    public CreatePaymentRequestDto(String title, BigDecimal amount, String payerName, String payerEmail, 
                                  List<PaymentMethodType> allowedPaymentMethods, Long tenantId) {
        this.title = title;
        this.amount = amount;
        this.payerName = payerName;
        this.payerEmail = payerEmail;
        this.allowedPaymentMethods = allowedPaymentMethods;
        this.tenantId = tenantId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<PaymentMethodType> getAllowedPaymentMethods() {
        return allowedPaymentMethods;
    }

    public void setAllowedPaymentMethods(List<PaymentMethodType> allowedPaymentMethods) {
        this.allowedPaymentMethods = allowedPaymentMethods;
    }

    public PaymentMethodType getPreSelectedPaymentMethod() {
        return preSelectedPaymentMethod;
    }

    public void setPreSelectedPaymentMethod(PaymentMethodType preSelectedPaymentMethod) {
        this.preSelectedPaymentMethod = preSelectedPaymentMethod;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
}