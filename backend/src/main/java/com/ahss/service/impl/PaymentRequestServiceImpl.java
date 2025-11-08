package com.ahss.service.impl;

import com.ahss.dto.request.CreatePaymentRequestDto;
import com.ahss.dto.request.UpdatePaymentRequestDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentSummaryDto;
import com.ahss.entity.PaymentRequest;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.enums.PaymentMethodType;
import com.ahss.repository.PaymentRequestRepository;
import com.ahss.service.PaymentRequestService;
import com.ahss.service.PaymentAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentRequestServiceImpl implements PaymentRequestService {

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private PaymentAuditLogService auditLogService;

    @Override
    public PaymentRequestDto createPaymentRequest(CreatePaymentRequestDto createDto) {
        PaymentRequest paymentRequest = convertToEntity(createDto);
        PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);
        
        // Log the creation
        auditLogService.logPaymentRequestAction(
            savedRequest.getId(),
            "CREATED",
            null,
            savedRequest.getStatus().toString(),
            "Payment request created",
            null,
            savedRequest.getCreatedBy(),
            null,
            null
        );
        
        return convertToDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentRequestDto> getPaymentRequestById(UUID id) {
        return paymentRequestRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentRequestDto> getPaymentRequestByCode(String requestCode) {
        return paymentRequestRepository.findByRequestCode(requestCode)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentRequestDto> getPaymentRequestByToken(String paymentToken) {
        return paymentRequestRepository.findByPaymentToken(paymentToken)
                .map(this::convertToDto);
    }

    @Override
    public PaymentRequestDto updatePaymentRequest(UUID id, UpdatePaymentRequestDto updateDto) {
        PaymentRequest existingRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment request not found with id: " + id));

        String oldStatus = existingRequest.getStatus().toString();
        updateEntityFromDto(existingRequest, updateDto);
        PaymentRequest updatedRequest = paymentRequestRepository.save(existingRequest);

        // Log the update
        auditLogService.logPaymentRequestAction(
            updatedRequest.getId(),
            "UPDATED",
            oldStatus,
            updatedRequest.getStatus().toString(),
            "Payment request updated",
            null,
            updatedRequest.getUpdatedBy(),
            null,
            null
        );

        return convertToDto(updatedRequest);
    }

    @Override
    public void deletePaymentRequest(UUID id) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment request not found with id: " + id));

        // Log the deletion
        auditLogService.logPaymentRequestAction(
            id,
            "DELETED",
            paymentRequest.getStatus().toString(),
            null,
            "Payment request deleted",
            null,
            null,
            null,
            null
        );

        paymentRequestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getAllPaymentRequests(Pageable pageable) {
        return paymentRequestRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getPaymentRequestsByStatus(PaymentRequestStatus status, Pageable pageable) {
        return paymentRequestRepository.findByStatus(status, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getPaymentRequestsByTenant(Long tenantId, Pageable pageable) {
        return paymentRequestRepository.findByTenantId(tenantId, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getPaymentRequestsByPayerEmail(String payerEmail, Pageable pageable) {
        return paymentRequestRepository.findByPayerEmail(payerEmail, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getPaymentRequestsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<PaymentRequest> requests = paymentRequestRepository.findByCreatedAtBetween(startDate, endDate);
        return convertListToPage(requests, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getPaymentRequestsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        List<PaymentRequest> requests = paymentRequestRepository.findByAmountBetween(minAmount, maxAmount);
        return convertListToPage(requests, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> getPaymentRequestsByCurrency(String currency, Pageable pageable) {
        List<PaymentRequest> requests = paymentRequestRepository.findByCurrency(currency);
        return convertListToPage(requests, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRequestDto> searchPaymentRequests(String searchTerm, Pageable pageable) {
        return paymentRequestRepository.searchPaymentRequests(searchTerm, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRequestDto> getExpiredPaymentRequests() {
        List<PaymentRequestStatus> expirableStatuses = List.of(PaymentRequestStatus.PENDING, PaymentRequestStatus.DRAFT);
        return paymentRequestRepository.findExpiredPaymentRequests(LocalDateTime.now(), expirableStatuses)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRequestDto> getRecentPaymentRequestsByStatus(PaymentRequestStatus status, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return paymentRequestRepository.findRecentByStatuses(List.of(status), pageable)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentRequestDto cancelPaymentRequest(UUID id, String reason) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment request not found with id: " + id));

        String oldStatus = paymentRequest.getStatus().toString();
        paymentRequest.setStatus(PaymentRequestStatus.CANCELLED);
        PaymentRequest updatedRequest = paymentRequestRepository.save(paymentRequest);

        // Log the cancellation
        auditLogService.logPaymentRequestAction(
            id,
            "CANCELLED",
            oldStatus,
            PaymentRequestStatus.CANCELLED.toString(),
            "Payment request cancelled: " + reason,
            null,
            null,
            null,
            null
        );

        return convertToDto(updatedRequest);
    }

    @Override
    public PaymentRequestDto expirePaymentRequest(UUID id) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment request not found with id: " + id));

        String oldStatus = paymentRequest.getStatus().toString();
        paymentRequest.setStatus(PaymentRequestStatus.CANCELLED); // Using CANCELLED as there's no EXPIRED
        PaymentRequest updatedRequest = paymentRequestRepository.save(paymentRequest);

        // Log the expiration
        auditLogService.logPaymentRequestAction(
            id,
            "EXPIRED",
            oldStatus,
            PaymentRequestStatus.CANCELLED.toString(),
            "Payment request expired",
            null,
            null,
            null,
            null
        );

        return convertToDto(updatedRequest);
    }

    @Override
    public PaymentRequestDto markAsPaid(UUID id, LocalDateTime paidAt) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment request not found with id: " + id));

        String oldStatus = paymentRequest.getStatus().toString();
        paymentRequest.setStatus(PaymentRequestStatus.COMPLETED);
        paymentRequest.setPaidAt(paidAt);
        PaymentRequest updatedRequest = paymentRequestRepository.save(paymentRequest);

        // Log the payment completion
        auditLogService.logPaymentRequestAction(
            id,
            "PAID",
            oldStatus,
            PaymentRequestStatus.COMPLETED.toString(),
            "Payment request marked as paid",
            null,
            null,
            null,
            null
        );

        return convertToDto(updatedRequest);
    }

    @Override
    public PaymentRequestDto updateStatus(UUID id, PaymentRequestStatus newStatus, String reason) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment request not found with id: " + id));

        String oldStatus = paymentRequest.getStatus() != null ? paymentRequest.getStatus().toString() : null;
        paymentRequest.setStatus(newStatus);
        PaymentRequest updatedRequest = paymentRequestRepository.save(paymentRequest);

        auditLogService.logPaymentRequestAction(
            id,
            "STATUS_UPDATED",
            oldStatus,
            newStatus.toString(),
            reason != null ? reason : "Status updated by orchestrator",
            null,
            null,
            null,
            null
        );

        return convertToDto(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRequestCode(String requestCode) {
        return paymentRequestRepository.existsByRequestCode(requestCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPaymentToken(String paymentToken) {
        return paymentRequestRepository.existsByPaymentToken(paymentToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByStatus(PaymentRequestStatus status) {
        return paymentRequestRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByTenantAndStatus(Long tenantId, PaymentRequestStatus status) {
        return paymentRequestRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumAmountByStatusAndCurrency(PaymentRequestStatus status, String currency) {
        return paymentRequestRepository.sumAmountByStatusAndCurrency(status, currency);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryDto getPaymentSummary(Long tenantId) {
        PaymentSummaryDto summary = new PaymentSummaryDto();
        
        // Set payment request counts - using simplified approach
        summary.setTotalPaymentRequests(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.PENDING) +
                                       paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.COMPLETED) +
                                       paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.FAILED));
        summary.setPendingPaymentRequests(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.PENDING));
        summary.setCompletedPaymentRequests(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.COMPLETED));
        summary.setFailedPaymentRequests(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.FAILED));
        
        // Set amounts - using simplified approach with USD currency
        summary.setTotalAmount(paymentRequestRepository.sumAmountByStatusAndCurrency(PaymentRequestStatus.COMPLETED, "USD"));
        summary.setPendingAmount(paymentRequestRepository.sumAmountByStatusAndCurrency(PaymentRequestStatus.PENDING, "USD"));
        summary.setCompletedAmount(paymentRequestRepository.sumAmountByStatusAndCurrency(PaymentRequestStatus.COMPLETED, "USD"));
        
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryDto getPaymentSummaryBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation would be similar to getPaymentSummary but with date filters
        return getPaymentSummary(tenantId); // Simplified for now
    }

    @Override
    public void processExpiredPaymentRequests() {
        List<PaymentRequestStatus> expirableStatuses = List.of(PaymentRequestStatus.PENDING, PaymentRequestStatus.DRAFT);
        List<PaymentRequest> expiredRequests = paymentRequestRepository.findExpiredPaymentRequests(LocalDateTime.now(), expirableStatuses);
        for (PaymentRequest request : expiredRequests) {
            if (request.getStatus() == PaymentRequestStatus.PENDING) {
                expirePaymentRequest(request.getId());
            }
        }
    }

    private Page<PaymentRequestDto> convertListToPage(List<PaymentRequest> requests, Pageable pageable) {
        List<PaymentRequestDto> dtos = requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        if (start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }
        
        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    private PaymentRequestDto convertToDto(PaymentRequest entity) {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setId(entity.getId());
        dto.setRequestCode(entity.getRequestCode());
        dto.setPaymentToken(entity.getPaymentToken());
        dto.setTitle(entity.getTitle());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setPayerName(entity.getPayerName());
        dto.setPayerEmail(entity.getPayerEmail());
        dto.setPayerPhone(entity.getPayerPhone());
        dto.setAllowedPaymentMethods(Arrays.asList(entity.getAllowedPaymentMethods()));
        dto.setPreSelectedPaymentMethod(entity.getPreSelectedPaymentMethod());
        dto.setStatus(entity.getStatus());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setPaidAt(entity.getPaidAt());
        dto.setTenantId(entity.getTenantId());
        dto.setMetadata(entity.getMetadata());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().toString() : null);
        dto.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().toString() : null);
        return dto;
    }

    private PaymentRequest convertToEntity(CreatePaymentRequestDto dto) {
        PaymentRequest entity = new PaymentRequest();
        entity.setTitle(dto.getTitle());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setPayerName(dto.getPayerName());
        entity.setPayerEmail(dto.getPayerEmail());
        entity.setPayerPhone(dto.getPayerPhone());
        entity.setAllowedPaymentMethods(dto.getAllowedPaymentMethods().toArray(new PaymentMethodType[0]));
        entity.setPreSelectedPaymentMethod(dto.getPreSelectedPaymentMethod());
        entity.setExpiresAt(dto.getExpiresAt());
        entity.setTenantId(dto.getTenantId());
        entity.setMetadata(dto.getMetadata());
        return entity;
    }

    private void updateEntityFromDto(PaymentRequest entity, UpdatePaymentRequestDto dto) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getAmount() != null) {
            entity.setAmount(dto.getAmount());
        }
        if (dto.getCurrency() != null) {
            entity.setCurrency(dto.getCurrency());
        }
        if (dto.getPayerName() != null) {
            entity.setPayerName(dto.getPayerName());
        }
        if (dto.getPayerEmail() != null) {
            entity.setPayerEmail(dto.getPayerEmail());
        }
        if (dto.getPayerPhone() != null) {
            entity.setPayerPhone(dto.getPayerPhone());
        }
        if (dto.getAllowedPaymentMethods() != null) {
            entity.setAllowedPaymentMethods(dto.getAllowedPaymentMethods().toArray(new PaymentMethodType[0]));
        }
        if (dto.getPreSelectedPaymentMethod() != null) {
            entity.setPreSelectedPaymentMethod(dto.getPreSelectedPaymentMethod());
        }
        if (dto.getExpiresAt() != null) {
            entity.setExpiresAt(dto.getExpiresAt());
        }
        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }
    }
}