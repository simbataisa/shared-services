package com.ahss.service;

import com.ahss.dto.request.CreatePaymentRequestDto;
import com.ahss.dto.request.UpdatePaymentRequestDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentSummaryDto;
import com.ahss.enums.PaymentRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRequestService {

    PaymentRequestDto createPaymentRequest(CreatePaymentRequestDto createDto);

    Optional<PaymentRequestDto> getPaymentRequestById(Long id);

    Optional<PaymentRequestDto> getPaymentRequestByCode(String requestCode);

    Optional<PaymentRequestDto> getPaymentRequestByToken(String paymentToken);

    PaymentRequestDto updatePaymentRequest(Long id, UpdatePaymentRequestDto updateDto);

    void deletePaymentRequest(Long id);

    Page<PaymentRequestDto> getAllPaymentRequests(Pageable pageable);

    Page<PaymentRequestDto> getPaymentRequestsByStatus(PaymentRequestStatus status, Pageable pageable);

    Page<PaymentRequestDto> getPaymentRequestsByTenant(Long tenantId, Pageable pageable);

    Page<PaymentRequestDto> getPaymentRequestsByPayerEmail(String payerEmail, Pageable pageable);

    Page<PaymentRequestDto> getPaymentRequestsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PaymentRequestDto> getPaymentRequestsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    Page<PaymentRequestDto> getPaymentRequestsByCurrency(String currency, Pageable pageable);

    Page<PaymentRequestDto> searchPaymentRequests(String searchTerm, Pageable pageable);

    List<PaymentRequestDto> getExpiredPaymentRequests();

    List<PaymentRequestDto> getRecentPaymentRequestsByStatus(PaymentRequestStatus status, int limit);

    PaymentRequestDto cancelPaymentRequest(Long id, String reason);

    PaymentRequestDto expirePaymentRequest(Long id);

    PaymentRequestDto markAsPaid(Long id, LocalDateTime paidAt);

    boolean existsByRequestCode(String requestCode);

    boolean existsByPaymentToken(String paymentToken);

    Long countByStatus(PaymentRequestStatus status);

    Long countByTenantAndStatus(Long tenantId, PaymentRequestStatus status);

    BigDecimal sumAmountByStatusAndCurrency(PaymentRequestStatus status, String currency);

    PaymentSummaryDto getPaymentSummary(Long tenantId);

    PaymentSummaryDto getPaymentSummaryBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);

    void processExpiredPaymentRequests();
}