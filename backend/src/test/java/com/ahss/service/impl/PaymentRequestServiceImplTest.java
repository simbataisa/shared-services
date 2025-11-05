package com.ahss.service.impl;

import com.ahss.dto.request.CreatePaymentRequestDto;
import com.ahss.dto.request.UpdatePaymentRequestDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentSummaryDto;
import com.ahss.entity.PaymentRequest;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.enums.PaymentMethodType;
import com.ahss.repository.PaymentRequestRepository;
import com.ahss.service.PaymentAuditLogService;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("Payment Lifecycle")
@Feature("Payment Requests")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PaymentRequestServiceImpl.class)
public class PaymentRequestServiceImplTest {

    @MockBean
    private PaymentRequestRepository paymentRequestRepository;
    @MockBean
    private PaymentAuditLogService auditLogService;

    @Autowired
    private PaymentRequestServiceImpl service;

    @Test
    @Story("Create payment request logs audit and returns DTO")
    @Severity(SeverityLevel.CRITICAL)
    void createPaymentRequest_logsAudit_andReturnsDto() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto();
        dto.setTitle("Test Payment");
        dto.setAmount(new BigDecimal("123.45"));
        dto.setCurrency("USD");
        dto.setTenantId(1L);
        dto.setAllowedPaymentMethods(java.util.List.of(PaymentMethodType.CREDIT_CARD));

        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> {
            PaymentRequest pr = inv.getArgument(0);
            pr.setId(UUID.randomUUID());
            pr.setStatus(PaymentRequestStatus.PENDING);
            pr.setCreatedBy(1L);
            return pr;
        });

        PaymentRequestDto result = service.createPaymentRequest(dto);
        assertEquals("Test Payment", result.getTitle());
        assertEquals(new BigDecimal("123.45"), result.getAmount());
        assertEquals(PaymentRequestStatus.PENDING, result.getStatus());
        verify(auditLogService).logPaymentRequestAction(any(UUID.class), eq("CREATED"), isNull(), eq(PaymentRequestStatus.PENDING.toString()), anyString(), isNull(), any(), isNull(), isNull());
    }

    @Test
    @Story("Update payment request logs and maps DTO")
    @Severity(SeverityLevel.NORMAL)
    void updatePaymentRequest_logsUpdate_andMapsDto() {
        UUID id = UUID.randomUUID();
        PaymentRequest existing = new PaymentRequest();
        existing.setId(id);
        existing.setStatus(PaymentRequestStatus.DRAFT);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> (PaymentRequest) inv.getArgument(0));

        UpdatePaymentRequestDto updateDto = new UpdatePaymentRequestDto();
        updateDto.setTitle("Updated Title");
        updateDto.setAmount(new BigDecimal("200.00"));

        PaymentRequestDto result = service.updatePaymentRequest(id, updateDto);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
        verify(auditLogService).logPaymentRequestAction(eq(id), eq("UPDATED"), eq(PaymentRequestStatus.DRAFT.toString()), anyString(), anyString(), isNull(), any(), isNull(), isNull());
    }

    @Test
    @Story("Delete payment request logs and deletes")
    @Severity(SeverityLevel.CRITICAL)
    void deletePaymentRequest_logsAndDeletes() {
        UUID id = UUID.randomUUID();
        PaymentRequest pr = new PaymentRequest();
        pr.setId(id);
        pr.setStatus(PaymentRequestStatus.PENDING);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(pr));

        service.deletePaymentRequest(id);
        verify(auditLogService).logPaymentRequestAction(eq(id), eq("DELETED"), eq(PaymentRequestStatus.PENDING.toString()), isNull(), anyString(), isNull(), isNull(), isNull(), isNull());
        verify(paymentRequestRepository).deleteById(id);
    }

    @Test
    @Story("Cancel payment request sets CANCELLED and logs")
    @Severity(SeverityLevel.NORMAL)
    void cancelPaymentRequest_setsCancelled_andLogs() {
        UUID id = UUID.randomUUID();
        PaymentRequest pr = new PaymentRequest();
        pr.setId(id);
        pr.setStatus(PaymentRequestStatus.PENDING);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(pr));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> (PaymentRequest) inv.getArgument(0));

        PaymentRequestDto result = service.cancelPaymentRequest(id, "No longer needed");
        assertEquals(PaymentRequestStatus.CANCELLED, result.getStatus());
        verify(auditLogService).logPaymentRequestAction(eq(id), eq("CANCELLED"), eq(PaymentRequestStatus.PENDING.toString()), eq(PaymentRequestStatus.CANCELLED.toString()), contains("cancelled"), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @Story("Created between converts list to page")
    @Severity(SeverityLevel.TRIVIAL)
    void getPaymentRequestsCreatedBetween_convertsToPage() {
        PaymentRequest pr1 = new PaymentRequest();
        PaymentRequest pr2 = new PaymentRequest();
        when(paymentRequestRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(pr1, pr2));

        Page<PaymentRequestDto> page = service.getPaymentRequestsCreatedBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now(), PageRequest.of(0, 1));
        assertEquals(1, page.getSize());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    @Story("Expire payment request sets CANCELLED and logs EXPIRED")
    @Severity(SeverityLevel.NORMAL)
    void expirePaymentRequest_setsCancelled_andLogs() {
        UUID id = UUID.randomUUID();
        PaymentRequest pr = new PaymentRequest();
        pr.setId(id);
        pr.setStatus(PaymentRequestStatus.PENDING);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(pr));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> (PaymentRequest) inv.getArgument(0));

        PaymentRequestDto result = service.expirePaymentRequest(id);
        assertEquals(PaymentRequestStatus.CANCELLED, result.getStatus());
        verify(auditLogService).logPaymentRequestAction(eq(id), eq("EXPIRED"), anyString(), eq(PaymentRequestStatus.CANCELLED.toString()), contains("expired"), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @Story("Mark as paid sets COMPLETED and logs PAID")
    @Severity(SeverityLevel.NORMAL)
    void markAsPaid_setsCompleted_andLogs() {
        UUID id = UUID.randomUUID();
        PaymentRequest pr = new PaymentRequest();
        pr.setId(id);
        pr.setStatus(PaymentRequestStatus.PENDING);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(pr));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> (PaymentRequest) inv.getArgument(0));

        LocalDateTime paidAt = LocalDateTime.now();
        PaymentRequestDto result = service.markAsPaid(id, paidAt);
        assertEquals(PaymentRequestStatus.COMPLETED, result.getStatus());
        assertEquals(paidAt, result.getPaidAt());
        verify(auditLogService).logPaymentRequestAction(eq(id), eq("PAID"), anyString(), eq(PaymentRequestStatus.COMPLETED.toString()), contains("marked as paid"), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @Story("Payment summary aggregates counts and amounts")
    @Severity(SeverityLevel.NORMAL)
    void getPaymentSummary_computesValues() {
        Long tenantId = 7L;
        when(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.PENDING)).thenReturn(2L);
        when(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.COMPLETED)).thenReturn(3L);
        when(paymentRequestRepository.countByTenantIdAndStatus(tenantId, PaymentRequestStatus.FAILED)).thenReturn(1L);

        when(paymentRequestRepository.sumAmountByStatusAndCurrency(PaymentRequestStatus.COMPLETED, "USD"))
                .thenReturn(new BigDecimal("100.00"));
        when(paymentRequestRepository.sumAmountByStatusAndCurrency(PaymentRequestStatus.PENDING, "USD"))
                .thenReturn(new BigDecimal("10.00"));

        PaymentSummaryDto summary = service.getPaymentSummary(tenantId);
        assertEquals(6L, summary.getTotalPaymentRequests());
        assertEquals(2L, summary.getPendingPaymentRequests());
        assertEquals(3L, summary.getCompletedPaymentRequests());
        assertEquals(1L, summary.getFailedPaymentRequests());
        assertEquals(new BigDecimal("100.00"), summary.getTotalAmount());
        assertEquals(new BigDecimal("10.00"), summary.getPendingAmount());
        assertEquals(new BigDecimal("100.00"), summary.getCompletedAmount());
    }

    @Test
    @Story("Exists and counts delegate to repository")
    @Severity(SeverityLevel.TRIVIAL)
    void existsAndCounts_delegate() {
        when(paymentRequestRepository.existsByRequestCode("REQ-1")).thenReturn(true);
        when(paymentRequestRepository.existsByPaymentToken("tok-1")).thenReturn(true);
        when(paymentRequestRepository.countByStatus(PaymentRequestStatus.PENDING)).thenReturn(7L);
        when(paymentRequestRepository.countByTenantIdAndStatus(5L, PaymentRequestStatus.COMPLETED)).thenReturn(4L);
        when(paymentRequestRepository.sumAmountByStatusAndCurrency(PaymentRequestStatus.COMPLETED, "USD"))
                .thenReturn(new BigDecimal("250.00"));

        assertTrue(service.existsByRequestCode("REQ-1"));
        assertTrue(service.existsByPaymentToken("tok-1"));
        assertEquals(7L, service.countByStatus(PaymentRequestStatus.PENDING));
        assertEquals(4L, service.countByTenantAndStatus(5L, PaymentRequestStatus.COMPLETED));
        assertEquals(new BigDecimal("250.00"), service.sumAmountByStatusAndCurrency(PaymentRequestStatus.COMPLETED, "USD"));
    }

    @Test
    @Story("Process expired payment requests only expires PENDING ones")
    @Severity(SeverityLevel.NORMAL)
    void processExpiredPaymentRequests_expiresOnlyPending() {
        PaymentRequest pending = new PaymentRequest();
        UUID id1 = UUID.randomUUID();
        pending.setId(id1);
        pending.setStatus(PaymentRequestStatus.PENDING);

        PaymentRequest draft = new PaymentRequest();
        UUID id2 = UUID.randomUUID();
        draft.setId(id2);
        draft.setStatus(PaymentRequestStatus.DRAFT);

        when(paymentRequestRepository.findExpiredPaymentRequests(any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(pending, draft));
        when(paymentRequestRepository.findById(id1)).thenReturn(java.util.Optional.of(pending));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processExpiredPaymentRequests();

        // Verify only the PENDING request was expired/logged
        verify(auditLogService, times(1)).logPaymentRequestAction(eq(id1), eq("EXPIRED"), anyString(), eq(PaymentRequestStatus.CANCELLED.toString()), anyString(), isNull(), isNull(), isNull(), isNull());
        verify(paymentRequestRepository, times(1)).save(any(PaymentRequest.class));
    }

    @Test
    @Story("Find by id returns Optional and maps to DTO")
    @Severity(SeverityLevel.NORMAL)
    void getPaymentRequestById_present_and_empty() {
        UUID id = UUID.randomUUID();
        PaymentRequest pr = new PaymentRequest();
        pr.setId(id);
        pr.setTitle("Find Me");
        pr.setAllowedPaymentMethods(new PaymentMethodType[]{PaymentMethodType.CREDIT_CARD});
        pr.setPreSelectedPaymentMethod(PaymentMethodType.CREDIT_CARD);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(pr));
        UUID another = UUID.randomUUID();
        when(paymentRequestRepository.findById(another)).thenReturn(java.util.Optional.empty());

        assertTrue(service.getPaymentRequestById(id).isPresent());
        assertEquals("Find Me", service.getPaymentRequestById(id).get().getTitle());
        assertTrue(service.getPaymentRequestById(another).isEmpty());
    }

    @Test
    @Story("Find by code and token returns Optional present/empty")
    @Severity(SeverityLevel.TRIVIAL)
    void getPaymentRequestByCode_andToken_present_and_empty() {
        PaymentRequest byCode = new PaymentRequest();
        byCode.setId(UUID.randomUUID());
        byCode.setRequestCode("REQ-123");
        PaymentRequest byToken = new PaymentRequest();
        byToken.setId(UUID.randomUUID());
        byToken.setPaymentToken("tok-abc");

        when(paymentRequestRepository.findByRequestCode("REQ-123")).thenReturn(java.util.Optional.of(byCode));
        when(paymentRequestRepository.findByRequestCode("REQ-404")).thenReturn(java.util.Optional.empty());
        when(paymentRequestRepository.findByPaymentToken("tok-abc")).thenReturn(java.util.Optional.of(byToken));
        when(paymentRequestRepository.findByPaymentToken("tok-missing")).thenReturn(java.util.Optional.empty());

        assertTrue(service.getPaymentRequestByCode("REQ-123").isPresent());
        assertTrue(service.getPaymentRequestByCode("REQ-404").isEmpty());
        assertTrue(service.getPaymentRequestByToken("tok-abc").isPresent());
        assertTrue(service.getPaymentRequestByToken("tok-missing").isEmpty());
    }

    @Test
    @Story("Page mapping for list endpoints")
    @Severity(SeverityLevel.NORMAL)
    void pageable_endpoints_mapEntitiesToDtos() {
        PageRequest pageable = PageRequest.of(0, 2);
        PaymentRequest e = new PaymentRequest();
        e.setId(UUID.randomUUID());
        e.setTitle("Paged");
        e.setAllowedPaymentMethods(new PaymentMethodType[]{PaymentMethodType.CREDIT_CARD});
        e.setPreSelectedPaymentMethod(PaymentMethodType.CREDIT_CARD);

        when(paymentRequestRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(e), pageable, 1));
        when(paymentRequestRepository.findByStatus(PaymentRequestStatus.PENDING, pageable)).thenReturn(new PageImpl<>(List.of(e), pageable, 1));
        when(paymentRequestRepository.findByTenantId(7L, pageable)).thenReturn(new PageImpl<>(List.of(e), pageable, 1));
        when(paymentRequestRepository.findByPayerEmail("a@b.com", pageable)).thenReturn(new PageImpl<>(List.of(e), pageable, 1));
        when(paymentRequestRepository.searchPaymentRequests("test", pageable)).thenReturn(new PageImpl<>(List.of(e), pageable, 1));

        assertEquals(1, service.getAllPaymentRequests(pageable).getTotalElements());
        assertEquals("Paged", service.getPaymentRequestsByStatus(PaymentRequestStatus.PENDING, pageable).getContent().get(0).getTitle());
        assertEquals(1, service.getPaymentRequestsByTenant(7L, pageable).getTotalElements());
        assertEquals(1, service.getPaymentRequestsByPayerEmail("a@b.com", pageable).getTotalElements());
        assertEquals(1, service.searchPaymentRequests("test", pageable).getTotalElements());
    }

    @Test
    @Story("List-to-page conversion for amount range and currency")
    @Severity(SeverityLevel.TRIVIAL)
    void amountRange_and_currency_convertToPage() {
        PaymentRequest a = new PaymentRequest();
        a.setId(UUID.randomUUID());
        PaymentRequest b = new PaymentRequest();
        b.setId(UUID.randomUUID());
        when(paymentRequestRepository.findByAmountBetween(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(List.of(a, b));
        when(paymentRequestRepository.findByCurrency("USD"))
                .thenReturn(List.of(a));

        Page<PaymentRequestDto> byAmount = service.getPaymentRequestsByAmountRange(new BigDecimal("1.00"), new BigDecimal("999.00"), PageRequest.of(0, 1));
        assertEquals(2, byAmount.getTotalElements());
        assertEquals(1, byAmount.getSize());

        Page<PaymentRequestDto> byCurrency = service.getPaymentRequestsByCurrency("USD", PageRequest.of(0, 1));
        assertEquals(1, byCurrency.getTotalElements());
        assertEquals(1, byCurrency.getContent().size());
    }

    @Test
    @Story("List-to-page returns empty page when offset exceeds size")
    @Severity(SeverityLevel.TRIVIAL)
    void convertListToPage_returnsEmpty_whenOffsetBeyond() {
        PaymentRequest only = new PaymentRequest();
        only.setId(UUID.randomUUID());
        when(paymentRequestRepository.findByCurrency("EUR")).thenReturn(List.of(only));

        Page<PaymentRequestDto> page = service.getPaymentRequestsByCurrency("EUR", PageRequest.of(5, 10));
        assertEquals(0, page.getContent().size());
        assertEquals(1, page.getTotalElements());
    }

    @Test
    @Story("Recent and expired lists convert to DTOs")
    @Severity(SeverityLevel.NORMAL)
    void recent_and_expired_requests_listConversions() {
        PaymentRequest r1 = new PaymentRequest();
        r1.setId(UUID.randomUUID());
        PaymentRequest r2 = new PaymentRequest();
        r2.setId(UUID.randomUUID());
        when(paymentRequestRepository.findRecentByStatuses(anyList(), any())).thenReturn(List.of(r1, r2));

        List<PaymentRequestDto> recent = service.getRecentPaymentRequestsByStatus(PaymentRequestStatus.PENDING, 2);
        assertEquals(2, recent.size());

        PaymentRequest e1 = new PaymentRequest();
        e1.setId(UUID.randomUUID());
        e1.setStatus(PaymentRequestStatus.PENDING);
        PaymentRequest e2 = new PaymentRequest();
        e2.setId(UUID.randomUUID());
        e2.setStatus(PaymentRequestStatus.DRAFT);
        when(paymentRequestRepository.findExpiredPaymentRequests(any(LocalDateTime.class), anyList())).thenReturn(List.of(e1, e2));

        List<PaymentRequestDto> expired = service.getExpiredPaymentRequests();
        assertEquals(2, expired.size());
    }

    @Test
    @Story("Update applies all non-null fields")
    @Severity(SeverityLevel.NORMAL)
    void updatePaymentRequest_updatesAllProvidedFields() {
        UUID id = UUID.randomUUID();
        PaymentRequest existing = new PaymentRequest();
        existing.setId(id);
        existing.setStatus(PaymentRequestStatus.DRAFT);
        existing.setAllowedPaymentMethods(new PaymentMethodType[]{PaymentMethodType.CREDIT_CARD});
        existing.setPreSelectedPaymentMethod(PaymentMethodType.CREDIT_CARD);
        when(paymentRequestRepository.findById(id)).thenReturn(java.util.Optional.of(existing));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> {
            PaymentRequest saved = inv.getArgument(0);
            saved.setUpdatedBy(2L);
            return saved;
        });

        UpdatePaymentRequestDto updateDto = new UpdatePaymentRequestDto();
        updateDto.setTitle("All Fields");
        updateDto.setAmount(new BigDecimal("300.00"));
        updateDto.setCurrency("EUR");
        updateDto.setPayerName("John");
        updateDto.setPayerEmail("john@example.com");
        updateDto.setPayerPhone("123");
        updateDto.setAllowedPaymentMethods(List.of(PaymentMethodType.CREDIT_CARD, PaymentMethodType.PAYPAL));
        updateDto.setPreSelectedPaymentMethod(PaymentMethodType.PAYPAL);
        updateDto.setExpiresAt(LocalDateTime.now().plusDays(1));
        updateDto.setMetadata(Map.of("k", "v"));

        PaymentRequestDto result = service.updatePaymentRequest(id, updateDto);
        assertEquals("All Fields", result.getTitle());
        assertEquals(new BigDecimal("300.00"), result.getAmount());
        assertEquals("EUR", result.getCurrency());
        assertEquals("John", result.getPayerName());
        assertEquals("john@example.com", result.getPayerEmail());
        assertEquals("123", result.getPayerPhone());
        assertEquals(2, result.getAllowedPaymentMethods().size());
        assertEquals(PaymentMethodType.PAYPAL, result.getPreSelectedPaymentMethod());
        assertNotNull(result.getExpiresAt());
        assertEquals("2", result.getUpdatedBy());
    }
}