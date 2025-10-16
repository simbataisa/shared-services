package com.ahss.service;

import com.ahss.dto.response.PaymentAuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentAuditLogService {

    PaymentAuditLogDto logPaymentRequestAction(Long paymentRequestId, String action, String oldStatus, 
                                              String newStatus, String description, Map<String, Object> changeDetails,
                                              Long userId, String userAgent, String ipAddress);

    PaymentAuditLogDto logTransactionAction(Long paymentTransactionId, String action, String oldStatus, 
                                           String newStatus, String description, Map<String, Object> changeDetails,
                                           Long userId, String userAgent, String ipAddress);

    PaymentAuditLogDto logRefundAction(Long paymentRefundId, String action, String oldStatus, 
                                      String newStatus, String description, Map<String, Object> changeDetails,
                                      Long userId, String userAgent, String ipAddress);

    Optional<PaymentAuditLogDto> getAuditLogById(Long id);

    Page<PaymentAuditLogDto> getAllAuditLogs(Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByPaymentRequest(Long paymentRequestId, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByTransaction(Long paymentTransactionId, Pageable pageable);

    Page<PaymentAuditLogDto> getAuditLogsByRefund(Long paymentRefundId, Pageable pageable);

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

    Long countByPaymentRequest(Long paymentRequestId);

    Long countByTransaction(Long paymentTransactionId);

    Long countByRefund(Long paymentRefundId);

    List<String> getDistinctActions();

    Map<String, Long> getActionCountBreakdown();

    Map<Long, Long> getUserActionCountBreakdown();

    void cleanupOldAuditLogs(LocalDateTime cutoffDate);
}