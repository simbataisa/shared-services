package com.ahss.service;

import com.ahss.dto.request.CreateRefundDto;
import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.enums.PaymentTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRefundService {

    PaymentRefundDto createRefund(CreateRefundDto createDto);

    PaymentRefundDto processRefund(UUID refundId);

    Optional<PaymentRefundDto> getRefundById(UUID id);

    Optional<PaymentRefundDto> getRefundByCode(String refundCode);

    Optional<PaymentRefundDto> getRefundByExternalId(String externalRefundId);

    Page<PaymentRefundDto> getAllRefunds(Pageable pageable);

    Page<PaymentRefundDto> getRefundsByTransaction(UUID paymentTransactionId, Pageable pageable);

    Page<PaymentRefundDto> getRefundsByStatus(PaymentTransactionStatus status, Pageable pageable);

    Page<PaymentRefundDto> getRefundsByGateway(String gatewayName, Pageable pageable);

    Page<PaymentRefundDto> getRefundsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentRefundDto> getRefundsProcessedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentRefundDto> getRefundsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    Page<PaymentRefundDto> getRefundsByCurrency(String currency, Pageable pageable);

    Page<PaymentRefundDto> getRefundsByErrorCode(String errorCode, Pageable pageable);

    Page<PaymentRefundDto> getRefundsByReason(String reason, Pageable pageable);

    Page<PaymentRefundDto> searchRefunds(String searchTerm, Pageable pageable);

    List<PaymentRefundDto> getStaleRefunds(LocalDateTime cutoffTime);

    List<PaymentRefundDto> getSuccessfulRefundsByTransaction(UUID paymentTransactionId);

    List<PaymentRefundDto> getRecentRefundsByStatuses(List<PaymentTransactionStatus> statuses, int limit);

    List<PaymentRefundDto> getRefundsByCreatedBy(Long userId);

    PaymentRefundDto updateRefundStatus(UUID id, PaymentTransactionStatus status, String reason);

    PaymentRefundDto markAsProcessed(UUID id, String externalRefundId, Map<String, Object> gatewayResponse);

    PaymentRefundDto markAsFailed(UUID id, String errorCode, String errorMessage);

    PaymentRefundDto retryRefund(UUID id);

    void cancelRefund(UUID id, String reason);

    boolean existsByRefundCode(String refundCode);

    boolean existsByExternalRefundId(String externalRefundId);

    boolean canRefund(UUID paymentTransactionId, BigDecimal refundAmount);

    BigDecimal getAvailableRefundAmount(UUID paymentTransactionId);

    Long countByStatus(PaymentTransactionStatus status);

    Long countByTransaction(UUID paymentTransactionId);

    BigDecimal sumRefundAmountByStatusAndCurrency(PaymentTransactionStatus status, String currency);

    BigDecimal sumRefundAmountByTransactionAndStatus(UUID transactionId, PaymentTransactionStatus status);

    Map<String, Long> getRefundCountByStatus();

    Map<String, BigDecimal> getRefundAmountByCurrency();

    void processStaleRefunds();

    void syncRefundStatusWithGateway(UUID id);
}