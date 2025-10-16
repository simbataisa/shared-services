package com.ahss.service.impl;

import com.ahss.dto.request.ProcessPaymentDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.entity.PaymentTransaction;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.enums.PaymentTransactionType;
import com.ahss.enums.PaymentMethodType;
import com.ahss.repository.PaymentTransactionRepository;
import com.ahss.service.PaymentTransactionService;
import com.ahss.service.PaymentAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PaymentAuditLogService auditLogService;

    @Override
    public PaymentTransactionDto processPayment(ProcessPaymentDto processDto) {
        // Implementation would process the payment
        // For now, just create a basic transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionCode("TXN-" + System.currentTimeMillis());
        transaction.setTransactionStatus(PaymentTransactionStatus.PENDING);
        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
        return convertToDto(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionDto> getTransactionById(Long id) {
        return paymentTransactionRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionDto> getTransactionByCode(String transactionCode) {
        return paymentTransactionRepository.findByTransactionCode(transactionCode)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionDto> getTransactionByExternalId(String externalTransactionId) {
        return paymentTransactionRepository.findByExternalTransactionId(externalTransactionId)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getAllTransactions(Pageable pageable) {
        return paymentTransactionRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByPaymentRequest(Long paymentRequestId, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByPaymentRequestId(paymentRequestId);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByStatus(PaymentTransactionStatus status, Pageable pageable) {
        return paymentTransactionRepository.findByTransactionStatus(status, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByType(PaymentTransactionType type, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByTransactionType(type);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByPaymentMethod(PaymentMethodType paymentMethod, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByPaymentMethod(paymentMethod);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByGateway(String gatewayName, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByGatewayName(gatewayName);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByCreatedAtBetween(startDate, endDate);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsProcessedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByProcessedAtBetween(startDate, endDate);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByAmountBetween(minAmount, maxAmount);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByCurrency(String currency, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByCurrency(currency);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> getTransactionsByErrorCode(String errorCode, Pageable pageable) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByErrorCode(errorCode);
        return convertListToPage(transactions, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDto> searchTransactions(String searchTerm, Pageable pageable) {
        return paymentTransactionRepository.searchTransactions(searchTerm, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getStaleTransactions(LocalDateTime cutoffTime) {
        // Implementation would find stale transactions
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransactionDto> getSuccessfulTransactionsByRequest(Long paymentRequestId) {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findByPaymentRequestId(paymentRequestId);
        return transactions.stream()
                .filter(t -> t.getTransactionStatus() == PaymentTransactionStatus.SUCCESS)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentTransactionDto updateTransactionStatus(Long id, PaymentTransactionStatus status, String reason) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found with id: " + id));

        String oldStatus = transaction.getTransactionStatus().toString();
        transaction.setTransactionStatus(status);
        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);

        return convertToDto(updatedTransaction);
    }

    @Override
    public PaymentTransactionDto markAsProcessed(Long id, String externalTransactionId, Map<String, Object> gatewayResponse) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found with id: " + id));

        String oldStatus = transaction.getTransactionStatus().toString();
        transaction.setTransactionStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setExternalTransactionId(externalTransactionId);
        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);

        return convertToDto(updatedTransaction);
    }

    @Override
    public PaymentTransactionDto markAsFailed(Long id, String errorCode, String errorMessage) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found with id: " + id));

        String oldStatus = transaction.getTransactionStatus().toString();
        transaction.setTransactionStatus(PaymentTransactionStatus.FAILED);
        transaction.setErrorCode(errorCode);
        transaction.setErrorMessage(errorMessage);
        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);

        return convertToDto(updatedTransaction);
    }

    @Override
    public PaymentTransactionDto retryTransaction(Long id) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found with id: " + id));

        String oldStatus = transaction.getTransactionStatus().toString();
        transaction.setTransactionStatus(PaymentTransactionStatus.PENDING);
        transaction.setErrorCode(null);
        transaction.setErrorMessage(null);
        PaymentTransaction updatedTransaction = paymentTransactionRepository.save(transaction);

        return convertToDto(updatedTransaction);
    }

    @Override
    public void cancelTransaction(Long id, String reason) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found with id: " + id));

        String oldStatus = transaction.getTransactionStatus().toString();
        transaction.setTransactionStatus(PaymentTransactionStatus.CANCELLED);
        paymentTransactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTransactionCode(String transactionCode) {
        return paymentTransactionRepository.existsByTransactionCode(transactionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByExternalTransactionId(String externalTransactionId) {
        return paymentTransactionRepository.existsByExternalTransactionId(externalTransactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByStatus(PaymentTransactionStatus status) {
        return paymentTransactionRepository.countByTransactionStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByType(PaymentTransactionType type) {
        // Implementation would count by type
        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByPaymentMethod(PaymentMethodType paymentMethod) {
        // Implementation would count by payment method
        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAmountByStatusAndCurrency(PaymentTransactionStatus status, String currency) {
        return paymentTransactionRepository.sumAmountByStatusAndCurrency(status, currency);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTransactionCountByPaymentMethod() {
        // Implementation would return count by payment method
        return Map.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getTransactionAmountByCurrency() {
        // Implementation would return amount by currency
        return Map.of();
    }

    @Override
    public void processStaleTransactions() {
        // Implementation would process stale transactions
    }

    @Override
    public void syncTransactionStatusWithGateway(Long id) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found with id: " + id));

        // Implementation would call the actual gateway API
        // For now, just log the action
    }

    private Page<PaymentTransactionDto> convertListToPage(List<PaymentTransaction> transactions, Pageable pageable) {
        List<PaymentTransactionDto> dtos = transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        if (start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    private PaymentTransactionDto convertToDto(PaymentTransaction entity) {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setId(entity.getId());
        dto.setTransactionCode(entity.getTransactionCode());
        dto.setPaymentRequestId(entity.getPaymentRequestId());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setTransactionType(entity.getTransactionType());
        dto.setTransactionStatus(entity.getTransactionStatus());
        dto.setExternalTransactionId(entity.getExternalTransactionId());
        dto.setGatewayName(entity.getGatewayName());
        dto.setGatewayResponse(entity.getGatewayResponse());
        dto.setProcessedAt(entity.getProcessedAt());
        dto.setErrorCode(entity.getErrorCode());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setMetadata(entity.getMetadata());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().toString() : null);
        dto.setUpdatedBy(null); // Entity doesn't have updatedBy field
        return dto;
    }
}