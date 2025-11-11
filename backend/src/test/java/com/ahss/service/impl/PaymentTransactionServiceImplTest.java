package com.ahss.service.impl;

import com.ahss.dto.request.ProcessPaymentDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.entity.PaymentTransaction;
import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.integration.PaymentIntegrator;
import com.ahss.integration.PaymentIntegratorFactory;
import com.ahss.kafka.producer.PaymentCallbackProducer;
import com.ahss.repository.PaymentTransactionRepository;
import com.ahss.service.PaymentAuditLogService;
import com.ahss.service.PaymentRequestService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;

@Epic("Payment Lifecycle")
@Feature("Payment Transactions")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {PaymentTransactionServiceImpl.class, PaymentTransactionServiceImplTest.TestConfig.class})
public class PaymentTransactionServiceImplTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
      mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return mapper;
    }
  }

  @MockBean private PaymentTransactionRepository transactionRepository;
  @MockBean private PaymentAuditLogService auditLogService;
  @MockBean private PaymentRequestService paymentRequestService;
  @MockBean private PaymentIntegratorFactory integratorFactory;
  @MockBean private PaymentCallbackProducer paymentCallbackProducer;

  @Autowired private PaymentTransactionServiceImpl service;

  @Test
  @Story("Process payment persists and maps DTO")
  @Severity(SeverityLevel.CRITICAL)
  void processPayment_createsPendingTransaction() {
    // Arrange: mock payment request found by token
    String token = "tok_123";
    PaymentRequestDto requestDto = new PaymentRequestDto();
    UUID requestId = UUID.randomUUID();
    requestDto.setId(requestId);
    requestDto.setPaymentToken(token);
    requestDto.setAmount(new BigDecimal("10.00"));
    requestDto.setCurrency("USD");
    when(paymentRequestService.getPaymentRequestByToken(eq(token)))
        .thenReturn(Optional.of(requestDto));

    // First save: assign an ID to the new transaction
    when(transactionRepository.save(any(PaymentTransaction.class)))
        .thenAnswer(
            inv -> {
              PaymentTransaction tx = inv.getArgument(0);
              if (tx.getId() == null) {
                tx.setId(UUID.randomUUID());
              }
              return tx;
            });

    // Mock integrator and response
    PaymentIntegrator integrator = mock(PaymentIntegrator.class);
    when(integratorFactory.getIntegrator(eq(PaymentMethodType.CREDIT_CARD), eq("Stripe")))
        .thenReturn(integrator);
    PaymentResponseDto response = new PaymentResponseDto();
    response.setSuccess(true);
    response.setGatewayName("Stripe");
    response.setExternalTransactionId("ext_123");
    response.setProcessedAt(LocalDateTime.now());
    response.setAmount(new BigDecimal("10.00"));
    response.setCurrency("USD");
    when(integrator.initiatePayment(eq(requestDto), any(PaymentTransactionDto.class)))
        .thenReturn(response);

    // Build valid process DTO
    ProcessPaymentDto processDto = new ProcessPaymentDto();
    processDto.setPaymentToken(token);
    processDto.setPaymentMethod(PaymentMethodType.CREDIT_CARD);
    processDto.setPaymentMethodDetails(Map.of("card", "4111111111111111"));
    processDto.setGatewayName("Stripe");

    // Act
    PaymentTransactionDto dto = service.processPayment(processDto);

    // Assert
    assertNotNull(dto.getId());
    assertEquals(PaymentTransactionStatus.SUCCESS, dto.getTransactionStatus());
    assertEquals("ext_123", dto.getExternalTransactionId());
    verify(transactionRepository, times(2)).save(any(PaymentTransaction.class));
    verify(integratorFactory).getIntegrator(PaymentMethodType.CREDIT_CARD, "Stripe");
    verify(paymentCallbackProducer).send(any());
  }

  @Test
  @Story("Update status persists change and maps DTO")
  @Severity(SeverityLevel.NORMAL)
  void updateTransactionStatus_setsStatus_andReturnsDto() {
    UUID id = UUID.randomUUID();
    PaymentTransaction existing = new PaymentTransaction();
    existing.setId(id);
    existing.setTransactionStatus(PaymentTransactionStatus.PENDING);
    when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));
    when(transactionRepository.save(any(PaymentTransaction.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PaymentTransactionDto result =
        service.updateTransactionStatus(id, PaymentTransactionStatus.SUCCESS, "ok");
    assertEquals(PaymentTransactionStatus.SUCCESS, result.getTransactionStatus());
  }

  @Test
  @Story("Mark as processed sets external id and SUCCESS")
  @Severity(SeverityLevel.NORMAL)
  void markAsProcessed_updatesFields() {
    UUID id = UUID.randomUUID();
    PaymentTransaction existing = new PaymentTransaction();
    existing.setId(id);
    existing.setTransactionStatus(PaymentTransactionStatus.PENDING);
    when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));
    when(transactionRepository.save(any(PaymentTransaction.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Map<String, Object> gatewayResponse = Map.of("ok", true);
    PaymentTransactionDto dto = service.markAsProcessed(id, "ext-999", gatewayResponse);
    assertEquals(PaymentTransactionStatus.SUCCESS, dto.getTransactionStatus());
    assertEquals("ext-999", dto.getExternalTransactionId());
  }

  @Test
  @Story("Mark as failed sets error fields and FAILED")
  @Severity(SeverityLevel.NORMAL)
  void markAsFailed_updatesErrorFields() {
    UUID id = UUID.randomUUID();
    PaymentTransaction existing = new PaymentTransaction();
    existing.setId(id);
    existing.setTransactionStatus(PaymentTransactionStatus.PENDING);
    when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));
    when(transactionRepository.save(any(PaymentTransaction.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    PaymentTransactionDto dto = service.markAsFailed(id, "E001", "Gateway timeout");
    assertEquals(PaymentTransactionStatus.FAILED, dto.getTransactionStatus());
    assertEquals("E001", dto.getErrorCode());
    assertEquals("Gateway timeout", dto.getErrorMessage());
  }

  @Test
  @Story("Retry sets PENDING, clears errors, increments retry")
  @Severity(SeverityLevel.CRITICAL)
  void retryTransaction_setsPending_andClearsErrors() {
    UUID id = UUID.randomUUID();
    PaymentTransaction mockTx = mock(PaymentTransaction.class);
    when(mockTx.getTransactionStatus()).thenReturn(PaymentTransactionStatus.FAILED);
    when(mockTx.canBeRetried()).thenReturn(true);
    when(transactionRepository.findById(id)).thenReturn(Optional.of(mockTx));

    PaymentTransaction saved = new PaymentTransaction();
    saved.setId(id);
    saved.setTransactionStatus(PaymentTransactionStatus.PENDING);
    saved.setErrorCode(null);
    saved.setErrorMessage(null);
    when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(saved);

    PaymentTransactionDto dto = service.retryTransaction(id);
    assertEquals(PaymentTransactionStatus.PENDING, dto.getTransactionStatus());
    assertNull(dto.getErrorCode());
    assertNull(dto.getErrorMessage());
    verify(mockTx).incrementRetryCount();
  }

  @Test
  @Story("Cancel sets CANCELLED")
  @Severity(SeverityLevel.MINOR)
  void cancelTransaction_setsCancelled() {
    UUID id = UUID.randomUUID();
    PaymentTransaction existing = new PaymentTransaction();
    existing.setId(id);
    existing.setTransactionStatus(PaymentTransactionStatus.PENDING);
    when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));

    service.cancelTransaction(id, "no need");
    verify(transactionRepository)
        .save(argThat(tx -> tx.getTransactionStatus() == PaymentTransactionStatus.CANCELLED));
  }

  @Test
  @Story("List by request converts list to page")
  @Severity(SeverityLevel.TRIVIAL)
  void getTransactionsByPaymentRequest_convertsToPage() {
    UUID reqId = UUID.randomUUID();
    PaymentTransaction t1 = new PaymentTransaction();
    PaymentTransaction t2 = new PaymentTransaction();
    when(transactionRepository.findByPaymentRequestId(reqId)).thenReturn(List.of(t1, t2));

    Page<PaymentTransactionDto> page =
        service.getTransactionsByPaymentRequest(reqId, PageRequest.of(0, 1));
    assertEquals(1, page.getSize());
    assertEquals(2, page.getTotalElements());
  }

  @Test
  @Story("Page by status maps DTOs")
  @Severity(SeverityLevel.TRIVIAL)
  void getTransactionsByStatus_mapsPage() {
    PaymentTransaction tx = new PaymentTransaction();
    tx.setTransactionStatus(PaymentTransactionStatus.PENDING);
    when(transactionRepository.findByTransactionStatus(eq(PaymentTransactionStatus.PENDING), any()))
        .thenReturn(new PageImpl<>(List.of(tx)));

    Page<PaymentTransactionDto> page =
        service.getTransactionsByStatus(PaymentTransactionStatus.PENDING, PageRequest.of(0, 10));
    assertEquals(1, page.getTotalElements());
    assertEquals(PaymentTransactionStatus.PENDING, page.getContent().get(0).getTransactionStatus());
  }

  @Test
  @Story("Exists lookups return booleans")
  @Severity(SeverityLevel.TRIVIAL)
  void exists_checks_delegateToRepository() {
    when(transactionRepository.existsByTransactionCode("TX-1")).thenReturn(true);
    when(transactionRepository.existsByExternalTransactionId("EXT-1")).thenReturn(false);
    assertTrue(service.existsByTransactionCode("TX-1"));
    assertFalse(service.existsByExternalTransactionId("EXT-1"));
  }

  @Test
  @Story("Sum amount by status and currency delegates to repository")
  @Severity(SeverityLevel.TRIVIAL)
  void sumAmountByStatusAndCurrency_delegates() {
    when(transactionRepository.sumAmountByStatusAndCurrency(
            PaymentTransactionStatus.SUCCESS, "USD"))
        .thenReturn(new BigDecimal("123.45"));
    assertEquals(
        new BigDecimal("123.45"),
        service.sumAmountByStatusAndCurrency(PaymentTransactionStatus.SUCCESS, "USD"));
  }

  @Test
  @Story("Filter successful transactions by request id")
  @Severity(SeverityLevel.NORMAL)
  void getSuccessfulTransactionsByRequest_filtersSuccess() {
    UUID reqId = UUID.randomUUID();
    PaymentTransaction s1 = new PaymentTransaction();
    s1.setTransactionStatus(PaymentTransactionStatus.SUCCESS);
    PaymentTransaction f1 = new PaymentTransaction();
    f1.setTransactionStatus(PaymentTransactionStatus.FAILED);
    when(transactionRepository.findByPaymentRequestId(reqId)).thenReturn(List.of(s1, f1));

    List<PaymentTransactionDto> result = service.getSuccessfulTransactionsByRequest(reqId);
    assertEquals(1, result.size());
    assertEquals(PaymentTransactionStatus.SUCCESS, result.get(0).getTransactionStatus());
  }
}
