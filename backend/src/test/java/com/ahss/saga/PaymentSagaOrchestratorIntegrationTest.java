package com.ahss.saga;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.kafka.event.PaymentDomainEvent;
import com.ahss.kafka.producer.PaymentEventProducer;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRefundService;
import com.ahss.service.PaymentRequestService;
import com.ahss.service.PaymentTransactionService;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@Epic("Saga")
@Feature("Payment Saga Orchestrator")
class PaymentSagaOrchestratorIntegrationTest {

  @Autowired private PaymentSagaOrchestrator orchestrator;

  @MockBean private PaymentTransactionService transactionService;
  @MockBean private PaymentRefundService refundService;
  @MockBean private PaymentRequestService requestService;
  @MockBean private PaymentAuditLogService auditLogService;
  @MockBean private PaymentEventProducer eventProducer;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Handles request approved and updates status")
  @Story("Payment Saga Orchestrator")
  void handles_request_approved_and_updates_status() {
    UUID requestId = UUID.randomUUID();
    PaymentRequestDto dto = Allure.step("Create PaymentRequestDto", PaymentRequestDto::new);
    dto.setId(requestId);
    Allure.step(
        "Mock PaymentRequestService.updateStatus",
        () ->
            when(requestService.updateStatus(
                    eq(requestId), eq(PaymentRequestStatus.APPROVED), anyString()))
                .thenReturn(dto));

    PaymentCallbackEvent event =
        Allure.step("Create PaymentCallbackEvent", PaymentCallbackEvent::new);
    event.setType(PaymentCallbackType.REQUEST_APPROVED);
    event.setCorrelationId("corr-req-1");
    event.setPaymentRequestId(requestId);
    event.setGatewayName("MockGateway");
    event.setReceivedAt(LocalDateTime.now());

    Allure.step("Invoke PaymentSagaOrchestrator.handle", () -> orchestrator.handle(event));

    Allure.step(
        "Verify PaymentRequestService.updateStatus",
        () ->
            verify(requestService, times(1))
                .updateStatus(eq(requestId), eq(PaymentRequestStatus.APPROVED), anyString()));
    Allure.step(
        "Verify PaymentAuditLogService.logPaymentRequestAction",
        () ->
            verify(auditLogService, times(1))
                .logPaymentRequestAction(
                    eq(requestId),
                    eq("REQUEST_APPROVED"),
                    isNull(),
                    eq(PaymentRequestStatus.APPROVED.toString()),
                    anyString(),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()));
    ArgumentCaptor<PaymentDomainEvent> evtCaptor =
        Allure.step(
            "Create ArgumentCaptor for PaymentDomainEvent",
            () -> ArgumentCaptor.forClass(PaymentDomainEvent.class));
    Allure.step(
        "Verify PaymentEventProducer.send",
        () -> verify(eventProducer, times(1)).send(evtCaptor.capture()));
    Allure.step(
        "Attach sent PaymentDomainEvent JSON (request approved)",
        () ->
            Allure.addAttachment(
                "Sent PaymentDomainEvent (request approved)",
                "application/json",
                objectMapper.writeValueAsString(evtCaptor.getValue())));
    Allure.step(
        "Verify PaymentDomainEvent type",
        () -> assertEquals("request.approved", evtCaptor.getValue().getType()));
  }

  @Test
  @DisplayName("Handles refund success and sets partial or full status")
  @Story("Payment Saga Orchestrator")
  void handles_refund_success_and_sets_partial_or_full_status() {
    UUID txId = UUID.randomUUID();
    UUID refundId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    PaymentRefundDto refundDto =
        Allure.step("Create PaymentRefundDto", PaymentRefundDto::new);
    refundDto.setId(refundId);
    refundDto.setPaymentTransactionId(txId);
    refundDto.setRefundAmount(new BigDecimal("50.00"));
    Allure.step(
        "Mock PaymentRefundService.getRefundByExternalId",
        () ->
            when(refundService.getRefundByExternalId(eq("ext-ref-1")))
                .thenReturn(Optional.of(refundDto)));

    PaymentTransactionDto txDto =
        Allure.step("Create PaymentTransactionDto", PaymentTransactionDto::new);
    txDto.setId(txId);
    txDto.setAmount(new BigDecimal("100.00"));
    txDto.setPaymentRequestId(requestId);
    Allure.step(
        "Mock PaymentTransactionService.getTransactionById",
        () -> when(transactionService.getTransactionById(eq(txId))).thenReturn(Optional.of(txDto)));

    PaymentCallbackEvent event =
        Allure.step("Create PaymentCallbackEvent", PaymentCallbackEvent::new);
    event.setType(PaymentCallbackType.REFUND_SUCCESS);
    event.setCorrelationId("corr-ref-1");
    event.setExternalRefundId("ext-ref-1");
    event.setGatewayName("MockGateway");
    event.setReceivedAt(LocalDateTime.now());
    event.setGatewayResponse(Map.of("status", "ok"));

    Allure.step("Invoke PaymentSagaOrchestrator.handle", () -> orchestrator.handle(event));

    Allure.step(
        "Verify PaymentRefundService.markAsProcessed",
        () ->
            verify(refundService, times(1))
                .markAsProcessed(eq(refundId), eq("ext-ref-1"), anyMap()));
    Allure.step(
        "Verify PaymentRequestService.updateStatus",
        () ->
            verify(requestService, times(1))
                .updateStatus(eq(requestId), eq(PaymentRequestStatus.PARTIAL_REFUND), anyString()));
    Allure.step(
        "Verify PaymentAuditLogService.logRefundAction",
        () ->
            verify(auditLogService, times(1))
                .logRefundAction(
                    eq(refundId),
                    eq("REFUND_SUCCESS"),
                    eq(PaymentTransactionStatus.PROCESSING.toString()),
                    eq(PaymentTransactionStatus.SUCCESS.toString()),
                    anyString(),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()));
    ArgumentCaptor<PaymentDomainEvent> evtCaptor2 =
        Allure.step(
            "Create ArgumentCaptor for PaymentDomainEvent (refund)",
            () -> ArgumentCaptor.forClass(PaymentDomainEvent.class));
    Allure.step(
        "Verify PaymentEventProducer.send",
        () -> verify(eventProducer, times(1)).send(evtCaptor2.capture()));
    Allure.step(
        "Attach sent PaymentDomainEvent JSON (refund success)",
        () ->
            Allure.addAttachment(
                "Sent PaymentDomainEvent (refund success)",
                "application/json",
                objectMapper.writeValueAsString(evtCaptor2.getValue())));
  }

  @Test
  @DisplayName("Handles payment success and marks processed and paid")
  @Story("Payment Saga Orchestrator")
  void handles_payment_success_and_marks_processed_and_paid() throws Exception {
    UUID txId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    PaymentTransactionDto txDto =
        Allure.step("Create PaymentTransactionDto", PaymentTransactionDto::new);
    txDto.setId(txId);
    txDto.setPaymentRequestId(requestId);

    Allure.step(
        "Mock PaymentTransactionService.getTransactionByExternalId",
        () ->
            when(transactionService.getTransactionByExternalId(eq("ext-tx-1")))
                .thenReturn(Optional.of(txDto)));

    LocalDateTime receivedAt = LocalDateTime.now();
    PaymentCallbackEvent event =
        Allure.step("Create PaymentCallbackEvent", PaymentCallbackEvent::new);
    event.setType(PaymentCallbackType.PAYMENT_SUCCESS);
    event.setCorrelationId("corr-pay-1");
    event.setExternalTransactionId("ext-tx-1");
    event.setReceivedAt(receivedAt);
    event.setGatewayResponse(Map.of("status", "ok"));

    Allure.step("Invoke PaymentSagaOrchestrator.handle", () -> orchestrator.handle(event));

    Allure.step(
        "Verify markAsProcessed called",
        () ->
            verify(transactionService, times(1))
                .markAsProcessed(eq(txId), eq("ext-tx-1"), anyMap()));
    Allure.step(
        "Verify markAsPaid called",
        () -> verify(requestService, times(1)).markAsPaid(eq(requestId), eq(receivedAt)));
    Allure.step(
        "Verify audit log for PAYMENT_SUCCESS",
        () ->
            verify(auditLogService, times(1))
                .logTransactionAction(
                    eq(txId),
                    eq("PAYMENT_SUCCESS"),
                    eq(PaymentTransactionStatus.PROCESSING.toString()),
                    eq(PaymentTransactionStatus.SUCCESS.toString()),
                    anyString(),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()));
    ArgumentCaptor<PaymentDomainEvent> evtCaptor =
        ArgumentCaptor.forClass(PaymentDomainEvent.class);
    Allure.step(
        "Verify event produced", () -> verify(eventProducer, times(1)).send(evtCaptor.capture()));
    Allure.addAttachment(
        "Sent PaymentDomainEvent (payment success)",
        "application/json",
        objectMapper.writeValueAsString(evtCaptor.getValue()));
    Allure.step(
        "Verify event type", () -> assertEquals("payment.success", evtCaptor.getValue().getType()));
  }

  @Test
  @DisplayName("Handles payment failed and updates request status")
  @Story("Payment Saga Orchestrator")
  void handles_payment_failed_and_updates_request_status() throws Exception {
    UUID txId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    PaymentTransactionDto txDto = new PaymentTransactionDto();
    txDto.setId(txId);
    txDto.setPaymentRequestId(requestId);
    when(transactionService.getTransactionByExternalId(eq("ext-tx-2")))
        .thenReturn(Optional.of(txDto));

    PaymentCallbackEvent event = new PaymentCallbackEvent();
    event.setType(PaymentCallbackType.PAYMENT_FAILED);
    event.setCorrelationId("corr-pay-2");
    event.setExternalTransactionId("ext-tx-2");
    event.setErrorCode("ERR");
    event.setErrorMessage("oops");

    orchestrator.handle(event);

    verify(transactionService, times(1)).markAsFailed(eq(txId), eq("ERR"), eq("oops"));
    verify(requestService, times(1))
        .updateStatus(eq(requestId), eq(PaymentRequestStatus.FAILED), eq("ERR: oops"));
    verify(auditLogService, times(1))
        .logTransactionAction(
            eq(txId),
            eq("PAYMENT_FAILED"),
            eq(PaymentTransactionStatus.PROCESSING.toString()),
            eq(PaymentTransactionStatus.FAILED.toString()),
            eq("ERR: oops"),
            isNull(),
            isNull(),
            isNull(),
            isNull());
    ArgumentCaptor<PaymentDomainEvent> evtCaptor =
        ArgumentCaptor.forClass(PaymentDomainEvent.class);
    verify(eventProducer, times(1)).send(evtCaptor.capture());
    Allure.addAttachment(
        "Sent PaymentDomainEvent (payment failed)",
        "application/json",
        objectMapper.writeValueAsString(evtCaptor.getValue()));
    assertEquals("payment.failed", evtCaptor.getValue().getType());
  }

  @Test
  @DisplayName("Handles request rejected and updates status")
  @Story("Payment Saga Orchestrator")
  void handles_request_rejected_and_updates_status() throws Exception {
    UUID requestId = UUID.randomUUID();
    PaymentRequestDto dto = new PaymentRequestDto();
    dto.setId(requestId);
    when(requestService.updateStatus(eq(requestId), eq(PaymentRequestStatus.REJECTED), anyString()))
        .thenReturn(dto);

    PaymentCallbackEvent event = new PaymentCallbackEvent();
    event.setType(PaymentCallbackType.REQUEST_REJECTED);
    event.setCorrelationId("corr-req-2");
    event.setPaymentRequestId(requestId);
    event.setErrorCode("REJ");
    event.setErrorMessage("invalid details");

    orchestrator.handle(event);

    verify(requestService, times(1))
        .updateStatus(eq(requestId), eq(PaymentRequestStatus.REJECTED), eq("REJ: invalid details"));
    verify(auditLogService, times(1))
        .logPaymentRequestAction(
            eq(requestId),
            eq("REQUEST_REJECTED"),
            isNull(),
            eq(PaymentRequestStatus.REJECTED.toString()),
            anyString(),
            isNull(),
            isNull(),
            isNull(),
            isNull());
    ArgumentCaptor<PaymentDomainEvent> evtCaptor =
        ArgumentCaptor.forClass(PaymentDomainEvent.class);
    verify(eventProducer, times(1)).send(evtCaptor.capture());
    Allure.addAttachment(
        "Sent PaymentDomainEvent (request rejected)",
        "application/json",
        objectMapper.writeValueAsString(evtCaptor.getValue()));
    assertEquals("request.rejected", evtCaptor.getValue().getType());
  }

  @Test
  @DisplayName("Handles refund failed and logs failure")
  @Story("Payment Saga Orchestrator")
  void handles_refund_failed_and_logs_failure() throws Exception {
    UUID refundId = UUID.randomUUID();

    PaymentRefundDto refundDto = Allure.step("Create refund DTO", PaymentRefundDto::new);
    refundDto.setId(refundId);
    Allure.step(
        "Mock refund service",
        () ->
            when(refundService.getRefundByExternalId(eq("ext-ref-2")))
                .thenReturn(Optional.of(refundDto)));

    PaymentCallbackEvent event =
        Allure.step("Create refund failed event", PaymentCallbackEvent::new);
    Allure.step(
        "Set event properties",
        () -> {
          event.setType(PaymentCallbackType.REFUND_FAILED);
          event.setCorrelationId("corr-ref-2");
          event.setExternalRefundId("ext-ref-2");
          event.setErrorCode("RF_ERR");
          event.setErrorMessage("gateway error");
        });

    Allure.step("Handle refund failed event", () -> orchestrator.handle(event));

    Allure.step(
        "Verify refund service marked as failed",
        () ->
            verify(refundService, times(1))
                .markAsFailed(eq(refundId), eq("RF_ERR"), eq("gateway error")));
    Allure.step(
        "Verify audit log service logged refund failed action",
        () ->
            verify(auditLogService, times(1))
                .logRefundAction(
                    eq(refundId),
                    eq("REFUND_FAILED"),
                    eq(PaymentTransactionStatus.PROCESSING.toString()),
                    eq(PaymentTransactionStatus.FAILED.toString()),
                    eq("RF_ERR: gateway error"),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()));
    Allure.step(
        "Verify event producer sent refund failed event",
        () -> {
          ArgumentCaptor<PaymentDomainEvent> evtCaptor =
              ArgumentCaptor.forClass(PaymentDomainEvent.class);
          verify(eventProducer, times(1)).send(evtCaptor.capture());
          Allure.addAttachment(
              "Sent PaymentDomainEvent (refund failed)",
              "application/json",
              objectMapper.writeValueAsString(evtCaptor.getValue()));
          assertEquals("refund.failed", evtCaptor.getValue().getType());
        });
  }

  @Test
  @DisplayName("Handles refund success and sets full refund status")
  @Story("Payment Saga Orchestrator")
  void handles_refund_success_and_sets_full_status() {
    UUID txId = UUID.randomUUID();
    UUID refundId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    PaymentRefundDto refundDto = Allure.step("Create refund DTO", PaymentRefundDto::new);
    Allure.step(
        "Set refund properties",
        () -> {
          refundDto.setId(refundId);
          refundDto.setPaymentTransactionId(txId);
          refundDto.setRefundAmount(new BigDecimal("100.00"));
        });
    Allure.step(
        "Mock refund service",
        () ->
            when(refundService.getRefundByExternalId(eq("ext-ref-3")))
                .thenReturn(Optional.of(refundDto)));

    PaymentTransactionDto txDto =
        Allure.step("Create transaction DTO", PaymentTransactionDto::new);
    Allure.step(
        "Set transaction properties",
        () -> {
          txDto.setId(txId);
          txDto.setAmount(new BigDecimal("100.00"));
          txDto.setPaymentRequestId(requestId);
        });
    Allure.step(
        "Mock transaction service",
        () -> when(transactionService.getTransactionById(eq(txId))).thenReturn(Optional.of(txDto)));

    PaymentCallbackEvent event =
        Allure.step("Create refund success event", PaymentCallbackEvent::new);
    Allure.step(
        "Set event properties",
        () -> {
          event.setType(PaymentCallbackType.REFUND_SUCCESS);
          event.setCorrelationId("corr-ref-3");
          event.setExternalRefundId("ext-ref-3");
          event.setGatewayName("MockGateway");
          event.setReceivedAt(LocalDateTime.now());
        });
    Allure.step("Set gateway response", () -> event.setGatewayResponse(Map.of("status", "ok")));

    Allure.step("Handle refund success event", () -> orchestrator.handle(event));

    Allure.step(
        "Verify refund service marked as processed",
        () ->
            verify(refundService, times(1))
                .markAsProcessed(eq(refundId), eq("ext-ref-3"), anyMap()));
    Allure.step(
        "Verify request service updated status to refunded",
        () ->
            verify(requestService, times(1))
                .updateStatus(eq(requestId), eq(PaymentRequestStatus.REFUNDED), anyString()));
    Allure.step(
        "Verify audit log service logged refund success action",
        () ->
            verify(auditLogService, times(1))
                .logRefundAction(
                    eq(refundId),
                    eq("REFUND_SUCCESS"),
                    eq(PaymentTransactionStatus.PROCESSING.toString()),
                    eq(PaymentTransactionStatus.SUCCESS.toString()),
                    anyString(),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()));
    Allure.step(
        "Verify event producer sent refund success event",
        () -> {
          ArgumentCaptor<PaymentDomainEvent> evtCaptor =
              ArgumentCaptor.forClass(PaymentDomainEvent.class);
          verify(eventProducer, times(1)).send(evtCaptor.capture());
          Allure.addAttachment(
              "Sent PaymentDomainEvent (refund success)",
              "application/json",
              objectMapper.writeValueAsString(evtCaptor.getValue()));
          assertEquals("refund.success", evtCaptor.getValue().getType());
        });
  }

  @Test
  @DisplayName("Missing transaction on payment success produces no event")
  @Story("Payment Saga Orchestrator")
  void missing_transaction_on_payment_success_produces_no_event() {
    Allure.step(
        "Mock transaction service to return empty for missing transaction",
        () ->
            when(transactionService.getTransactionByExternalId(eq("ext-tx-missing")))
                .thenReturn(Optional.empty()));

    PaymentCallbackEvent event =
        Allure.step("Create payment success event", PaymentCallbackEvent::new);
    Allure.step(
        "Set event properties",
        () -> {
          event.setType(PaymentCallbackType.PAYMENT_SUCCESS);
          event.setCorrelationId("corr-pay-miss");
          event.setExternalTransactionId("ext-tx-missing");
        });

    Allure.step("Handle payment success event", () -> orchestrator.handle(event));

    Allure.step(
        "Verify event producer never sent any event",
        () -> verify(eventProducer, never()).send(any()));
    Allure.step(
        "Set event properties",
        () -> {
          event.setType(PaymentCallbackType.PAYMENT_SUCCESS);
          event.setCorrelationId("corr-pay-miss");
          event.setExternalTransactionId("ext-tx-missing");
        });
    Allure.step("Handle payment success event", () -> orchestrator.handle(event));

    Allure.step(
        "Verify event producer never sent any event",
        () -> verify(eventProducer, never()).send(any()));
    Allure.step(
        "Verify transaction service never marked as processed",
        () -> verify(transactionService, never()).markAsProcessed(any(), any(), any()));
    Allure.step(
        "Verify request service never marked as paid",
        () -> verify(requestService, never()).markAsPaid(any(), any()));
  }

  @Test
  @DisplayName("Handles request approved via token resolution")
  @Story("Payment Saga Orchestrator")
  void handles_request_approved_via_token_resolution() throws Exception {
    UUID requestId = Allure.step("Generate random request ID", () -> UUID.randomUUID());
    PaymentRequestDto dto =
        Allure.step("Create payment request DTO", PaymentRequestDto::new);
    Allure.step("Set request properties", () -> dto.setId(requestId));
    Allure.step(
        "Mock request service to return request for token",
        () ->
            when(requestService.getPaymentRequestByToken(eq("tok-123")))
                .thenReturn(Optional.of(dto)));
    Allure.step(
        "Mock request service to update status to approved",
        () ->
            when(requestService.updateStatus(
                    eq(requestId), eq(PaymentRequestStatus.APPROVED), anyString()))
                .thenReturn(dto));

    PaymentCallbackEvent event =
        Allure.step("Create request approved event", PaymentCallbackEvent::new);
    Allure.step(
        "Set event properties",
        () -> {
          event.setType(PaymentCallbackType.REQUEST_APPROVED);
          event.setCorrelationId("corr-req-token");
          event.setPaymentToken("tok-123");
          event.setGatewayName("MockGateway");
        });

    Allure.step("Handle request approved event", () -> orchestrator.handle(event));

    Allure.step(
        "Verify request service updated status to approved",
        () ->
            verify(requestService, times(1))
                .updateStatus(eq(requestId), eq(PaymentRequestStatus.APPROVED), anyString()));
    Allure.step(
        "Verify audit log service logged request approved action",
        () ->
            verify(auditLogService, times(1))
                .logPaymentRequestAction(
                    eq(requestId),
                    eq("REQUEST_APPROVED"),
                    isNull(),
                    eq(PaymentRequestStatus.APPROVED.toString()),
                    anyString(),
                    isNull(),
                    isNull(),
                    isNull(),
                    isNull()));
    Allure.step(
        "Verify event producer sent request approved event",
        () -> {
          ArgumentCaptor<PaymentDomainEvent> evtCaptor =
              ArgumentCaptor.forClass(PaymentDomainEvent.class);
          verify(eventProducer, times(1)).send(evtCaptor.capture());
          Allure.addAttachment(
              "Sent PaymentDomainEvent (request approved via token)",
              "application/json",
              objectMapper.writeValueAsString(evtCaptor.getValue()));
          assertEquals("request.approved", evtCaptor.getValue().getType());
        });
  }
}
