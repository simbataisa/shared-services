package com.ahss.service;

import com.ahss.dto.response.PaymentAuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PaymentAuditLogService {

    PaymentAuditLogDto logPaymentRequestAction(UUID paymentRequestId, String action, String oldStatus, 
                                              String newStatus, String description, Map<String, Object> changeDetails,
                                              Long userId, String userAgent, String ipAddress);

    PaymentAuditLogDto logTransactionAction(UUID paymentTransactionId, String action, String oldStatus, 
                                           String newStatus, String description, Map<String, Object> changeDetails,
                                           Long userId, String userAgent, String ipAddress);

    PaymentAuditLogDto logRefundAction(UUID paymentRefundId, String action, String oldStatus, 
                                      String newStatus, String description, Map<String, Object> changeDetails,
                                      Long userId, String userAgent, String ipAddress);

    Optional<PaymentAuditLogDto> getAuditLogById(UUID id);

    Page<PaymentAuditLogDto> getAllAuditLogs(Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByPaymentRequest(UUID paymentRequestId, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByTransaction(UUID paymentTransactionId, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByRefund(UUID paymentRefundId, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByAction(String action, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByUser(Long userId, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByIpAddress(String ipAddress, Pageable pageable);

    Page<PaymentAuditLogDto> searchAuditLogs(String searchTerm, Pageable pageable);

    List<PaymentAuditLogDto> getSystemActions();

    List<PaymentAuditLogDto> getUserActions();

    List<PaymentAuditLogDto> getStatusChanges();

    Long countByAction(String action);

    Long countByUser(Long userId);

    Long countByPaymentRequest(UUID paymentRequestId);

    Long countByTransaction(UUID paymentTransactionId);

    Long countByRefund(UUID paymentRefundId);

    List<String> getDistinctActions();

    Map<String, Long> getActionCountBreakdown();

    Map<Long, Long> getUserActionCountBreakdown();

    void cleanupOldAuditLogs(LocalDateTime cutoffDate);
}