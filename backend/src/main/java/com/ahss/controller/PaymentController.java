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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class PaymentController {

  private final PaymentRequestService paymentRequestService;

  private final PaymentTransactionService paymentTransactionService;

  private final PaymentRefundService paymentRefundService;

  private final PaymentAuditLogService auditLogService;

  public PaymentController(
      PaymentRequestService paymentRequestService,
      PaymentTransactionService paymentTransactionService,
      PaymentRefundService paymentRefundService,
      PaymentAuditLogService auditLogService) {
    this.paymentRequestService = paymentRequestService;
    this.paymentTransactionService = paymentTransactionService;
    this.paymentRefundService = paymentRefundService;
    this.auditLogService = auditLogService;
  }

  // ===== PAYMENT REQUESTS =====

  @GetMapping("/requests")
  public ResponseEntity<ApiResponse<Page<PaymentRequestDto>>> getAllPaymentRequests(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentRequestDto> requests = paymentRequestService.getAllPaymentRequests(pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            requests, "Payment requests retrieved successfully", "/api/v1/payments/requests"));
  }

  @GetMapping("/requests/{id}")
  public ResponseEntity<ApiResponse<PaymentRequestDto>> getPaymentRequestById(
      @PathVariable UUID id) {
    Optional<PaymentRequestDto> request = paymentRequestService.getPaymentRequestById(id);
    return request
        .map(
            paymentRequestDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentRequestDto,
                        "Payment request retrieved successfully",
                        "/api/v1/payments/requests/" + id)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null, "Payment request not found", "/api/v1/payments/requests/" + id)));
  }

  @GetMapping("/requests/code/{code}")
  public ResponseEntity<ApiResponse<PaymentRequestDto>> getPaymentRequestByCode(
      @PathVariable String code) {
    Optional<PaymentRequestDto> request = paymentRequestService.getPaymentRequestByCode(code);
    return request
        .map(
            paymentRequestDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentRequestDto,
                        "Payment request retrieved successfully",
                        "/api/v1/payments/requests/code/" + code)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null,
                            "Payment request not found",
                            "/api/v1/payments/requests/code/" + code)));
  }

  @GetMapping("/requests/tenant/{tenantId}")
  public ResponseEntity<ApiResponse<Page<PaymentRequestDto>>> getPaymentRequestsByTenant(
      @PathVariable Long tenantId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentRequestDto> requests =
        paymentRequestService.getPaymentRequestsByTenant(tenantId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            requests,
            "Payment requests retrieved successfully",
            "/api/v1/payments/requests/tenant/" + tenantId));
  }

  @GetMapping("/requests/status/{status}")
  public ResponseEntity<ApiResponse<Page<PaymentRequestDto>>> getPaymentRequestsByStatus(
      @PathVariable PaymentRequestStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentRequestDto> requests =
        paymentRequestService.getPaymentRequestsByStatus(status, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            requests,
            "Payment requests retrieved successfully",
            "/api/v1/payments/requests/status/" + status));
  }

  @PostMapping("/requests")
  public ResponseEntity<ApiResponse<PaymentRequestDto>> createPaymentRequest(
      @Valid @RequestBody CreatePaymentRequestDto requestDto) {
    try {
      PaymentRequestDto createdRequest = paymentRequestService.createPaymentRequest(requestDto);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.ok(
                  createdRequest,
                  "Payment request created successfully",
                  "/api/v1/payments/requests"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/requests"));
    }
  }

  @PutMapping("/requests/{id}")
  public ResponseEntity<ApiResponse<PaymentRequestDto>> updatePaymentRequest(
      @PathVariable UUID id, @Valid @RequestBody UpdatePaymentRequestDto requestDto) {
    try {
      PaymentRequestDto updatedRequest = paymentRequestService.updatePaymentRequest(id, requestDto);
      return ResponseEntity.ok(
          ApiResponse.ok(
              updatedRequest,
              "Payment request updated successfully",
              "/api/v1/payments/requests/" + id));
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
  public ResponseEntity<ApiResponse<Void>> cancelPaymentRequest(@PathVariable UUID id) {
    try {
      paymentRequestService.cancelPaymentRequest(id, "Cancelled by user request");
      return ResponseEntity.ok(
          ApiResponse.ok(
              null,
              "Payment request cancelled successfully",
              "/api/v1/payments/requests/" + id + "/cancel"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/requests/" + id + "/cancel"));
    }
  }

  @PatchMapping("/requests/{id}/approve")
  public ResponseEntity<ApiResponse<PaymentRequestDto>> approvePaymentRequest(
      @PathVariable UUID id) {
    try {
      PaymentRequestDto updatedRequest = paymentRequestService.updatePaymentRequest(id, null);
      // Update status to APPROVED
      updatedRequest.setStatus(PaymentRequestStatus.APPROVED);
      return ResponseEntity.ok(
          ApiResponse.ok(
              updatedRequest,
              "Payment request approved successfully",
              "/api/v1/payments/requests/" + id + "/approve"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/requests/" + id + "/approve"));
    }
  }

  @PatchMapping("/requests/{id}/reject")
  public ResponseEntity<ApiResponse<PaymentRequestDto>> rejectPaymentRequest(
      @PathVariable UUID id) {
    try {
      PaymentRequestDto updatedRequest = paymentRequestService.updatePaymentRequest(id, null);
      // Update status to REJECTED
      updatedRequest.setStatus(PaymentRequestStatus.REJECTED);
      return ResponseEntity.ok(
          ApiResponse.ok(
              updatedRequest,
              "Payment request rejected successfully",
              "/api/v1/payments/requests/" + id + "/reject"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/requests/" + id + "/reject"));
    }
  }

  // ===== PAYMENT TRANSACTIONS =====

  @GetMapping("/transactions")
  public ResponseEntity<ApiResponse<Page<PaymentTransactionDto>>> getAllTransactions(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentTransactionDto> transactions =
        paymentTransactionService.getAllTransactions(pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            transactions,
            "Payment transactions retrieved successfully",
            "/api/v1/payments/transactions"));
  }

  @GetMapping("/transactions/{id}")
  public ResponseEntity<ApiResponse<PaymentTransactionDto>> getTransactionById(
      @PathVariable UUID id) {
    Optional<PaymentTransactionDto> transaction = paymentTransactionService.getTransactionById(id);
    return transaction
        .map(
            paymentTransactionDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentTransactionDto,
                        "Payment transaction retrieved successfully",
                        "/api/v1/payments/transactions/" + id)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null,
                            "Payment transaction not found",
                            "/api/v1/payments/transactions/" + id)));
  }

  @GetMapping("/transactions/code/{code}")
  public ResponseEntity<ApiResponse<PaymentTransactionDto>> getTransactionByCode(
      @PathVariable String code) {
    Optional<PaymentTransactionDto> transaction =
        paymentTransactionService.getTransactionByCode(code);
    return transaction
        .map(
            paymentTransactionDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentTransactionDto,
                        "Payment transaction retrieved successfully",
                        "/api/v1/payments/transactions/code/" + code)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null,
                            "Payment transaction not found",
                            "/api/v1/payments/transactions/code/" + code)));
  }

  @GetMapping("/transactions/request/{requestId}")
  public ResponseEntity<ApiResponse<Page<PaymentTransactionDto>>> getTransactionsByRequest(
      @PathVariable UUID requestId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentTransactionDto> transactions =
        paymentTransactionService.getTransactionsByPaymentRequest(requestId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            transactions,
            "Payment transactions retrieved successfully",
            "/api/v1/payments/transactions/request/" + requestId));
  }

  @GetMapping("/transactions/status/{status}")
  public ResponseEntity<ApiResponse<Page<PaymentTransactionDto>>> getTransactionsByStatus(
      @PathVariable PaymentTransactionStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentTransactionDto> transactions =
        paymentTransactionService.getTransactionsByStatus(status, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            transactions,
            "Payment transactions retrieved successfully",
            "/api/v1/payments/transactions/status/" + status));
  }

  @PostMapping("/transactions/process")
  public ResponseEntity<ApiResponse<PaymentTransactionDto>> processPayment(
      @Valid @RequestBody ProcessPaymentDto processDto) {
    try {
      PaymentTransactionDto transaction = paymentTransactionService.processPayment(processDto);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.ok(
                  transaction,
                  "Payment processed successfully",
                  "/api/v1/payments/transactions/process"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/transactions/process"));
    }
  }

  @PatchMapping("/transactions/{id}/retry")
  public ResponseEntity<ApiResponse<Void>> retryTransaction(@PathVariable UUID id) {
    try {
      paymentTransactionService.retryTransaction(id);
      return ResponseEntity.ok(
          ApiResponse.ok(
              null,
              "Payment transaction retry initiated successfully",
              "/api/v1/payments/transactions/" + id + "/retry"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/transactions/" + id + "/retry"));
    }
  }

  @PatchMapping("/transactions/{id}/cancel")
  public ResponseEntity<ApiResponse<Void>> cancelTransaction(@PathVariable UUID id) {
    try {
      paymentTransactionService.cancelTransaction(id, "Cancelled by user request");
      return ResponseEntity.ok(
          ApiResponse.ok(
              null,
              "Payment transaction cancelled successfully",
              "/api/v1/payments/transactions/" + id + "/cancel"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/transactions/" + id + "/cancel"));
    }
  }

  // ===== PAYMENT REFUNDS =====

  @GetMapping("/refunds")
  public ResponseEntity<ApiResponse<Page<PaymentRefundDto>>> getAllRefunds(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentRefundDto> refunds = paymentRefundService.getAllRefunds(pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            refunds, "Payment refunds retrieved successfully", "/api/v1/payments/refunds"));
  }

  @GetMapping("/refunds/{id}")
  public ResponseEntity<ApiResponse<PaymentRefundDto>> getRefundById(@PathVariable UUID id) {
    Optional<PaymentRefundDto> refund = paymentRefundService.getRefundById(id);
    return refund
        .map(
            paymentRefundDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentRefundDto,
                        "Payment refund retrieved successfully",
                        "/api/v1/payments/refunds/" + id)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null, "Payment refund not found", "/api/v1/payments/refunds/" + id)));
  }

  @GetMapping("/refunds/code/{code}")
  public ResponseEntity<ApiResponse<PaymentRefundDto>> getRefundByCode(@PathVariable String code) {
    Optional<PaymentRefundDto> refund = paymentRefundService.getRefundByCode(code);
    return refund
        .map(
            paymentRefundDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentRefundDto,
                        "Payment refund retrieved successfully",
                        "/api/v1/payments/refunds/code/" + code)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null,
                            "Payment refund not found",
                            "/api/v1/payments/refunds/code/" + code)));
  }

  @GetMapping("/refunds/transaction/{transactionId}")
  public ResponseEntity<ApiResponse<Page<PaymentRefundDto>>> getRefundsByTransaction(
      @PathVariable UUID transactionId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentRefundDto> refunds =
        paymentRefundService.getRefundsByTransaction(transactionId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            refunds,
            "Payment refunds retrieved successfully",
            "/api/v1/payments/refunds/transaction/" + transactionId));
  }

  @GetMapping("/refunds/status/{status}")
  public ResponseEntity<ApiResponse<Page<PaymentRefundDto>>> getRefundsByStatus(
      @PathVariable PaymentTransactionStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentRefundDto> refunds = paymentRefundService.getRefundsByStatus(status, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            refunds,
            "Payment refunds retrieved successfully",
            "/api/v1/payments/refunds/status/" + status));
  }

  @PostMapping("/refunds")
  public ResponseEntity<ApiResponse<PaymentRefundDto>> createRefund(
      @Valid @RequestBody CreateRefundDto refundDto) {
    try {
      PaymentRefundDto createdRefund = paymentRefundService.createRefund(refundDto);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.ok(
                  createdRefund,
                  "Payment refund created successfully",
                  "/api/v1/payments/refunds"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/payments/refunds"));
    }
  }

  @PostMapping("/refunds/{id}/process")
  public ResponseEntity<ApiResponse<PaymentRefundDto>> processRefund(@PathVariable UUID id) {
    try {
      PaymentRefundDto processedRefund = paymentRefundService.processRefund(id);
      return ResponseEntity.ok(
          ApiResponse.ok(
              processedRefund,
              "Payment refund processed successfully",
              "/api/v1/payments/refunds/" + id + "/process"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/refunds/" + id + "/process"));
    }
  }

  @PatchMapping("/refunds/{id}/cancel")
  public ResponseEntity<ApiResponse<Void>> cancelRefund(@PathVariable UUID id) {
    try {
      paymentRefundService.cancelRefund(id, "Cancelled by user request");
      return ResponseEntity.ok(
          ApiResponse.ok(
              null,
              "Payment refund cancelled successfully",
              "/api/v1/payments/refunds/" + id + "/cancel"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.notOk(
                  null, e.getMessage(), "/api/v1/payments/refunds/" + id + "/cancel"));
    }
  }

  // ===== AUDIT LOGS =====

  @GetMapping("/audit-logs")
  public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAllAuditLogs(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentAuditLogDto> auditLogs = auditLogService.getAllAuditLogs(pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            auditLogs, "Payment audit logs retrieved successfully", "/api/v1/payments/audit-logs"));
  }

  @GetMapping("/audit-logs/{id}")
  public ResponseEntity<ApiResponse<PaymentAuditLogDto>> getAuditLogById(@PathVariable UUID id) {
    Optional<PaymentAuditLogDto> auditLog = auditLogService.getAuditLogById(id);
    return auditLog
        .map(
            paymentAuditLogDto ->
                ResponseEntity.ok(
                    ApiResponse.ok(
                        paymentAuditLogDto,
                        "Payment audit log retrieved successfully",
                        "/api/v1/payments/audit-logs/" + id)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        ApiResponse.notOk(
                            null,
                            "Payment audit log not found",
                            "/api/v1/payments/audit-logs/" + id)));
  }

  @GetMapping("/audit-logs/request/{requestId}")
  public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByPaymentRequest(
      @PathVariable UUID requestId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentAuditLogDto> auditLogs =
        auditLogService.getAuditLogsByPaymentRequest(requestId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            auditLogs,
            "Payment audit logs retrieved successfully",
            "/api/v1/payments/audit-logs/request/" + requestId));
  }

  @GetMapping("/audit-logs/transaction/{transactionId}")
  public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByTransaction(
      @PathVariable UUID transactionId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentAuditLogDto> auditLogs =
        auditLogService.getAuditLogsByTransaction(transactionId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            auditLogs,
            "Payment audit logs retrieved successfully",
            "/api/v1/payments/audit-logs/transaction/" + transactionId));
  }

  @GetMapping("/audit-logs/refund/{refundId}")
  public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByRefund(
      @PathVariable UUID refundId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentAuditLogDto> auditLogs = auditLogService.getAuditLogsByRefund(refundId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            auditLogs,
            "Payment audit logs retrieved successfully",
            "/api/v1/payments/audit-logs/refund/" + refundId));
  }

  @GetMapping("/audit-logs/user/{userId}")
  public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> getAuditLogsByUser(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentAuditLogDto> auditLogs = auditLogService.getAuditLogsByUser(userId, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            auditLogs,
            "Payment audit logs retrieved successfully",
            "/api/v1/payments/audit-logs/user/" + userId));
  }

  @GetMapping("/audit-logs/search")
  public ResponseEntity<ApiResponse<Page<PaymentAuditLogDto>>> searchAuditLogs(
      @RequestParam String searchTerm,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PaymentAuditLogDto> auditLogs = auditLogService.searchAuditLogs(searchTerm, pageable);
    return ResponseEntity.ok(
        ApiResponse.ok(
            auditLogs,
            "Payment audit logs retrieved successfully",
            "/api/v1/payments/audit-logs/search"));
  }

  // ===== STATISTICS AND ANALYTICS =====

  @GetMapping("/stats/requests")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentRequestStats() {
    try {
      Map<String, Object> stats =
          Map.of(
              "totalRequests",
                  paymentRequestService.countByStatus(PaymentRequestStatus.PENDING)
                      + paymentRequestService.countByStatus(PaymentRequestStatus.COMPLETED)
                      + paymentRequestService.countByStatus(PaymentRequestStatus.FAILED)
                      + paymentRequestService.countByStatus(PaymentRequestStatus.CANCELLED),
              "pendingRequests", paymentRequestService.countByStatus(PaymentRequestStatus.PENDING),
              "completedRequests",
                  paymentRequestService.countByStatus(PaymentRequestStatus.COMPLETED),
              "failedRequests", paymentRequestService.countByStatus(PaymentRequestStatus.FAILED),
              "cancelledRequests",
                  paymentRequestService.countByStatus(PaymentRequestStatus.CANCELLED));
      return ResponseEntity.ok(
          ApiResponse.ok(
              stats,
              "Payment request statistics retrieved successfully",
              "/api/v1/payments/stats/requests"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.notOk(
                  null,
                  "Failed to retrieve payment request statistics",
                  "/api/v1/payments/stats/requests"));
    }
  }

  @GetMapping("/stats/transactions")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getTransactionStats() {
    try {
      Map<String, Object> stats =
          Map.of(
              "totalTransactions",
                  paymentTransactionService.countByStatus(PaymentTransactionStatus.PENDING)
                      + paymentTransactionService.countByStatus(PaymentTransactionStatus.SUCCESS)
                      + paymentTransactionService.countByStatus(PaymentTransactionStatus.FAILED)
                      + paymentTransactionService.countByStatus(PaymentTransactionStatus.CANCELLED),
              "pendingTransactions",
                  paymentTransactionService.countByStatus(PaymentTransactionStatus.PENDING),
              "successfulTransactions",
                  paymentTransactionService.countByStatus(PaymentTransactionStatus.SUCCESS),
              "failedTransactions",
                  paymentTransactionService.countByStatus(PaymentTransactionStatus.FAILED),
              "cancelledTransactions",
                  paymentTransactionService.countByStatus(PaymentTransactionStatus.CANCELLED));
      return ResponseEntity.ok(
          ApiResponse.ok(
              stats,
              "Payment transaction statistics retrieved successfully",
              "/api/v1/payments/stats/transactions"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.notOk(
                  null,
                  "Failed to retrieve payment transaction statistics",
                  "/api/v1/payments/stats/transactions"));
    }
  }

  @GetMapping("/stats/refunds")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getRefundStats() {
    try {
      Map<String, Object> stats =
          Map.of(
              "totalRefunds",
                  paymentRefundService.countByStatus(PaymentTransactionStatus.PENDING)
                      + paymentRefundService.countByStatus(PaymentTransactionStatus.SUCCESS)
                      + paymentRefundService.countByStatus(PaymentTransactionStatus.FAILED)
                      + paymentRefundService.countByStatus(PaymentTransactionStatus.CANCELLED),
              "pendingRefunds",
                  paymentRefundService.countByStatus(PaymentTransactionStatus.PENDING),
              "successfulRefunds",
                  paymentRefundService.countByStatus(PaymentTransactionStatus.SUCCESS),
              "failedRefunds", paymentRefundService.countByStatus(PaymentTransactionStatus.FAILED),
              "cancelledRefunds",
                  paymentRefundService.countByStatus(PaymentTransactionStatus.CANCELLED));
      return ResponseEntity.ok(
          ApiResponse.ok(
              stats,
              "Payment refund statistics retrieved successfully",
              "/api/v1/payments/stats/refunds"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.notOk(
                  null,
                  "Failed to retrieve payment refund statistics",
                  "/api/v1/payments/stats/refunds"));
    }
  }

  @GetMapping("/stats/audit-logs")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogStats() {
    try {
      List<String> distinctActions = auditLogService.getDistinctActions();
      Map<String, Long> actionBreakdown = auditLogService.getActionCountBreakdown();

      Map<String, Object> stats =
          Map.of(
              "totalAuditLogs", actionBreakdown.values().stream().mapToLong(Long::longValue).sum(),
              "distinctActions", distinctActions.size(),
              "actionBreakdown", actionBreakdown);
      return ResponseEntity.ok(
          ApiResponse.ok(
              stats,
              "Payment audit log statistics retrieved successfully",
              "/api/v1/payments/stats/audit-logs"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.notOk(
                  null,
                  "Failed to retrieve payment audit log statistics",
                  "/api/v1/payments/stats/audit-logs"));
    }
  }
}
