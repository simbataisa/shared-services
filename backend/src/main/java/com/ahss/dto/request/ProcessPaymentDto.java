package com.ahss.dto.request;

import com.ahss.enums.PaymentMethodType;
import jakarta.validation.constraints.*;

import java.util.Map;

public class ProcessPaymentDto {

    @NotBlank(message = "Payment token is required")
    private String paymentToken;

    @NotNull(message = "Payment method is required")
    private PaymentMethodType paymentMethod;

    @NotNull(message = "Payment method details are required")
    private Map<String, Object> paymentMethodDetails;

    @Size(max = 100, message = "Gateway name must not exceed 100 characters")
    private String gatewayName;

    private String returnUrl;

    private String cancelUrl;

    private Map<String, Object> metadata;

    // Constructors
    public ProcessPaymentDto() {}

    public ProcessPaymentDto(String paymentToken, PaymentMethodType paymentMethod, 
                           Map<String, Object> paymentMethodDetails) {
        this.paymentToken = paymentToken;
        this.paymentMethod = paymentMethod;
        this.paymentMethodDetails = paymentMethodDetails;
    }

    // Getters and Setters
    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
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

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}