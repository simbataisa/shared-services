package com.ahss.service.impl;

import com.ahss.dto.request.CreateRefundDto;
import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.entity.PaymentRefund;
import com.ahss.entity.PaymentTransaction;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.integration.PaymentIntegratorFactory;
import com.ahss.repository.PaymentRefundRepository;
import com.ahss.repository.PaymentRequestRepository;
import com.ahss.repository.PaymentTransactionRepository;
import com.ahss.security.UserPrincipal;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRequestService;
import io.qameta.allure.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("Payment Lifecycle")
@Feature("Payment Refunds")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PaymentRefundServiceImpl.class)
public class PaymentRefundServiceImplTest {

    @MockBean
    private PaymentRefundRepository refundRepository;
    @MockBean
    private PaymentTransactionRepository transactionRepository;
    @MockBean
    private PaymentRequestRepository paymentRequestRepository;
    @MockBean
    private PaymentIntegratorFactory integratorFactory;
    @MockBean
    private PaymentRequestService paymentRequestService;
    @MockBean
    private PaymentAuditLogService paymentAuditLogService;

    @Autowired
    private PaymentRefundServiceImpl service;

    @BeforeEach
    void setUp() {
        // Set up security context with an authenticated user
        UserPrincipal principal = new UserPrincipal(1L, "testuser@example.com");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Story("Create refund sets PENDING and returns DTO")
    @Severity(SeverityLevel.CRITICAL)
    void createRefund_setsPending_andReturnsDto() {
        CreateRefundDto dto = Allure.step(
                "Create a new refund request DTO with random paymentTransactionId, refundAmount 50.00, currency USD, and reason duplicate charge",
                () -> new CreateRefundDto());
        dto.setPaymentTransactionId(UUID.randomUUID());
        dto.setRefundAmount(new BigDecimal("50.00"));
        dto.setCurrency("USD");
        dto.setReason("duplicate charge");

        // Mock original transaction exists with sufficient amount
        PaymentTransaction originalTx = new PaymentTransaction();
        originalTx.setId(dto.getPaymentTransactionId());
        originalTx.setAmount(new BigDecimal("100.00"));
        when(transactionRepository.findById(eq(dto.getPaymentTransactionId())))
            .thenReturn(Optional.of(originalTx));

        // No prior successful refunds
        when(refundRepository.sumRefundAmountByTransactionIdAndStatus(eq(dto.getPaymentTransactionId()),
            eq(PaymentTransactionStatus.SUCCESS))).thenReturn(null);

        Allure.step("Mock refundRepository.save to return a new PaymentRefund with PENDING status",
                () -> when(refundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> {
                    PaymentRefund pr = inv.getArgument(0);
                    pr.setId(UUID.randomUUID());
                    pr.setRefundStatus(PaymentTransactionStatus.PENDING);
                    return pr;
                }));

        Allure.step(
                "Verify service createRefund returns a PaymentRefundDto with refundAmount 50.00, refundStatus PENDING",
                () -> {
                    PaymentRefundDto result = service.createRefund(dto);
                    assertEquals(new BigDecimal("50.00"), result.getRefundAmount());
                    assertEquals(PaymentTransactionStatus.PENDING, result.getRefundStatus());
                });
    }

    @Test
    @Story("Update refund status persists and maps DTO")
    @Severity(SeverityLevel.NORMAL)
    void updateRefundStatus_setsStatus_andReturnsDto() {
        UUID id = UUID.randomUUID();
        PaymentRefund existing = Allure.step("Create a new PaymentRefund with id " + id + " and refundStatus PENDING",
                () -> new PaymentRefund());
        existing.setId(id);
        existing.setRefundStatus(PaymentTransactionStatus.PENDING);
        Allure.step("Mock refundRepository.findById to return the existing PaymentRefund",
                () -> when(refundRepository.findById(id)).thenReturn(Optional.of(existing)));
        Allure.step("Mock refundRepository.save to return the updated PaymentRefund",
                () -> when(refundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> inv.getArgument(0)));

        Allure.step("Verify service updateRefundStatus returns a PaymentRefundDto with refundStatus SUCCESS",
                () -> {
                    PaymentRefundDto result = service.updateRefundStatus(id, PaymentTransactionStatus.SUCCESS,
                            "status update");
                    assertEquals(PaymentTransactionStatus.SUCCESS, result.getRefundStatus());
                });
    }

    @Test
    @Story("Mark as processed sets fields and returns DTO")
    @Severity(SeverityLevel.NORMAL)
    void markAsProcessed_setsFields() {
        UUID id = UUID.randomUUID();
        PaymentRefund existing = Allure.step("Create a new PaymentRefund with id " + id + " and refundStatus PENDING",
                () -> new PaymentRefund());
        existing.setId(id);
        existing.setRefundStatus(PaymentTransactionStatus.PENDING);
        Allure.step("Mock refundRepository.findById to return the existing PaymentRefund",
                () -> when(refundRepository.findById(id)).thenReturn(Optional.of(existing)));
        Allure.step("Mock refundRepository.save to return the updated PaymentRefund",
                () -> when(refundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> inv.getArgument(0)));

        Map<String, Object> gatewayResponse = Map.of("ok", true);
        PaymentRefundDto result = Allure.step("Mark refund as processed with ext-123 and gateway response ok:true",
                () -> service.markAsProcessed(id, "ext-123", gatewayResponse));
        Allure.step(
                "Verify service markAsProcessed returns a PaymentRefundDto with refundStatus SUCCESS, externalRefundId ext-123, and processedAt set",
                () -> {
                    assertEquals(PaymentTransactionStatus.SUCCESS, result.getRefundStatus());
                    assertEquals("ext-123", result.getExternalRefundId());
                    assertNotNull(result.getProcessedAt());
                });
    }

    @Test
    @Story("Retry refund resets status to PENDING")
    @Severity(SeverityLevel.MINOR)
    void retryRefund_resetsToPending() {
        UUID id = UUID.randomUUID();
        PaymentRefund existing = Allure.step("Create a new PaymentRefund with id " + id + " and refundStatus FAILED",
                () -> new PaymentRefund());
        existing.setId(id);
        existing.setRefundStatus(PaymentTransactionStatus.FAILED);
        Allure.step("Mock refundRepository.findById to return the existing PaymentRefund",
                () -> when(refundRepository.findById(id)).thenReturn(Optional.of(existing)));
        Allure.step("Mock refundRepository.save to return the updated PaymentRefund",
                () -> when(refundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> inv.getArgument(0)));

        PaymentRefundDto result = Allure.step("Retry refund with id " + id, () -> service.retryRefund(id));
        Allure.step(
                "Verify service retryRefund returns a PaymentRefundDto with refundStatus PENDING, errorCode null, and errorMessage null",
                () -> {
                    assertEquals(PaymentTransactionStatus.PENDING, result.getRefundStatus());
                    assertNull(result.getErrorCode());
                    assertNull(result.getErrorMessage());
                });
    }

    @Test
    @Story("Can refund compares available amount")
    @Severity(SeverityLevel.CRITICAL)
    void canRefund_comparesAvailableAmount() {
        UUID txId = UUID.randomUUID();
        Allure.step("Mock refundRepository.sumRefundAmountByTransactionIdAndStatus to return 30.00",
                () -> when(refundRepository.sumRefundAmountByTransactionIdAndStatus(eq(txId),
                        eq(PaymentTransactionStatus.SUCCESS)))
                        .thenReturn(new BigDecimal("30.00")));

        // Mock original transaction amount to compute available amount (60 - 30 = 30)
        com.ahss.entity.PaymentTransaction originalTx = new com.ahss.entity.PaymentTransaction();
        originalTx.setId(txId);
        originalTx.setAmount(new BigDecimal("60.00"));
        when(transactionRepository.findById(eq(txId))).thenReturn(java.util.Optional.of(originalTx));

        Allure.step("Verify service canRefund returns true when available amount is sufficient",
                () -> assertTrue(service.canRefund(txId, new BigDecimal("20.00")))); // available 30 >= 20 -> can
        Allure.step("Verify service canRefund returns false when available amount is insufficient",
                () -> assertFalse(service.canRefund(txId, new BigDecimal("31.00")))); // 30 < 31 -> cannot
    }

    @Test
    @Story("Get refunds by tx converts to page")
    @Severity(SeverityLevel.TRIVIAL)
    void getRefundsByTransaction_convertListToPage() {
        UUID txId = UUID.randomUUID();
        PaymentRefund r1 = Allure.step("Create a new PaymentRefund with id " + txId + " and refundStatus SUCCESS",
                () -> new PaymentRefund());
        PaymentRefund r2 = Allure.step("Create a new PaymentRefund with id " + txId + " and refundStatus PENDING",
                () -> new PaymentRefund());
        Allure.step("Mock refundRepository.findByPaymentTransactionId to return the list of PaymentRefund r1 and r2",
                () -> when(refundRepository.findByPaymentTransactionId(eq(txId)))
                        .thenReturn(List.of(r1, r2)));

        Allure.step(
                "Verify service getRefundsByTransaction returns a Page of PaymentRefundDto with size 1 and totalElements 2",
                () -> {
                    Page<PaymentRefundDto> page = service.getRefundsByTransaction(txId, PageRequest.of(0, 1));
                    assertEquals(1, page.getSize());
                    assertEquals(2, page.getTotalElements());
                });
    }
}