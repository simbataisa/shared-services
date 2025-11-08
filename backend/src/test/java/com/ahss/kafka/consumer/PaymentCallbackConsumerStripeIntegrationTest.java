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
public class PaymentCallbackConsumerStripeIntegrationTest {

  @Autowired private PaymentCallbackConsumer consumer;

  @Autowired private PaymentSagaOrchestrator orchestrator; // ensure real orchestrator runs

  @MockBean private PaymentTransactionService transactionService;

  @MockBean private PaymentRefundService refundService;

  @MockBean private PaymentRequestService requestService;

  @MockBean private PaymentAuditLogService auditLogService;

  @MockBean private com.ahss.kafka.producer.PaymentEventProducer eventProducer;

  @Test
  @DisplayName("Consumes Stripe payment_intent.succeeded and processes payment")
  @Story("Stripe Payment Intent Succeeded")
  void stripe_payment_intent_succeeded_triggers_processing() {
    String externalTxId = "pi_abc123";
    String stripePayload =
        Allure.step(
            "Create Stripe Payment Intent Succeeded Event",
            () ->
                "{"
                    + "\"id\":\"evt_123\","
                    + "\"type\":\"payment_intent.succeeded\","
                    + "\"data\":{"
                    + "  \"object\": {\"id\": \""
                    + externalTxId
                    + "\", \"amount_received\": 5000, \"currency\": \"usd\"}"
                    + " }"
                    + "}");

    UUID txId = UUID.randomUUID();
    UUID reqId = UUID.randomUUID();
    PaymentTransactionDto txDto =
        Allure.step("Create Payment Transaction DTO", PaymentTransactionDto::new);
    txDto.setId(txId);
    txDto.setPaymentRequestId(reqId);
    Allure.step(
        "Mock Transaction Service to return Payment Transaction DTO",
        () ->
            when(transactionService.getTransactionByExternalId(eq(externalTxId)))
                .thenReturn(Optional.of(txDto)));

    Allure.step(
        "Consume Stripe Payment Intent Succeeded Event", () -> consumer.onMessage(stripePayload));

    Allure.step(
        "Verify Transaction Service is called to get transaction by external ID",
        () -> verify(transactionService, times(1)).getTransactionByExternalId(eq(externalTxId)));
    Allure.step(
        "Verify Transaction Service is called to mark transaction as processed",
        () ->
            verify(transactionService, times(1))
                .markAsProcessed(eq(txId), eq(externalTxId), any(Map.class)));
    Allure.step(
        "Verify Request Service is called to mark request as paid",
        () -> verify(requestService, times(1)).markAsPaid(eq(reqId), any(LocalDateTime.class)));
    Allure.step(
        "Verify Audit Log Service is called to log transaction action",
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
        "Verify Event Producer is called to send payment success event",
        () -> {
          ArgumentCaptor<PaymentDomainEvent> eventCaptor =
              ArgumentCaptor.forClass(PaymentDomainEvent.class);
          verify(eventProducer, times(1)).send(eventCaptor.capture());
          PaymentDomainEvent domainEvent = eventCaptor.getValue();
          org.junit.jupiter.api.Assertions.assertEquals("payment.success", domainEvent.getType());
        });
  }
}
