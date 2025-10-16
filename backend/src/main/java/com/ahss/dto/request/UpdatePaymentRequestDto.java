package com.ahss.dto.request;

import com.ahss.enums.PaymentMethodType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UpdatePaymentRequestDto {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    @Size(max = 255, message = "Payer name must not exceed 255 characters")
    private String payerName;

    @Email(message = "Payer email should be valid")
    private String payerEmail;

    @Size(max = 20, message = "Payer phone must not exceed 20 characters")
    private String payerPhone;

    private List<PaymentMethodType> allowedPaymentMethods;

    private PaymentMethodType preSelectedPaymentMethod;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    private Map<String, Object> metadata;

    // Constructors
    public UpdatePaymentRequestDto() {}

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}