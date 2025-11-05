package com.ahss.service.impl;

import com.ahss.dto.response.PaymentAuditLogDto;
import com.ahss.entity.PaymentAuditLog;
import com.ahss.repository.PaymentAuditLogRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("Payment Lifecycle")
@Feature("Audit Logs")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PaymentAuditLogServiceImpl.class)
public class PaymentAuditLogServiceImplTest {

    @MockBean
    private PaymentAuditLogRepository auditLogRepository;

    @Autowired
    private PaymentAuditLogServiceImpl service;

    @Test
    @Story("Log payment request action persists and maps DTO")
    @Severity(SeverityLevel.CRITICAL)
    void logPaymentRequestAction_saves_andReturnsDto() {
        UUID reqId = UUID.randomUUID();
        Map<String, Object> changes = Map.of("field", "value");

        Allure.step("Mock auditLogRepository save to return a new log with random ID and timestamp",
                () -> when(auditLogRepository.save(any(PaymentAuditLog.class))).thenAnswer(inv -> {
                    PaymentAuditLog log = inv.getArgument(0);
                    log.setId(UUID.randomUUID());
                    log.setCreatedAt(LocalDateTime.now());
                    return log;
                }));

        PaymentAuditLogDto dto = Allure.step(
                "Verify service logPaymentRequestAction returns a DTO with expected fields",
                () -> service.logPaymentRequestAction(reqId, "CREATED", null, "PENDING",
                        "created", changes, 1L, "UA", "127.0.0.1"));
        Allure.step("Verify DTO action is CREATED", () -> assertEquals("CREATED", dto.getAction()));
        Allure.step("Verify DTO payment request ID matches input",
                () -> assertEquals(reqId, dto.getPaymentRequestId()));
        Allure.step("Verify DTO new status is PENDING", () -> assertEquals("PENDING", dto.getNewStatus()));
        Allure.step("Verify DTO user agent is UA", () -> assertEquals("UA", dto.getUserAgent()));
        Allure.step("Verify DTO IP address is 127.0.0.1", () -> assertEquals("127.0.0.1", dto.getIpAddress()));
        Allure.step("Verify DTO change details match input", () -> assertEquals(changes, dto.getChangeDetails()));
    }

    @Test
    @Story("Log transaction action persists and maps statuses")
    @Severity(SeverityLevel.NORMAL)
    void logTransactionAction_setsOldNewStatus() {
        UUID txId = UUID.randomUUID();
        Allure.step("Mock auditLogRepository save to return a new log with random ID and timestamp",
                () -> when(auditLogRepository.save(any(PaymentAuditLog.class))).thenAnswer(inv -> {
                    PaymentAuditLog log = inv.getArgument(0);
                    log.setId(UUID.randomUUID());
                    log.setCreatedAt(LocalDateTime.now());
                    return log;
                }));

        PaymentAuditLogDto dto = Allure.step("Verify service logTransactionAction returns a DTO with expected fields",
                () -> service.logTransactionAction(txId, "UPDATED", "PENDING", "SUCCESS",
                        "updated", null, 2L, null, null));
        Allure.step("Verify DTO payment transaction ID matches input",
                () -> assertEquals(txId, dto.getPaymentTransactionId()));
        Allure.step("Verify DTO old status is PENDING", () -> assertEquals("PENDING", dto.getOldStatus()));
        Allure.step("Verify DTO new status is SUCCESS", () -> assertEquals("SUCCESS", dto.getNewStatus()));
    }

    @Test
    @Story("Log refund action persists and maps DTO")
    @Severity(SeverityLevel.NORMAL)
    void logRefundAction_setsFields() {
        UUID refundId = UUID.randomUUID();
        Allure.step("Mock auditLogRepository save to return a new log with random ID and timestamp",
                () -> when(auditLogRepository.save(any(PaymentAuditLog.class))).thenAnswer(inv -> {
                    PaymentAuditLog log = inv.getArgument(0);
                    log.setId(UUID.randomUUID());
                    log.setCreatedAt(LocalDateTime.now());
                    return log;
                }));

        PaymentAuditLogDto dto = Allure.step("Verify service logRefundAction returns a DTO with expected fields",
                () -> service.logRefundAction(refundId, "FAILED", "PENDING", "FAILED",
                        "failed", Map.of("code", "E001"), 3L, "UA2", "10.0.0.1"));
        Allure.step("Verify DTO payment refund ID matches input",
                () -> assertEquals(refundId, dto.getPaymentRefundId()));
        Allure.step("Verify DTO action is FAILED", () -> assertEquals("FAILED", dto.getAction()));
        Allure.step("Verify DTO change details contain code E001",
                () -> assertEquals("E001", ((Map<?, ?>) dto.getChangeDetails()).get("code")));
        Allure.step("Verify DTO old status is PENDING", () -> assertEquals("PENDING", dto.getOldStatus()));
        Allure.step("Verify DTO new status is FAILED", () -> assertEquals("FAILED", dto.getNewStatus()));
        Allure.step("Verify DTO user agent is UA2", () -> assertEquals("UA2", dto.getUserAgent()));
        Allure.step("Verify DTO IP address is 10.0.0.1", () -> assertEquals("10.0.0.1", dto.getIpAddress()));
    }

    @Test
    @Story("Find by id returns mapped DTO")
    @Severity(SeverityLevel.TRIVIAL)
    void getAuditLogById_mapsDto() {
        UUID id = UUID.randomUUID();
        PaymentAuditLog entity = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        entity.setId(id);
        entity.setAction("CREATED");
        Allure.step("Mock auditLogRepository findById to return a new log with random ID and timestamp",
                () -> when(auditLogRepository.findById(id)).thenReturn(Optional.of(entity)));

        Optional<PaymentAuditLogDto> dto = Allure.step(
                "Verify service getAuditLogById returns a DTO with expected fields",
                () -> service.getAuditLogById(id));
        Allure.step("Verify DTO is present", () -> assertTrue(dto.isPresent()));
        Allure.step("Verify DTO action is CREATED", () -> assertEquals("CREATED", dto.get().getAction()));
    }

    @Test
    @Story("List by request returns page")
    @Severity(SeverityLevel.TRIVIAL)
    void getAuditLogsByPaymentRequest_mapsPage() {
        UUID reqId = UUID.randomUUID();
        PaymentAuditLog l1 = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        PaymentAuditLog l2 = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        Allure.step(
                "Mock auditLogRepository findByPaymentRequestIdOrderByCreatedAtDesc to return a new page with 2 logs",
                () -> when(auditLogRepository.findByPaymentRequestIdOrderByCreatedAtDesc(eq(reqId), any()))
                        .thenReturn(new PageImpl<>(List.of(l1, l2))));

        Page<PaymentAuditLogDto> page = Allure.step(
                "Verify service getAuditLogsByPaymentRequest returns a page with 2 elements",
                () -> service.getAuditLogsByPaymentRequest(reqId, PageRequest.of(0, 10)));
        Allure.step("Verify page total elements is 2", () -> assertEquals(2, page.getTotalElements()));
    }

    @Test
    @Story("List by transaction converts list to page")
    @Severity(SeverityLevel.TRIVIAL)
    void getAuditLogsByTransaction_convertListToPage() {
        UUID txId = UUID.randomUUID();
        PaymentAuditLog l1 = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        PaymentAuditLog l2 = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        Allure.step(
                "Mock auditLogRepository findByPaymentTransactionIdOrderByCreatedAtDesc to return a new page with 2 logs",
                () -> when(auditLogRepository.findByPaymentTransactionIdOrderByCreatedAtDesc(txId))
                        .thenReturn(List.of(l1, l2)));

        Page<PaymentAuditLogDto> page = Allure.step(
                "Verify service getAuditLogsByTransaction returns a page with 1 element",
                () -> service.getAuditLogsByTransaction(txId, PageRequest.of(0, 1)));
        Allure.step("Verify page size is 1", () -> assertEquals(1, page.getSize()));
        Allure.step("Verify page total elements is 2", () -> assertEquals(2, page.getTotalElements()));
    }

    @Test
    @Story("Search delegates to repository and maps page")
    @Severity(SeverityLevel.TRIVIAL)
    void searchAuditLogs_delegates() {
        PaymentAuditLog l1 = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        Allure.step("Mock auditLogRepository searchAuditLogs to return a new page with 1 log",
                () -> when(auditLogRepository.searchAuditLogs(eq("term"), any()))
                        .thenReturn(new PageImpl<>(List.of(l1))));
        Page<PaymentAuditLogDto> page = Allure.step("Verify service searchAuditLogs returns a page with 1 element",
                () -> service.searchAuditLogs("term", PageRequest.of(0, 10)));
        Allure.step("Verify page total elements is 1", () -> assertEquals(1, page.getTotalElements()));
    }

    @Test
    @Story("Cleanup deletes old logs")
    @Severity(SeverityLevel.NORMAL)
    void cleanupOldAuditLogs_deletesBeforeCutoff() {
        PaymentAuditLog oldLog = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        Allure.step("Set audit log ID to random UUID", () -> oldLog.setId(UUID.randomUUID()));
        Allure.step("Set audit log createdAt to 10 days ago",
                () -> oldLog.setCreatedAt(LocalDateTime.now().minusDays(10)));
        PaymentAuditLog newLog = Allure.step("Create a new audit log entity with random ID and action CREATED",
                () -> new PaymentAuditLog());
        Allure.step("Set audit log ID to random UUID", () -> newLog.setId(UUID.randomUUID()));
        Allure.step("Set audit log createdAt to now", () -> newLog.setCreatedAt(LocalDateTime.now()));
        Allure.step("Mock auditLogRepository findAll to return a list with 2 logs",
                () -> when(auditLogRepository.findAll()).thenReturn(List.of(oldLog, newLog)));

        service.cleanupOldAuditLogs(LocalDateTime.now().minusDays(5));
        Allure.step("Verify auditLogRepository delete is called with oldLog",
                () -> verify(auditLogRepository).delete(eq(oldLog)));
        Allure.step("Verify auditLogRepository delete is never called with newLog",
                () -> verify(auditLogRepository, never()).delete(eq(newLog)));
    }

    @Test
    @Story("Stats and breakdown delegate to repository")
    @Severity(SeverityLevel.TRIVIAL)
    void stats_delegation_methods_work() {
        Allure.step("Mock auditLogRepository countByAction to return 5",
                () -> when(auditLogRepository.countByAction("UPDATED")).thenReturn(5L));
        Allure.step("Mock auditLogRepository countByUserId to return 3",
                () -> when(auditLogRepository.countByUserId(1L)).thenReturn(3L));
        Allure.step("Mock auditLogRepository countByPaymentRequestId to return 2",
                () -> when(auditLogRepository.countByPaymentRequestId(any())).thenReturn(2L));
        Allure.step("Mock auditLogRepository countByPaymentTransactionId to return 4",
                () -> when(auditLogRepository.countByPaymentTransactionId(any())).thenReturn(4L));
        Allure.step("Mock auditLogRepository countByPaymentRefundId to return 1",
                () -> when(auditLogRepository.countByPaymentRefundId(any())).thenReturn(1L));
        Allure.step("Mock auditLogRepository findDistinctActions to return a list with 2 actions",
                () -> when(auditLogRepository.findDistinctActions()).thenReturn(List.of("CREATED", "UPDATED")));
        Allure.step("Verify service countByAction returns 5 for UPDATED",
                () -> assertEquals(5L, service.countByAction("UPDATED")));
        Allure.step("Verify service countByUser returns 3 for user 1",
                () -> assertEquals(3L, service.countByUser(1L)));
        Allure.step("Verify service countByPaymentRequest returns 2 for random request ID",
                () -> assertEquals(2L, service.countByPaymentRequest(UUID.randomUUID())));
        Allure.step("Verify service countByTransaction returns 4 for random transaction ID",
                () -> assertEquals(4L, service.countByTransaction(UUID.randomUUID())));
        Allure.step("Verify service countByRefund returns 1 for random refund ID",
                () -> assertEquals(1L, service.countByRefund(UUID.randomUUID())));

        Map<String, Long> breakdown = Allure.step("Verify service getActionCountBreakdown returns a map with 2 keys",
                () -> service.getActionCountBreakdown());
        Allure.step("Verify service getActionCountBreakdown returns a map with 2 keys",
                () -> assertEquals(2, breakdown.size()));
        Allure.step("Verify service getActionCountBreakdown returns a map with key CREATED",
                () -> assertTrue(breakdown.containsKey("CREATED")));
        Allure.step("Verify service getActionCountBreakdown returns a map with key UPDATED",
                () -> assertTrue(breakdown.containsKey("UPDATED")));
        Allure.step("Verify service getActionCountBreakdown returns a map with value 5 for UPDATED",
                () -> assertEquals(5L, breakdown.get("UPDATED")));
    }

    @Test
    @Story("User action count breakdown groups by userId")
    @Severity(SeverityLevel.TRIVIAL)
    void getUserActionCountBreakdown_groupsCounts() {
        PaymentAuditLog l1 = Allure.step("Create a new audit log entity with userId 1",
                () -> new PaymentAuditLog());
        l1.setUserId(1L);
        PaymentAuditLog l2 = Allure.step("Create a new audit log entity with userId 1",
                () -> new PaymentAuditLog());
        l2.setUserId(1L);
        PaymentAuditLog l3 = Allure.step("Create a new audit log entity with userId 2",
                () -> new PaymentAuditLog());
        l3.setUserId(2L);
        Allure.step("Mock auditLogRepository findUserActionsOrderByCreatedAtDesc to return a list with 3 logs",
                () -> when(auditLogRepository.findUserActionsOrderByCreatedAtDesc()).thenReturn(List.of(l1, l2, l3)));

        Map<Long, Long> result = Allure.step("Verify service getUserActionCountBreakdown returns a map with 2 keys",
                () -> service.getUserActionCountBreakdown());
        Allure.step("Verify service getUserActionCountBreakdown returns a map with 2 keys",
                () -> assertEquals(2, result.size()));
        Allure.step("Verify service getUserActionCountBreakdown returns a map with value 2 for userId 1",
                () -> assertEquals(2L, result.get(1L)));
        Allure.step("Verify service getUserActionCountBreakdown returns a map with value 1 for userId 2",
                () -> assertEquals(1L, result.get(2L)));
    }
}