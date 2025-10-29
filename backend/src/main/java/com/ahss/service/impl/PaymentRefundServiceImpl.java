package com.ahss.service.impl;

import com.ahss.dto.request.CreateRefundDto;

import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.entity.PaymentRefund;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.repository.PaymentRefundRepository;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRefundService;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentRefundServiceImpl implements PaymentRefundService {

    @Autowired
    private PaymentRefundRepository paymentRefundRepository;

    @Autowired
    private PaymentAuditLogService auditLogService;

    @Override
    public PaymentRefundDto createRefund(CreateRefundDto createDto) {
        PaymentRefund refund = convertToEntity(createDto);
        refund.setRefundStatus(PaymentTransactionStatus.PENDING);
        
        PaymentRefund savedRefund = paymentRefundRepository.save(refund);
        return convertToDto(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentRefundDto> getRefundById(UUID id) {
        return paymentRefundRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentRefundDto> getRefundByCode(String refundCode) {
        return paymentRefundRepository.findByRefundCode(refundCode)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentRefundDto> getRefundByExternalId(String externalRefundId) {
        return paymentRefundRepository.findByExternalRefundId(externalRefundId)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getAllRefunds(Pageable pageable) {
        return paymentRefundRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByTransaction(UUID paymentTransactionId, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByPaymentTransactionId(paymentTransactionId);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByStatus(PaymentTransactionStatus status, Pageable pageable) {
        return paymentRefundRepository.findByRefundStatus(status, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByGateway(String gatewayName, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByGatewayName(gatewayName);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByCreatedAtBetween(startDate, endDate);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsProcessedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByProcessedAtBetween(startDate, endDate);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByRefundAmountBetween(minAmount, maxAmount);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByCurrency(String currency, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByCurrency(currency);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByErrorCode(String errorCode, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByErrorCode(errorCode);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByReason(String reason, Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByReasonContaining(reason);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> searchRefunds(String searchTerm, Pageable pageable) {
        return paymentRefundRepository.searchRefunds(searchTerm, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefundDto> getStaleRefunds(LocalDateTime cutoffTime) {
        List<PaymentRefund> refunds = paymentRefundRepository.findStaleRefunds(PaymentTransactionStatus.PENDING, cutoffTime);
        return refunds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefundDto> getSuccessfulRefundsByTransaction(UUID paymentTransactionId) {
        List<PaymentRefund> refunds = paymentRefundRepository.findSuccessfulRefundsByTransactionId(paymentTransactionId);
        return refunds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefundDto> getRecentRefundsByStatuses(List<PaymentTransactionStatus> statuses, int limit) {
        List<PaymentRefund> refunds = paymentRefundRepository.findRecentByStatuses(statuses, Pageable.ofSize(limit));
        return refunds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefundDto> getRefundsByCreatedBy(Long userId) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByCreatedBy(userId);
        return refunds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentRefundDto updateRefundStatus(UUID id, PaymentTransactionStatus status, String reason) {
        PaymentRefund refund = paymentRefundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment refund not found with id: " + id));

        refund.setRefundStatus(status);
        
        PaymentRefund updatedRefund = paymentRefundRepository.save(refund);
        return convertToDto(updatedRefund);
    }

    @Override
    public PaymentRefundDto markAsProcessed(UUID id, String externalRefundId, Map<String, Object> gatewayResponse) {
        PaymentRefund refund = paymentRefundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment refund not found with id: " + id));

        refund.setRefundStatus(PaymentTransactionStatus.SUCCESS);
        refund.setExternalRefundId(externalRefundId);
        refund.setGatewayResponse(gatewayResponse);
        refund.setProcessedAt(LocalDateTime.now());
        
        PaymentRefund updatedRefund = paymentRefundRepository.save(refund);
        return convertToDto(updatedRefund);
    }

    @Override
    public PaymentRefundDto markAsFailed(UUID id, String errorCode, String errorMessage) {
        PaymentRefund refund = paymentRefundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment refund not found with id: " + id));

        refund.setRefundStatus(PaymentTransactionStatus.FAILED);
        refund.setErrorCode(errorCode);
        refund.setErrorMessage(errorMessage);
        
        PaymentRefund updatedRefund = paymentRefundRepository.save(refund);
        return convertToDto(updatedRefund);
    }

    @Override
    public PaymentRefundDto retryRefund(UUID id) {
        PaymentRefund refund = paymentRefundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment refund not found with id: " + id));

        refund.setRefundStatus(PaymentTransactionStatus.PENDING);
        refund.setErrorCode(null);
        refund.setErrorMessage(null);
        
        PaymentRefund updatedRefund = paymentRefundRepository.save(refund);
        return convertToDto(updatedRefund);
    }

    @Override
    public void cancelRefund(UUID id, String reason) {
        PaymentRefund refund = paymentRefundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment refund not found with id: " + id));

        refund.setRefundStatus(PaymentTransactionStatus.CANCELLED);
        refund.setReason(reason);
        
        paymentRefundRepository.save(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRefundCode(String refundCode) {
        return paymentRefundRepository.existsByRefundCode(refundCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByExternalRefundId(String externalRefundId) {
        return paymentRefundRepository.existsByExternalRefundId(externalRefundId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canRefund(UUID paymentTransactionId, BigDecimal refundAmount) {
        BigDecimal availableAmount = getAvailableRefundAmount(paymentTransactionId);
        return availableAmount.compareTo(refundAmount) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAvailableRefundAmount(UUID paymentTransactionId) {
        BigDecimal totalRefunded = paymentRefundRepository.sumRefundAmountByTransactionIdAndStatus(
                paymentTransactionId, PaymentTransactionStatus.SUCCESS);
        return totalRefunded != null ? totalRefunded : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByStatus(PaymentTransactionStatus status) {
        return paymentRefundRepository.countByRefundStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByTransaction(UUID paymentTransactionId) {
        return paymentRefundRepository.countByPaymentTransactionId(paymentTransactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumRefundAmountByStatusAndCurrency(PaymentTransactionStatus status, String currency) {
        return paymentRefundRepository.sumRefundAmountByStatusAndCurrency(status, currency);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumRefundAmountByTransactionAndStatus(UUID transactionId, PaymentTransactionStatus status) {
        return paymentRefundRepository.sumRefundAmountByTransactionIdAndStatus(transactionId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getRefundCountByStatus() {
        // Implementation would aggregate counts by status
        return Map.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getRefundAmountByCurrency() {
        // Implementation would aggregate amounts by currency
        return Map.of();
    }

    @Override
    public void processStaleRefunds() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<PaymentRefund> staleRefunds = paymentRefundRepository.findStaleRefunds(PaymentTransactionStatus.PENDING, cutoffTime);
        
        for (PaymentRefund refund : staleRefunds) {
            refund.setRefundStatus(PaymentTransactionStatus.FAILED);
            refund.setErrorMessage("Refund timed out");
            paymentRefundRepository.save(refund);
        }
    }

    @Override
    public void syncRefundStatusWithGateway(UUID id) {
        // Implementation would sync with payment gateway
    }

    private Page<PaymentRefundDto> convertListToPage(List<PaymentRefund> refunds, Pageable pageable) {
        List<PaymentRefundDto> dtos = refunds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        if (start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    private PaymentRefundDto convertToDto(PaymentRefund entity) {
        PaymentRefundDto dto = new PaymentRefundDto();
        dto.setId(entity.getId());
        dto.setRefundCode(entity.getRefundCode());
        dto.setPaymentTransactionId(entity.getPaymentTransactionId());
        dto.setRefundAmount(entity.getRefundAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setReason(entity.getReason());
        dto.setRefundStatus(entity.getRefundStatus());
        dto.setExternalRefundId(entity.getExternalRefundId());
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

    private PaymentRefund convertToEntity(CreateRefundDto dto) {
        PaymentRefund entity = new PaymentRefund();
        entity.setPaymentTransactionId(dto.getPaymentTransactionId());
        entity.setRefundAmount(dto.getRefundAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setReason(dto.getReason());
        return entity;
    }
}