package com.ahss.service.impl;

import com.ahss.dto.response.PaymentAuditLogDto;
import com.ahss.entity.PaymentAuditLog;
import com.ahss.repository.PaymentAuditLogRepository;
import com.ahss.service.PaymentAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentAuditLogServiceImpl implements PaymentAuditLogService {

    @Autowired
    private PaymentAuditLogRepository auditLogRepository;

    @Override
    public PaymentAuditLogDto logPaymentRequestAction(UUID paymentRequestId, String action, String oldStatus,
                                                     String newStatus, String description, Map<String, Object> changeDetails,
                                                     Long userId, String userAgent, String ipAddress) {
        PaymentAuditLog auditLog = PaymentAuditLog.createPaymentRequestAudit(
                paymentRequestId, action, oldStatus, newStatus, description, userId);
        auditLog.setChangeDetails(changeDetails);
        auditLog.setUserAgent(userAgent);
        auditLog.setIpAddress(ipAddress);
        
        PaymentAuditLog savedAuditLog = auditLogRepository.save(auditLog);
        return convertToDto(savedAuditLog);
    }

    @Override
    public PaymentAuditLogDto logTransactionAction(UUID paymentTransactionId, String action, String oldStatus,
                                                  String newStatus, String description, Map<String, Object> changeDetails,
                                                  Long userId, String userAgent, String ipAddress) {
        PaymentAuditLog auditLog = PaymentAuditLog.createTransactionAudit(
                null, paymentTransactionId, action, description, userId);
        auditLog.setOldStatus(oldStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setChangeDetails(changeDetails);
        auditLog.setUserAgent(userAgent);
        auditLog.setIpAddress(ipAddress);
        
        PaymentAuditLog savedAuditLog = auditLogRepository.save(auditLog);
        return convertToDto(savedAuditLog);
    }

    @Override
    public PaymentAuditLogDto logRefundAction(UUID paymentRefundId, String action, String oldStatus,
                                             String newStatus, String description, Map<String, Object> changeDetails,
                                             Long userId, String userAgent, String ipAddress) {
        PaymentAuditLog auditLog = PaymentAuditLog.createRefundAudit(
                null, paymentRefundId, action, description, userId);
        auditLog.setOldStatus(oldStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setChangeDetails(changeDetails);
        auditLog.setUserAgent(userAgent);
        auditLog.setIpAddress(ipAddress);
        
        PaymentAuditLog savedAuditLog = auditLogRepository.save(auditLog);
        return convertToDto(savedAuditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentAuditLogDto> getAuditLogById(UUID id) {
        return auditLogRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsByPaymentRequest(UUID paymentRequestId, Pageable pageable) {
        return auditLogRepository.findByPaymentRequestIdOrderByCreatedAtDesc(paymentRequestId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsByTransaction(UUID paymentTransactionId, Pageable pageable) {
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByPaymentTransactionIdOrderByCreatedAtDesc(paymentTransactionId);
        return convertListToPage(auditLogs, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsByRefund(UUID paymentRefundId, Pageable pageable) {
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByPaymentRefundIdOrderByCreatedAtDesc(paymentRefundId);
        return convertListToPage(auditLogs, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> getAuditLogsByIpAddress(String ipAddress, Pageable pageable) {
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByIpAddressOrderByCreatedAtDesc(ipAddress);
        return convertListToPage(auditLogs, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAuditLogDto> searchAuditLogs(String searchTerm, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(searchTerm, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAuditLogDto> getSystemActions() {
        List<PaymentAuditLog> auditLogs = auditLogRepository.findSystemActionsOrderByCreatedAtDesc();
        return auditLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAuditLogDto> getUserActions() {
        List<PaymentAuditLog> auditLogs = auditLogRepository.findUserActionsOrderByCreatedAtDesc();
        return auditLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAuditLogDto> getStatusChanges() {
        List<PaymentAuditLog> auditLogs = auditLogRepository.findStatusChangesOrderByCreatedAtDesc();
        return auditLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByAction(String action) {
        return auditLogRepository.countByAction(action);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByUser(Long userId) {
        return auditLogRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByPaymentRequest(UUID paymentRequestId) {
        return auditLogRepository.countByPaymentRequestId(paymentRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByTransaction(UUID paymentTransactionId) {
        return auditLogRepository.countByPaymentTransactionId(paymentTransactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByRefund(UUID paymentRefundId) {
        return auditLogRepository.countByPaymentRefundId(paymentRefundId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctActions() {
        return auditLogRepository.findDistinctActions();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getActionCountBreakdown() {
        List<String> actions = auditLogRepository.findDistinctActions();
        Map<String, Long> breakdown = new HashMap<>();
        for (String action : actions) {
            Long count = auditLogRepository.countByAction(action);
            breakdown.put(action, count);
        }
        return breakdown;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getUserActionCountBreakdown() {
        // Get all user actions and group by userId
        List<PaymentAuditLog> userActions = auditLogRepository.findUserActionsOrderByCreatedAtDesc();
        Map<Long, Long> breakdown = new HashMap<>();
        for (PaymentAuditLog log : userActions) {
            if (log.getUserId() != null) {
                breakdown.merge(log.getUserId(), 1L, Long::sum);
            }
        }
        return breakdown;
    }

    @Override
    @Transactional
    public void cleanupOldAuditLogs(LocalDateTime cutoffDate) {
        // Find and delete audit logs older than cutoff date
        List<PaymentAuditLog> oldLogs = auditLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt().isBefore(cutoffDate))
                .collect(Collectors.toList());
        
        for (PaymentAuditLog log : oldLogs) {
            auditLogRepository.delete(log);
        }
    }

    private Page<PaymentAuditLogDto> convertListToPage(List<PaymentAuditLog> auditLogs, Pageable pageable) {
        List<PaymentAuditLogDto> dtos = auditLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        if (start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    private PaymentAuditLogDto convertToDto(PaymentAuditLog entity) {
        PaymentAuditLogDto dto = new PaymentAuditLogDto();
        dto.setId(entity.getId());
        dto.setPaymentRequestId(entity.getPaymentRequestId());
        dto.setPaymentTransactionId(entity.getPaymentTransactionId());
        dto.setPaymentRefundId(entity.getPaymentRefundId());
        dto.setAction(entity.getAction());
        dto.setOldStatus(entity.getOldStatus());
        dto.setNewStatus(entity.getNewStatus());
        dto.setDescription(entity.getDescription());
        dto.setChangeDetails(entity.getChangeDetails());
        dto.setUserId(entity.getUserId());
        dto.setUserAgent(entity.getUserAgent());
        dto.setIpAddress(entity.getIpAddress());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}