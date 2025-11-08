package com.ahss.kafka.consumer;

import com.ahss.SharedServicesApplication;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.kafka.event.PaymentDomainEvent;
import com.ahss.saga.PaymentSagaOrchestrator;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRefundService;
import com.ahss.service.PaymentRequestService;
import com.ahss.service.PaymentTransactionService;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SharedServicesApplication.class)
@org.springframework.test.context.ActiveProfiles("test")
@Epic("Saga")
@Feature("Payment Callback Consumer")
public class PaymentCallbackConsumerPayPalIntegrationTest {

  @Autowired private PaymentCallbackConsumer consumer;

  @Autowired private PaymentSagaOrchestrator orchestrator; // ensure real orchestrator runs

  @MockBean private PaymentTransactionService transactionService;

  @MockBean private PaymentRefundService refundService;

  @MockBean private PaymentRequestService requestService;

  @MockBean private PaymentAuditLogService auditLogService;

  @MockBean private com.ahss.kafka.producer.PaymentEventProducer eventProducer;

  @Test
  @DisplayName("Consumes PayPal PAYMENT.SALE.COMPLETED and processes payment")
  @Story("PayPal Payment Callback")
  void paypal_sale_completed_triggers_processing() {
    String externalTxId = "sale_123";
    String paypalPayload =
        Allure.step(
            "Create PayPal payload",
            () ->
                "{"
                    + "\"id\":\"WH-987\","
                    + "\"event_type\":\"PAYMENT.SALE.COMPLETED\","
                    + "\"resource\": {\"id\": \""
                    + externalTxId
                    + "\", \"amount\": {\"value\": \"50.00\", \"currency_code\": \"USD\"}, \"create_time\": \"2024-10-20T12:00:00Z\" }"
                    + "}");

    UUID txId = UUID.randomUUID();
    UUID reqId = UUID.randomUUID();
    PaymentTransactionDto txDto =
        Allure.step("Create PaymentTransactionDto", PaymentTransactionDto::new);
    txDto.setId(txId);
    txDto.setPaymentRequestId(reqId);
    Allure.step(
        "Mock transactionService.getTransactionByExternalId",
        () ->
            when(transactionService.getTransactionByExternalId(eq(externalTxId)))
                .thenReturn(Optional.of(txDto)));

    Allure.step("Consume PayPal payload", () -> consumer.onMessage(paypalPayload));

    Allure.step(
        "Verify transactionService.getTransactionByExternalId",
        () -> verify(transactionService, times(1)).getTransactionByExternalId(eq(externalTxId)));
    Allure.step(
        "Verify transactionService.markAsProcessed",
        () ->
            verify(transactionService, times(1))
                .markAsProcessed(eq(txId), eq(externalTxId), any(Map.class)));
    Allure.step(
        "Verify requestService.markAsPaid",
        () -> verify(requestService, times(1)).markAsPaid(eq(reqId), any(LocalDateTime.class)));
    Allure.step(
        "Verify auditLogService.logTransactionAction",
        () ->
            verify(auditLogService, times(1))
                .logTransactionAction(
                    eq(txId),
                    eq("PAYMENT_SUCCESS"),
                    anyString(),
                    anyString(),
                    anyString(),
                    any(),
                    any(),
                    any(),
                    any()));

    Allure.step(
        "Verify eventProducer.send",
        () -> {
          ArgumentCaptor<PaymentDomainEvent> eventCaptor =
              ArgumentCaptor.forClass(PaymentDomainEvent.class);
          verify(eventProducer, times(1)).send(eventCaptor.capture());
          PaymentDomainEvent domainEvent = eventCaptor.getValue();
          Assertions.assertEquals("payment.success", domainEvent.getType());
        });
  }
}
