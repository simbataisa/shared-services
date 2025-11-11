package com.ahss.service.impl;

import com.ahss.dto.request.CreateRefundDto;
import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.entity.PaymentRefund;
import com.ahss.entity.PaymentRequest;
import com.ahss.entity.PaymentTransaction;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.integration.PaymentIntegrator;
import com.ahss.integration.PaymentIntegratorFactory;
import com.ahss.repository.PaymentRefundRepository;
import com.ahss.repository.PaymentRequestRepository;
import com.ahss.repository.PaymentTransactionRepository;
import com.ahss.exception.BadRequestException;
import com.ahss.exception.ResourceNotFoundException;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRefundService;
import com.ahss.service.PaymentRequestService;
import com.ahss.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentRefundServiceImpl.class);

    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final PaymentIntegratorFactory integratorFactory;
    private final PaymentRequestService paymentRequestService;
    private final PaymentAuditLogService auditLogService;

    public PaymentRefundServiceImpl(
            PaymentRefundRepository paymentRefundRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            PaymentRequestRepository paymentRequestRepository,
            PaymentIntegratorFactory integratorFactory,
            PaymentRequestService paymentRequestService,
            PaymentAuditLogService auditLogService) {
        this.paymentRefundRepository = paymentRefundRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.integratorFactory = integratorFactory;
        this.paymentRequestService = paymentRequestService;
        this.auditLogService = auditLogService;
    }

    @Override
    public PaymentRefundDto createRefund(CreateRefundDto createDto) {
        // Ensure the referenced transaction exists (business validation beyond @Valid)
        PaymentTransaction originalTransaction = paymentTransactionRepository
                .findById(createDto.getPaymentTransactionId())
                .orElseThrow(() -> new BadRequestException(
                        "Original payment transaction not found with id: " + createDto.getPaymentTransactionId()));

        // Validate refund amount does not exceed remaining refundable amount
        if (originalTransaction == null) {
            throw new ResourceNotFoundException(
                    "No original transaction found with " + createDto.getPaymentTransactionId());
        }

        if (originalTransaction.getAmount().compareTo(createDto.getRefundAmount()) < 0) {
            throw new BadRequestException("Refund amount exceeds original transaction amount: requested "
                    + createDto.getRefundAmount() + ", available " + originalTransaction.getAmount());

        }

        BigDecimal alreadyRefunded = paymentRefundRepository
                .sumRefundAmountByTransactionIdAndStatus(originalTransaction.getId(), PaymentTransactionStatus.SUCCESS);
        if (alreadyRefunded == null) {
            alreadyRefunded = BigDecimal.ZERO;
        }
        BigDecimal remainingRefundable = originalTransaction.getAmount().subtract(alreadyRefunded);
        if (remainingRefundable.compareTo(BigDecimal.ZERO) < 0) {
            remainingRefundable = BigDecimal.ZERO;
        }

        if (createDto.getRefundAmount().compareTo(remainingRefundable) > 0) {
            throw new BadRequestException("Refund amount exceeds remaining refundable amount: requested "
                    + createDto.getRefundAmount() + ", available " + remainingRefundable);
        }

        // Optionally, additional business rules like currency match could go here

        PaymentRefund refund = convertToEntity(createDto);
        refund.setRefundStatus(PaymentTransactionStatus.PENDING);

        PaymentRefund savedRefund = paymentRefundRepository.save(refund);
        return convertToDto(savedRefund);
    }

    @Override
    public PaymentRefundDto processRefund(UUID refundId) {
        log.info("Processing refund with ID: {}", refundId);

        // Get the refund record
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new BadRequestException("Payment refund not found with id: " + refundId));

        // Validate refund is in PENDING status
        if (refund.getRefundStatus() != PaymentTransactionStatus.PENDING) {
            throw new BadRequestException(
                    "Refund can only be processed when in PENDING status. Current status: " + refund.getRefundStatus());
        }

        // Get the original payment transaction
        PaymentTransaction originalTransaction = paymentTransactionRepository.findById(refund.getPaymentTransactionId())
                .orElseThrow(() -> new BadRequestException(
                        "Original payment transaction not found with id: " + refund.getPaymentTransactionId()));

        // Get the payment request
        PaymentRequest paymentRequest = paymentRequestRepository.findById(originalTransaction.getPaymentRequestId())
                .orElseThrow(() -> new BadRequestException(
                        "Payment request not found with id: " + originalTransaction.getPaymentRequestId()));

        log.info("Found original transaction: {} for payment request: {}", originalTransaction.getId(),
                paymentRequest.getId());

        // Determine gateway - use refund's gateway if specified, otherwise use
        // transaction's gateway
        String gatewayName = refund.getGatewayName() != null ? refund.getGatewayName()
                : originalTransaction.getGatewayName();
        if (gatewayName == null) {
            throw new RuntimeException("Gateway name not specified in refund or original transaction");
        }

        log.info("Processing refund through gateway: {}", gatewayName);

        // Get the payment integrator
        PaymentIntegrator integrator = integratorFactory.getIntegrator(originalTransaction.getPaymentMethod(),
                gatewayName);

        // Convert entities to DTOs for integrator
        PaymentTransactionDto transactionDto = convertTransactionToDto(originalTransaction);

        try {
            // Call the payment gateway to process the refund
            log.info("Calling gateway to process refund of {} {} for transaction {}",
                    refund.getRefundAmount(), refund.getCurrency(), transactionDto.getId());
            PaymentResponseDto gatewayResponse = integrator.processRefund(transactionDto, refund.getRefundAmount());

            // Update refund with gateway response
            if (gatewayResponse.isSuccess()) {
                refund.setRefundStatus(PaymentTransactionStatus.SUCCESS);
                refund.setExternalRefundId(gatewayResponse.getExternalRefundId());
                refund.setGatewayResponse(gatewayResponse.getGatewayResponse());
                refund.setProcessedAt(LocalDateTime.now());
                log.info("Refund processed successfully. External refund ID: {}",
                        gatewayResponse.getExternalRefundId());
            } else {
                refund.setRefundStatus(PaymentTransactionStatus.FAILED);
                refund.setErrorMessage(gatewayResponse.getMessage());
                refund.setProcessedAt(LocalDateTime.now());
                log.error("Refund processing failed: {}", gatewayResponse.getMessage());
            }

            PaymentRefund savedRefund = paymentRefundRepository.save(refund);

            // If refund was successful, update payment request status
            if (gatewayResponse.isSuccess()) {
                updatePaymentRequestStatusAfterRefund(paymentRequest, refund.getRefundAmount());
            }

            return convertToDto(savedRefund);

        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            refund.setRefundStatus(PaymentTransactionStatus.FAILED);
            refund.setErrorMessage("Error processing refund: " + e.getMessage());
            refund.setProcessedAt(LocalDateTime.now());
            paymentRefundRepository.save(refund);
            throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
        }
    }

    private void updatePaymentRequestStatusAfterRefund(PaymentRequest paymentRequest, BigDecimal refundAmount) {
        // Get all transactions for this payment request
        List<PaymentTransaction> transactions = paymentTransactionRepository
                .findByPaymentRequestId(paymentRequest.getId());

        // Calculate total successful refunds for all transactions of this payment
        // request
        BigDecimal totalRefunded = BigDecimal.ZERO;
        for (PaymentTransaction transaction : transactions) {
            List<PaymentRefund> successfulRefunds = paymentRefundRepository
                    .findSuccessfulRefundsByTransactionId(transaction.getId());
            BigDecimal transactionRefunds = successfulRefunds.stream()
                    .map(PaymentRefund::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalRefunded = totalRefunded.add(transactionRefunds);
        }

        log.info("Total refunded for payment request {}: {} (original amount: {})",
                paymentRequest.getId(), totalRefunded, paymentRequest.getAmount());

        // Determine if it's a full or partial refund
        if (totalRefunded.compareTo(paymentRequest.getAmount()) >= 0) {
            // Full refund
            log.info("Full refund detected, updating payment request status to REFUNDED");
            paymentRequestService.updateStatus(paymentRequest.getId(), PaymentRequestStatus.REFUNDED,
                    "Full refund processed: " + totalRefunded);
        } else {
            // Partial refund
            log.info("Partial refund detected, updating payment request status to PARTIAL_REFUND");
            paymentRequestService.updateStatus(paymentRequest.getId(), PaymentRequestStatus.PARTIAL_REFUND,
                    "Partial refund processed: " + totalRefunded + " of " + paymentRequest.getAmount());
        }
    }

    private PaymentTransactionDto convertTransactionToDto(PaymentTransaction transaction) {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setId(transaction.getId());
        dto.setPaymentRequestId(transaction.getPaymentRequestId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setExternalTransactionId(transaction.getExternalTransactionId());
        dto.setGatewayName(transaction.getGatewayName());
        dto.setGatewayResponse(transaction.getGatewayResponse());
        dto.setProcessedAt(transaction.getProcessedAt());
        dto.setMetadata(transaction.getMetadata());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
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
    public Page<PaymentRefundDto> getRefundsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByCreatedAtBetween(startDate, endDate);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsProcessedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        List<PaymentRefund> refunds = paymentRefundRepository.findByProcessedAtBetween(startDate, endDate);
        return convertListToPage(refunds, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRefundDto> getRefundsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount,
            Pageable pageable) {
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
        List<PaymentRefund> refunds = paymentRefundRepository.findStaleRefunds(PaymentTransactionStatus.PENDING,
                cutoffTime);
        return refunds.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefundDto> getSuccessfulRefundsByTransaction(UUID paymentTransactionId) {
        List<PaymentRefund> refunds = paymentRefundRepository
                .findSuccessfulRefundsByTransactionId(paymentTransactionId);
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
        return refundAmount != null && availableAmount.compareTo(refundAmount) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAvailableRefundAmount(UUID paymentTransactionId) {
        BigDecimal totalRefunded = paymentRefundRepository.sumRefundAmountByTransactionIdAndStatus(
                paymentTransactionId, PaymentTransactionStatus.SUCCESS);
        if (totalRefunded == null) {
            totalRefunded = BigDecimal.ZERO;
        }
        BigDecimal originalAmount = paymentTransactionRepository.findById(paymentTransactionId)
                .map(PaymentTransaction::getAmount)
                .orElse(BigDecimal.ZERO);
        BigDecimal remaining = originalAmount.subtract(totalRefunded);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
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
        List<PaymentRefund> staleRefunds = paymentRefundRepository.findStaleRefunds(PaymentTransactionStatus.PENDING,
                cutoffTime);

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
        dto.setRefundTransactionId(entity.getRefundTransactionId());
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
        entity.setGatewayName(dto.getGatewayName());
        entity.setMetadata(dto.getMetadata());

        // Set createdBy from security context
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User must be authenticated to create a refund"));
        entity.setCreatedBy(currentUserId);

        return entity;
    }
}