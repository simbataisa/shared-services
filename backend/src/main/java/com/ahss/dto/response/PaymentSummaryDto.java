package com.ahss.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentSummaryDto {

    private Long totalPaymentRequests;
    private Long pendingPaymentRequests;
    private Long completedPaymentRequests;
    private Long failedPaymentRequests;
    private Long expiredPaymentRequests;
    
    private BigDecimal totalAmount;
    private BigDecimal pendingAmount;
    private BigDecimal completedAmount;
    private BigDecimal refundedAmount;
    
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Long pendingTransactions;
    
    private Long totalRefunds;
    private Long successfulRefunds;
    private Long failedRefunds;
    private Long pendingRefunds;
    
    private Map<String, Long> paymentMethodBreakdown;
    private Map<String, BigDecimal> currencyBreakdown;
    private Map<String, Long> statusBreakdown;

    // Constructors
    public PaymentSummaryDto() {}

    // Getters and Setters
    public Long getTotalPaymentRequests() {
        return totalPaymentRequests;
    }

    public void setTotalPaymentRequests(Long totalPaymentRequests) {
        this.totalPaymentRequests = totalPaymentRequests;
    }

    public Long getPendingPaymentRequests() {
        return pendingPaymentRequests;
    }

    public void setPendingPaymentRequests(Long pendingPaymentRequests) {
        this.pendingPaymentRequests = pendingPaymentRequests;
    }

    public Long getCompletedPaymentRequests() {
        return completedPaymentRequests;
    }

    public void setCompletedPaymentRequests(Long completedPaymentRequests) {
        this.completedPaymentRequests = completedPaymentRequests;
    }

    public Long getFailedPaymentRequests() {
        return failedPaymentRequests;
    }

    public void setFailedPaymentRequests(Long failedPaymentRequests) {
        this.failedPaymentRequests = failedPaymentRequests;
    }

    public Long getExpiredPaymentRequests() {
        return expiredPaymentRequests;
    }

    public void setExpiredPaymentRequests(Long expiredPaymentRequests) {
        this.expiredPaymentRequests = expiredPaymentRequests;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public BigDecimal getCompletedAmount() {
        return completedAmount;
    }

    public void setCompletedAmount(BigDecimal completedAmount) {
        this.completedAmount = completedAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Long getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(Long successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public Long getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(Long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public Long getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(Long pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Long getTotalRefunds() {
        return totalRefunds;
    }

    public void setTotalRefunds(Long totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    public Long getSuccessfulRefunds() {
        return successfulRefunds;
    }

    public void setSuccessfulRefunds(Long successfulRefunds) {
        this.successfulRefunds = successfulRefunds;
    }

    public Long getFailedRefunds() {
        return failedRefunds;
    }

    public void setFailedRefunds(Long failedRefunds) {
        this.failedRefunds = failedRefunds;
    }

    public Long getPendingRefunds() {
        return pendingRefunds;
    }

    public void setPendingRefunds(Long pendingRefunds) {
        this.pendingRefunds = pendingRefunds;
    }

    public Map<String, Long> getPaymentMethodBreakdown() {
        return paymentMethodBreakdown;
    }

    public void setPaymentMethodBreakdown(Map<String, Long> paymentMethodBreakdown) {
        this.paymentMethodBreakdown = paymentMethodBreakdown;
    }

    public Map<String, BigDecimal> getCurrencyBreakdown() {
        return currencyBreakdown;
    }

    public void setCurrencyBreakdown(Map<String, BigDecimal> currencyBreakdown) {
        this.currencyBreakdown = currencyBreakdown;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }
}