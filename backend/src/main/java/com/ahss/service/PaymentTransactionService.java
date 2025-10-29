package com.ahss.service;

import com.ahss.dto.request.ProcessPaymentDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.enums.PaymentTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionService {

    PaymentTransactionDto processPayment(ProcessPaymentDto processDto);

    Optional<PaymentTransactionDto> getTransactionById(UUID id);

    Optional<PaymentTransactionDto> getTransactionByCode(String transactionCode);

    Optional<PaymentTransactionDto> getTransactionByExternalId(String externalTransactionId);

    Page<PaymentTransactionDto> getAllTransactions(Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByPaymentRequest(UUID paymentRequestId, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByStatus(PaymentTransactionStatus status, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByType(PaymentTransactionType type, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByPaymentMethod(PaymentMethodType paymentMethod, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByGateway(String gatewayName, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsProcessedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByCurrency(String currency, Pageable pageable);

    Page<PaymentTransactionDto> getTransactionsByErrorCode(String errorCode, Pageable pageable);

    Page<PaymentTransactionDto> searchTransactions(String searchTerm, Pageable pageable);

    List<PaymentTransactionDto> getStaleTransactions(LocalDateTime cutoffTime);

    List<PaymentTransactionDto> getSuccessfulTransactionsByRequest(UUID paymentRequestId);

    PaymentTransactionDto updateTransactionStatus(UUID id, PaymentTransactionStatus status, String reason);

    PaymentTransactionDto markAsProcessed(UUID id, String externalTransactionId, Map<String, Object> gatewayResponse);

    PaymentTransactionDto markAsFailed(UUID id, String errorCode, String errorMessage);

    PaymentTransactionDto retryTransaction(UUID id);

    void cancelTransaction(UUID id, String reason);

    boolean existsByTransactionCode(String transactionCode);

    boolean existsByExternalTransactionId(String externalTransactionId);

    Long countByStatus(PaymentTransactionStatus status);

    Long countByType(PaymentTransactionType type);

    Long countByPaymentMethod(PaymentMethodType paymentMethod);

    BigDecimal sumAmountByStatusAndCurrency(PaymentTransactionStatus status, String currency);

    Map<String, Long> getTransactionCountByPaymentMethod();

    Map<String, BigDecimal> getTransactionAmountByCurrency();

    void processStaleTransactions();

    void syncTransactionStatusWithGateway(UUID id);
}