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

public interface PaymentRefundService {

    PaymentRefundDto createRefund(CreateRefundDto createDto);

    Optional<PaymentRefundDto> getRefundById(Long id);

    Optional<PaymentRefundDto> getRefundByCode(String refundCode);

    Optional<PaymentRefundDto> getRefundByExternalId(String externalRefundId);

    Page<PaymentRefundDto> getAllRefunds(Pageable pageable);

    Page<PaymentRefundDto> getRefundsByTransaction(Long paymentTransactionId, Pageable pageable);

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

    List<PaymentRefundDto> getSuccessfulRefundsByTransaction(Long paymentTransactionId);

    List<PaymentRefundDto> getRecentRefundsByStatuses(List<PaymentTransactionStatus> statuses, int limit);

    List<PaymentRefundDto> getRefundsByCreatedBy(Long userId);

    PaymentRefundDto updateRefundStatus(Long id, PaymentTransactionStatus status, String reason);

    PaymentRefundDto markAsProcessed(Long id, String externalRefundId, Map<String, Object> gatewayResponse);

    PaymentRefundDto markAsFailed(Long id, String errorCode, String errorMessage);

    PaymentRefundDto retryRefund(Long id);

    void cancelRefund(Long id, String reason);

    boolean existsByRefundCode(String refundCode);

    boolean existsByExternalRefundId(String externalRefundId);

    boolean canRefund(Long paymentTransactionId, BigDecimal refundAmount);

    BigDecimal getAvailableRefundAmount(Long paymentTransactionId);

    Long countByStatus(PaymentTransactionStatus status);

    Long countByTransaction(Long paymentTransactionId);

    BigDecimal sumRefundAmountByStatusAndCurrency(PaymentTransactionStatus status, String currency);

    BigDecimal sumRefundAmountByTransactionAndStatus(Long transactionId, PaymentTransactionStatus status);

    Map<String, Long> getRefundCountByStatus();

    Map<String, BigDecimal> getRefundAmountByCurrency();

    void processStaleRefunds();

    void syncRefundStatusWithGateway(Long id);
}