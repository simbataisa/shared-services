package com.ahss.controller;

import com.ahss.dto.request.CreatePaymentRequestDto;
import com.ahss.dto.request.CreateRefundDto;
import com.ahss.dto.request.ProcessPaymentDto;
import com.ahss.dto.request.UpdatePaymentRequestDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.dto.response.PaymentAuditLogDto;
import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.enums.PaymentTransactionType;
import com.ahss.enums.PaymentMethodType;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRefundService;
import com.ahss.service.PaymentRequestService;
import com.ahss.service.PaymentTransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentRequestService paymentRequestService;

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Autowired
    private PaymentRefundService paymentRefundService;

    @Autowired
    private PaymentAuditLogService auditLogService;

    // ===== PAYMENT REQUESTS =====

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<PaymentRequestDto>>> getAllPaymentRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentRequestDto> requests = paymentRequestService.getAllPaymentRequests(pageable);
        return ResponseEntity.ok(ApiResponse.ok(requests, "Payment requests retrieved successfully", "/api/v1/payments/requests"));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> getPaymentRequestById(@PathVariable Long id) {
        Optional<PaymentRequestDto> request = paymentRequestService.getPaymentRequestById(id);
        if (request.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(request.get(), "Payment request retrieved successfully", "/api/v1/payments/requests/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment request not found", "/api/v1/payments/requests/" + id));
        }
    }

    @GetMapping("/requests/code/{code}")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> getPaymentRequestByCode(@PathVariable String code) {
        Optional<PaymentRequestDto> request = paymentRequestService.getPaymentRequestByCode(code);
        if (request.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(request.get(), "Payment request retrieved successfully", "/api/v1/payments/requests/code/" + code));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment request not found", "/api/v1/payments/requests/code/" + code));
        }
    }

    @GetMapping("/requests/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<Page<PaymentRequestDto>>> getPaymentRequestsByTenant(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentRequestDto> requests = paymentRequestService.getPaymentRequestsByTenant(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(requests, "Payment requests retrieved successfully", "/api/v1/payments/requests/tenant/" + tenantId));
    }

    @GetMapping("/requests/status/{status}")
    public ResponseEntity<ApiResponse<Page<PaymentRequestDto>>> getPaymentRequestsByStatus(
            @PathVariable PaymentRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentRequestDto> requests = paymentRequestService.getPaymentRequestsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(requests, "Payment requests retrieved successfully", "/api/v1/payments/requests/status/" + status));
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> createPaymentRequest(@Valid @RequestBody CreatePaymentRequestDto requestDto) {
        try {
            PaymentRequestDto createdRequest = paymentRequestService.createPaymentRequest(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdRequest, "Payment request created successfully", "/api/v1/payments/requests"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests"));
        }
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> updatePaymentRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentRequestDto requestDto) {
        try {
            PaymentRequestDto updatedRequest = paymentRequestService.updatePaymentRequest(id, requestDto);
            return ResponseEntity.ok(ApiResponse.ok(updatedRequest, "Payment request updated successfully", "/api/v1/payments/requests/" + id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests/" + id));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests/" + id));
            }
        }
    }

    @PatchMapping("/requests/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPaymentRequest(@PathVariable Long id) {
        try {
            paymentRequestService.cancelPaymentRequest(id, "Cancelled by user request");
            return ResponseEntity.ok(ApiResponse.ok(null, "Payment request cancelled successfully", "/api/v1/payments/requests/" + id + "/cancel"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests/" + id + "/cancel"));
        }
    }

    @PatchMapping("/requests/{id}/approve")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> approvePaymentRequest(@PathVariable Long id) {
        try {
            PaymentRequestDto updatedRequest = paymentRequestService.updatePaymentRequest(id, null);
            // Update status to APPROVED
            updatedRequest.setStatus(PaymentRequestStatus.APPROVED);
            return ResponseEntity.ok(ApiResponse.ok(updatedRequest, "Payment request approved successfully", "/api/v1/payments/requests/" + id + "/approve"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests/" + id + "/approve"));
        }
    }

    @PatchMapping("/requests/{id}/reject")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> rejectPaymentRequest(@PathVariable Long id) {
        try {
            PaymentRequestDto updatedRequest = paymentRequestService.updatePaymentRequest(id, null);
            // Update status to REJECTED
            updatedRequest.setStatus(PaymentRequestStatus.REJECTED);
            return ResponseEntity.ok(ApiResponse.ok(updatedRequest, "Payment request rejected successfully", "/api/v1/payments/requests/" + id + "/reject"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests/" + id + "/reject"));
        }
    }

    // ===== PAYMENT TRANSACTIONS =====

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<PaymentTransactionDto>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentTransactionDto> transactions = paymentTransactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(transactions, "Payment transactions retrieved successfully", "/api/v1/payments/transactions"));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> getTransactionById(@PathVariable Long id) {
        Optional<PaymentTransactionDto> transaction = paymentTransactionService.getTransactionById(id);
        if (transaction.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(transaction.get(), "Payment transaction retrieved successfully", "/api/v1/payments/transactions/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment transaction not found", "/api/v1/payments/transactions/" + id));
        }
    }

    @GetMapping("/transactions/code/{code}")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> getTransactionByCode(@PathVariable String code) {
        Optional<PaymentTransactionDto> transaction = paymentTransactionService.getTransactionByCode(code);
        if (transaction.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(transaction.get(), "Payment transaction retrieved successfully", "/api/v1/payments/transactions/code/" + code));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment transaction not found", "/api/v1/payments/transactions/code/" + code));
        }
    }

    @GetMapping("/transactions/request/{requestId}")
    public ResponseEntity<ApiResponse<Page<PaymentTransactionDto>>> getTransactionsByRequest(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentTransactionDto> transactions = paymentTransactionService.getTransactionsByPaymentRequest(requestId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(transactions, "Payment transactions retrieved successfully", "/api/v1/payments/transactions/request/" + requestId));
    }

    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<ApiResponse<Page<PaymentTransactionDto>>> getTransactionsByStatus(
            @PathVariable PaymentTransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentTransactionDto> transactions = paymentTransactionService.getTransactionsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(transactions, "Payment transactions retrieved successfully", "/api/v1/payments/transactions/status/" + status));
    }

    @PostMapping("/transactions/process")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> processPayment(@Valid @RequestBody ProcessPaymentDto processDto) {
        try {
            PaymentTransactionDto transaction = paymentTransactionService.processPayment(processDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(transaction, "Payment processed successfully", "/api/v1/payments/transactions/process"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/transactions/process"));
        }
    }

    @PatchMapping("/transactions/{id}/retry")
    public ResponseEntity<ApiResponse<Void>> retryTransaction(@PathVariable Long id) {
        try {
            paymentTransactionService.retryTransaction(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Payment transaction retry initiated successfully", "/api/v1/payments/transactions/" + id + "/retry"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/transactions/" + id + "/retry"));
        }
    }

    @PatchMapping("/transactions/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTransaction(@PathVariable Long id) {
        try {
            paymentTransactionService.cancelTransaction(id, "Cancelled by user request");
            return ResponseEntity.ok(ApiResponse.ok(null, "Payment transaction cancelled successfully", "/api/v1/payments/transactions/" + id + "/cancel"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/transactions/" + id + "/cancel"));
        }
    }

    // ===== PAYMENT REFUNDS =====

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<Page<PaymentRefundDto>>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentRefundDto> refunds = paymentRefundService.getAllRefunds(pageable);
        return ResponseEntity.ok(ApiResponse.ok(refunds, "Payment refunds retrieved successfully", "/api/v1/payments/refunds"));
    }

    @GetMapping("/refunds/{id}")
    public ResponseEntity<ApiResponse<PaymentRefundDto>> getRefundById(@PathVariable Long id) {
        Optional<PaymentRefundDto> refund = paymentRefundService.getRefundById(id);
        if (refund.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(refund.get(), "Payment refund retrieved successfully", "/api/v1/payments/refunds/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment refund not found", "/api/v1/payments/refunds/" + id));
        }
    }

    @GetMapping("/refunds/code/{code}")
    public ResponseEntity<ApiResponse<PaymentRefundDto>> getRefundByCode(@PathVariable String code) {
        Optional<PaymentRefundDto> refund = paymentRefundService.getRefundByCode(code);
        if (refund.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(refund.get(), "Payment refund retrieved successfully", "/api/v1/payments/refunds/code/" + code));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment refund not found", "/api/v1/payments/refunds/code/" + code));
        }
    }

    @GetMapping("/refunds/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Page<PaymentRefundDto>>> getRefundsByTransaction(
            @PathVariable Long transactionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentRefundDto> refunds = paymentRefundService.getRefundsByTransaction(transactionId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(refunds, "Payment refunds retrieved successfully", "/api/v1/payments/refunds/transaction/" + transactionId));
    }

    @GetMapping("/refunds/status/{status}")
    public ResponseEntity<ApiResponse<Page<PaymentRefundDto>>> getRefundsByStatus(
            @PathVariable PaymentTransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentRefundDto> refunds = paymentRefundService.getRefundsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(refunds, "Payment refunds retrieved successfully", "/api/v1/payments/refunds/status/" + status));
    }

    @PostMapping("/refunds")
    public ResponseEntity<ApiResponse<PaymentRefundDto>> createRefund(@Valid @RequestBody CreateRefundDto refundDto) {
        try {
            PaymentRefundDto createdRefund = paymentRefundService.createRefund(refundDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdRefund, "Payment refund created successfully", "/api/v1/payments/refunds"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/refunds"));
        }
    }

    @PatchMapping("/refunds/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRefund(@PathVariable Long id) {
        try {
            paymentRefundService.cancelRefund(id, "Cancelled by user request");
            return ResponseEntity.ok(ApiResponse.ok(null, "Payment refund cancelled successfully", "/api/v1/payments/refunds/" + id + "/cancel"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/refunds/" + id + "/cancel"));
        }
    }

    // ===== AUDIT LOGS =====

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentAuditLogDto> auditLogs = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs"));
    }

    @GetMapping("/audit-logs/{id}")
    public ResponseEntity<ApiResponse<PaymentAuditLogDto>> getAuditLogById(@PathVariable Long id) {
        Optional<PaymentAuditLogDto> auditLog = auditLogService.getAuditLogById(id);
        if (auditLog.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(auditLog.get(), "Payment audit log retrieved successfully", "/api/v1/payments/audit-logs/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Payment audit log not found", "/api/v1/payments/audit-logs/" + id));
        }
    }

    @GetMapping("/audit-logs/request/{requestId}")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByPaymentRequest(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentAuditLogDto> auditLogs = auditLogService.getAuditLogsByPaymentRequest(requestId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs/request/" + requestId));
    }

    @GetMapping("/audit-logs/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByTransaction(
            @PathVariable Long transactionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentAuditLogDto> auditLogs = auditLogService.getAuditLogsByTransaction(transactionId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs/transaction/" + transactionId));
    }

    @GetMapping("/audit-logs/refund/{refundId}")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByRefund(
            @PathVariable Long refundId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentAuditLogDto> auditLogs = auditLogService.getAuditLogsByRefund(refundId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs/refund/" + refundId));
    }

    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentAuditLogDto> auditLogs = auditLogService.getAuditLogsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs/user/" + userId));
    }

    @GetMapping("/audit-logs/search")
    public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> searchAuditLogs(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentAuditLogDto> auditLogs = auditLogService.searchAuditLogs(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.ok(auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs/search"));
    }

    // ===== STATISTICS AND ANALYTICS =====

    @GetMapping("/stats/requests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentRequestStats() {
        try {
            Map<String, Object> stats = Map.of(
                "totalRequests", paymentRequestService.countByStatus(PaymentRequestStatus.PENDING) +
                               paymentRequestService.countByStatus(PaymentRequestStatus.APPROVED) +
                               paymentRequestService.countByStatus(PaymentRequestStatus.REJECTED) +
                               paymentRequestService.countByStatus(PaymentRequestStatus.CANCELLED),
                "pendingRequests", paymentRequestService.countByStatus(PaymentRequestStatus.PENDING),
                "approvedRequests", paymentRequestService.countByStatus(PaymentRequestStatus.APPROVED),
                "rejectedRequests", paymentRequestService.countByStatus(PaymentRequestStatus.REJECTED),
                "cancelledRequests", paymentRequestService.countByStatus(PaymentRequestStatus.CANCELLED)
            );
            return ResponseEntity.ok(ApiResponse.ok(stats, "Payment request statistics retrieved successfully", "/api/v1/payments/stats/requests"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.notOk(null, "Failed to retrieve payment request statistics", "/api/v1/payments/stats/requests"));
        }
    }

    @GetMapping("/stats/transactions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTransactionStats() {
        try {
            Map<String, Object> stats = Map.of(
                "totalTransactions", paymentTransactionService.countByStatus(PaymentTransactionStatus.PENDING) +
                                   paymentTransactionService.countByStatus(PaymentTransactionStatus.PROCESSING) +
                                   paymentTransactionService.countByStatus(PaymentTransactionStatus.COMPLETED) +
                                   paymentTransactionService.countByStatus(PaymentTransactionStatus.FAILED) +
                                   paymentTransactionService.countByStatus(PaymentTransactionStatus.CANCELLED),
                "pendingTransactions", paymentTransactionService.countByStatus(PaymentTransactionStatus.PENDING),
                "processingTransactions", paymentTransactionService.countByStatus(PaymentTransactionStatus.PROCESSING),
                "completedTransactions", paymentTransactionService.countByStatus(PaymentTransactionStatus.COMPLETED),
                "failedTransactions", paymentTransactionService.countByStatus(PaymentTransactionStatus.FAILED),
                "cancelledTransactions", paymentTransactionService.countByStatus(PaymentTransactionStatus.CANCELLED)
            );
            return ResponseEntity.ok(ApiResponse.ok(stats, "Payment transaction statistics retrieved successfully", "/api/v1/payments/stats/transactions"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.notOk(null, "Failed to retrieve payment transaction statistics", "/api/v1/payments/stats/transactions"));
        }
    }

    @GetMapping("/stats/refunds")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRefundStats() {
        try {
            Map<String, Object> stats = Map.of(
                "totalRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.PENDING) +
                              paymentRefundService.countByStatus(PaymentTransactionStatus.PROCESSING) +
                              paymentRefundService.countByStatus(PaymentTransactionStatus.COMPLETED) +
                              paymentRefundService.countByStatus(PaymentTransactionStatus.FAILED) +
                              paymentRefundService.countByStatus(PaymentTransactionStatus.CANCELLED),
                "pendingRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.PENDING),
                "processingRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.PROCESSING),
                "completedRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.COMPLETED),
                "failedRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.FAILED),
                "cancelledRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.CANCELLED)
            );
            return ResponseEntity.ok(ApiResponse.ok(stats, "Payment refund statistics retrieved successfully", "/api/v1/payments/stats/refunds"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.notOk(null, "Failed to retrieve payment refund statistics", "/api/v1/payments/stats/refunds"));
        }
    }

    @GetMapping("/stats/audit-logs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogStats() {
        try {
            List<String> distinctActions = auditLogService.getDistinctActions();
            Map<String, Long> actionBreakdown = auditLogService.getActionCountBreakdown();
            
            Map<String, Object> stats = Map.of(
                "totalAuditLogs", actionBreakdown.values().stream().mapToLong(Long::longValue).sum(),
                "distinctActions", distinctActions.size(),
                "actionBreakdown", actionBreakdown
            );
            return ResponseEntity.ok(ApiResponse.ok(stats, "Payment audit log statistics retrieved successfully", "/api/v1/payments/stats/audit-logs"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.notOk(null, "Failed to retrieve payment audit log statistics", "/api/v1/payments/stats/audit-logs"));
        }
    }
}