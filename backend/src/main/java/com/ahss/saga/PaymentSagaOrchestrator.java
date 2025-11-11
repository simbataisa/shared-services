package com.ahss.saga;

import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentDomainEvent;
import com.ahss.kafka.producer.PaymentEventProducer;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRefundService;
import com.ahss.service.PaymentRequestService;
import com.ahss.service.PaymentTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class PaymentSagaOrchestrator {

    private final PaymentTransactionService transactionService;
    private final PaymentRefundService refundService;
    private final PaymentRequestService requestService;
    private final PaymentAuditLogService auditLogService;
    private final PaymentEventProducer eventProducer;

    public PaymentSagaOrchestrator(PaymentTransactionService transactionService,
            PaymentRefundService refundService,
            PaymentRequestService requestService,
            PaymentAuditLogService auditLogService,
            PaymentEventProducer eventProducer) {
        this.transactionService = transactionService;
        this.refundService = refundService;
        this.requestService = requestService;
        this.auditLogService = auditLogService;
        this.eventProducer = eventProducer;
    }

    public void handle(PaymentCallbackEvent event) {
        switch (event.getType()) {
            case REQUEST_APPROVED -> handleRequestApproval(event);
            case REQUEST_REJECTED -> handleRequestRejection(event);
            case PAYMENT_SUCCESS -> handlePaymentSuccess(event);
            case PAYMENT_FAILED -> handlePaymentFailure(event);
            case REFUND_SUCCESS -> handleRefundSuccess(event);
            case REFUND_FAILED -> handleRefundFailure(event);
        }
    }

    private void handleRequestApproval(PaymentCallbackEvent event) {
        log.info("Handling request approval event: {}", event);
        UUID requestId = resolveRequestId(event);
        if (requestId == null) {
            logMissingReference("REQUEST_APPROVED", event);
            return;
        }
        PaymentRequestDto updated = requestService.updateStatus(requestId, PaymentRequestStatus.APPROVED,
                "Approved via callback: " + safe(event.getGatewayName()));
        auditLogService.logPaymentRequestAction(requestId, "REQUEST_APPROVED", null,
                PaymentRequestStatus.APPROVED.toString(), "Request approved by vendor", null, null, null, null);
        eventProducer.send(new PaymentDomainEvent("request.approved", event.getCorrelationId(),
                Map.of("requestId", updated.getId())));
    }

    private void handleRequestRejection(PaymentCallbackEvent event) {
        log.info("Handling request rejection event: {}", event);
        UUID requestId = resolveRequestId(event);
        if (requestId == null) {
            logMissingReference("REQUEST_REJECTED", event);
            return;
        }
        PaymentRequestDto updated = requestService.updateStatus(requestId, PaymentRequestStatus.REJECTED,
                reasonFrom(event));
        auditLogService.logPaymentRequestAction(requestId, "REQUEST_REJECTED", null,
                PaymentRequestStatus.REJECTED.toString(), "Request rejected by vendor", null, null, null, null);
        eventProducer.send(new PaymentDomainEvent("request.rejected", event.getCorrelationId(),
                Map.of("requestId", updated.getId())));
    }

    private void handlePaymentSuccess(PaymentCallbackEvent event) {
        log.info("Handling payment success event: {}", event);
        Optional<PaymentTransactionDto> opt = transactionService
                .getTransactionByExternalId(event.getExternalTransactionId());
        if (opt.isEmpty()) {
            logMissingReference("PAYMENT_SUCCESS", event);
            return;
        }
        PaymentTransactionDto tx = opt.get();
        transactionService.markAsProcessed(tx.getId(), event.getExternalTransactionId(), event.getGatewayResponse());
        requestService.markAsPaid(tx.getPaymentRequestId(),
                event.getReceivedAt() != null ? event.getReceivedAt() : LocalDateTime.now());
        auditLogService.logTransactionAction(tx.getId(), "PAYMENT_SUCCESS",
                PaymentTransactionStatus.PROCESSING.toString(), PaymentTransactionStatus.SUCCESS.toString(),
                "Payment processed successfully", null, null, null, null);
        eventProducer.send(new PaymentDomainEvent("payment.success", event.getCorrelationId(),
                Map.of("transactionId", tx.getId())));
    }

    private void handlePaymentFailure(PaymentCallbackEvent event) {
        log.info("Handling payment failure event: {}", event);
        Optional<PaymentTransactionDto> opt = transactionService
                .getTransactionByExternalId(event.getExternalTransactionId());
        if (opt.isEmpty()) {
            logMissingReference("PAYMENT_FAILED", event);
            return;
        }
        PaymentTransactionDto tx = opt.get();
        transactionService.markAsFailed(tx.getId(), safe(event.getErrorCode()), safe(event.getErrorMessage()));
        requestService.updateStatus(tx.getPaymentRequestId(), PaymentRequestStatus.FAILED, reasonFrom(event));
        auditLogService.logTransactionAction(tx.getId(), "PAYMENT_FAILED",
                PaymentTransactionStatus.PROCESSING.toString(), PaymentTransactionStatus.FAILED.toString(),
                reasonFrom(event), null, null, null, null);
        eventProducer.send(new PaymentDomainEvent("payment.failed", event.getCorrelationId(),
                Map.of("transactionId", tx.getId())));
    }

    private void handleRefundSuccess(PaymentCallbackEvent event) {
        log.info("Handling refund success event: {}", event);
        Optional<PaymentRefundDto> opt = refundService.getRefundByExternalId(event.getExternalRefundId());
        if (opt.isEmpty()) {
            logMissingReference("REFUND_SUCCESS", event);
            return;
        }
        PaymentRefundDto refund = opt.get();
        refundService.markAsProcessed(refund.getId(), event.getExternalRefundId(), event.getGatewayResponse());

        // Determine full vs partial refund
        UUID txId = refund.getPaymentTransactionId();
        Optional<PaymentTransactionDto> txOpt = transactionService.getTransactionById(txId);
        if (txOpt.isPresent()) {
            BigDecimal txAmount = txOpt.get().getAmount();
            BigDecimal refundAmount = refund.getRefundAmount();
            PaymentRequestStatus newStatus = txAmount != null && refundAmount != null
                    && refundAmount.compareTo(txAmount) >= 0
                            ? PaymentRequestStatus.REFUNDED
                            : PaymentRequestStatus.PARTIAL_REFUND;
            requestService.updateStatus(txOpt.get().getPaymentRequestId(), newStatus,
                    "Refund processed: " + safe(event.getGatewayName()));
        }
        auditLogService.logRefundAction(refund.getId(), "REFUND_SUCCESS",
                PaymentTransactionStatus.PROCESSING.toString(), PaymentTransactionStatus.SUCCESS.toString(),
                "Refund processed successfully", null, null, null, null);
        eventProducer.send(
                new PaymentDomainEvent("refund.success", event.getCorrelationId(), Map.of("refundId", refund.getId())));
    }

    private void handleRefundFailure(PaymentCallbackEvent event) {
        log.info("Handling refund failure event: {}", event);
        Optional<PaymentRefundDto> opt = refundService.getRefundByExternalId(event.getExternalRefundId());
        if (opt.isEmpty()) {
            logMissingReference("REFUND_FAILED", event);
            return;
        }
        PaymentRefundDto refund = opt.get();
        refundService.markAsFailed(refund.getId(), safe(event.getErrorCode()), safe(event.getErrorMessage()));
        auditLogService.logRefundAction(refund.getId(), "REFUND_FAILED", PaymentTransactionStatus.PROCESSING.toString(),
                PaymentTransactionStatus.FAILED.toString(), reasonFrom(event), null, null, null, null);
        eventProducer.send(
                new PaymentDomainEvent("refund.failed", event.getCorrelationId(), Map.of("refundId", refund.getId())));
    }

    private UUID resolveRequestId(PaymentCallbackEvent event) {
        log.info("Resolving request ID for event: {}", event);
        if (event.getPaymentRequestId() != null)
            return event.getPaymentRequestId();
        if (event.getPaymentToken() != null) {
            Optional<PaymentRequestDto> pr = requestService.getPaymentRequestByToken(event.getPaymentToken());
            if (pr.isPresent())
                return pr.get().getId();
        }
        if (event.getRequestCode() != null) {
            Optional<PaymentRequestDto> pr = requestService.getPaymentRequestByCode(event.getRequestCode());
            if (pr.isPresent())
                return pr.get().getId();
        }
        return null;
    }

    private void logMissingReference(String action, PaymentCallbackEvent event) {
        // No audit entity available to attach; consider logging to an external system.
        log.warn("{}: No payment request found for event: {}", action, event);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String reasonFrom(PaymentCallbackEvent e) {
        return safe(e.getErrorCode()) + (e.getErrorMessage() != null ? (": " + e.getErrorMessage()) : "");
    }
}