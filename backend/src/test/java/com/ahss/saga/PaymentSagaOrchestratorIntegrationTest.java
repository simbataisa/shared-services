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
    PaymentRequestDto dto = Allure.step("Create PaymentRequestDto", () -> new PaymentRequestDto());
    dto.setId(requestId);
    Allure.step(
        "Mock PaymentRequestService.updateStatus",
        () ->
            when(requestService.updateStatus(
                    eq(requestId), eq(PaymentRequestStatus.APPROVED), anyString()))
                .thenReturn(dto));

    PaymentCallbackEvent event =
        Allure.step("Create PaymentCallbackEvent", () -> new PaymentCallbackEvent());
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
        () -> Allure.addAttachment("Sent PaymentDomainEvent (request approved)", "application/json", objectMapper.writeValueAsString(evtCaptor.getValue())));
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
        Allure.step("Create PaymentRefundDto", () -> new PaymentRefundDto());
    refundDto.setId(refundId);
    refundDto.setPaymentTransactionId(txId);
    refundDto.setRefundAmount(new BigDecimal("50.00"));
    Allure.step(
        "Mock PaymentRefundService.getRefundByExternalId",
        () ->
            when(refundService.getRefundByExternalId(eq("ext-ref-1")))
                .thenReturn(Optional.of(refundDto)));

    PaymentTransactionDto txDto =
        Allure.step("Create PaymentTransactionDto", () -> new PaymentTransactionDto());
    txDto.setId(txId);
    txDto.setAmount(new BigDecimal("100.00"));
    txDto.setPaymentRequestId(requestId);
    Allure.step(
        "Mock PaymentTransactionService.getTransactionById",
        () -> when(transactionService.getTransactionById(eq(txId))).thenReturn(Optional.of(txDto)));

    PaymentCallbackEvent event =
        Allure.step("Create PaymentCallbackEvent", () -> new PaymentCallbackEvent());
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
        () -> Allure.addAttachment("Sent PaymentDomainEvent (refund success)", "application/json", objectMapper.writeValueAsString(evtCaptor2.getValue())));
  }
}
