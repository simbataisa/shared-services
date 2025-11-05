package com.ahss.controller;

import com.ahss.dto.request.CreatePaymentRequestDto;
import com.ahss.dto.request.ProcessPaymentDto;
import com.ahss.dto.request.UpdatePaymentRequestDto;
import com.ahss.dto.response.PaymentAuditLogDto;
import com.ahss.dto.response.PaymentRefundDto;
import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentRequestStatus;
import com.ahss.service.PaymentRequestService;
import com.ahss.service.PaymentTransactionService;
import com.ahss.service.PaymentRefundService;
import com.ahss.service.PaymentAuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import io.qameta.allure.Allure;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("Payment Lifecycle")
@Feature("Payment Requests")
@Owner("backend")
class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PaymentRequestService paymentRequestService;
  @MockBean private PaymentTransactionService paymentTransactionService;
  @MockBean private PaymentRefundService paymentRefundService;
  @MockBean private PaymentAuditLogService auditLogService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    // No runtime labels; using annotations instead
  }

  private PaymentRequestDto requestDto(UUID id) {
    PaymentRequestDto dto = new PaymentRequestDto();
    dto.setId(id);
    dto.setTitle("Test Payment");
    dto.setAmount(new BigDecimal("123.45"));
    dto.setCurrency("USD");
    dto.setPayerName("John Doe");
    dto.setPayerEmail("john@example.com");
    dto.setAllowedPaymentMethods(List.of(PaymentMethodType.CREDIT_CARD, PaymentMethodType.PAYPAL));
    dto.setStatus(PaymentRequestStatus.PENDING);
    dto.setTenantId(10L);
    return dto;
  }

  private PaymentTransactionDto transactionDto(UUID id) {
    PaymentTransactionDto dto = new PaymentTransactionDto();
    dto.setId(id);
    dto.setTransactionCode("TX-" + id);
    dto.setAmount(new BigDecimal("123.45"));
    dto.setCurrency("USD");
    return dto;
  }

  @Test
  @DisplayName("POST /payments/requests returns 201 with payload")
  @Story("Create payment request")
  @Severity(SeverityLevel.CRITICAL)
  void create_payment_request_success_returns_201() throws Exception {
    UUID id = UUID.randomUUID();
    when(paymentRequestService.createPaymentRequest(
            org.mockito.ArgumentMatchers.any(CreatePaymentRequestDto.class)))
        .thenReturn(requestDto(id));
    CreatePaymentRequestDto req = new CreatePaymentRequestDto();
    req.setTitle("Test Payment");
    req.setAmount(new BigDecimal("123.45"));
    req.setCurrency("USD");
    req.setPayerName("John Doe");
    req.setPayerEmail("john@example.com");
    req.setAllowedPaymentMethods(List.of(PaymentMethodType.CREDIT_CARD, PaymentMethodType.PAYPAL));
    req.setTenantId(10L);
    String body = objectMapper.writeValueAsString(req);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "POST /api/v1/payments/requests",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/payments/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment request created successfully")))
                    .andExpect(jsonPath("$.data.title", is("Test Payment")))
                    .andExpect(jsonPath("$.data.amount", is(123.45)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Create payment request returns 400 for invalid tenant")
  @Severity(SeverityLevel.MINOR)
  void create_payment_request_bad_request_returns_400() throws Exception {
    Allure.step(
        "Stub service to throw 'Invalid tenant'",
        () ->
            when(paymentRequestService.createPaymentRequest(
                    org.mockito.ArgumentMatchers.any(CreatePaymentRequestDto.class)))
                .thenThrow(new RuntimeException("Invalid tenant")));
    CreatePaymentRequestDto req = new CreatePaymentRequestDto();
    req.setTitle("Bad");
    req.setAmount(new BigDecimal("1.00"));
    req.setCurrency("USD");
    req.setPayerName("Jane");
    req.setPayerEmail("jane@example.com");
    req.setAllowedPaymentMethods(List.of(PaymentMethodType.CREDIT_CARD));
    req.setTenantId(999L);
    String body = objectMapper.writeValueAsString(req);

    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "POST /api/v1/payments/requests",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/payments/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Invalid tenant")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get payment request by ID returns 200 when found")
  @Severity(SeverityLevel.NORMAL)
  void get_payment_request_by_id_found_returns_200() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub service to return payment request for id=" + id,
        () ->
            when(paymentRequestService.getPaymentRequestById(eq(id)))
                .thenReturn(Optional.of(requestDto(id))));

    var result =
        Allure.step(
            "GET /api/v1/payments/requests/" + id,
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/requests/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(id.toString())))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get payment request by ID returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void get_payment_request_by_id_not_found_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub service to return empty for id=" + id,
        () ->
            when(paymentRequestService.getPaymentRequestById(eq(id))).thenReturn(Optional.empty()));

    var result =
        Allure.step(
            "GET /api/v1/payments/requests/" + id,
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/requests/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Payment request not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update payment request returns 200 and payload")
  @Severity(SeverityLevel.NORMAL)
  void update_payment_request_success_returns_200() throws Exception {
    UUID id = UUID.randomUUID();
    PaymentRequestDto updated = requestDto(id);
    updated.setTitle("Updated Title");
    Allure.step(
        "Stub updatePaymentRequest to return updated DTO",
        () ->
            when(paymentRequestService.updatePaymentRequest(
                    eq(id), org.mockito.ArgumentMatchers.any(UpdatePaymentRequestDto.class)))
                .thenReturn(updated));
    UpdatePaymentRequestDto req = new UpdatePaymentRequestDto();
    req.setTitle("Updated Title");
    req.setAmount(new BigDecimal("200.00"));
    String body = objectMapper.writeValueAsString(req);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "PUT /api/v1/payments/requests/" + id,
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/payments/requests/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment request updated successfully")))
                    .andExpect(jsonPath("$.data.title", is("Updated Title")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update payment request returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void update_payment_request_not_found_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub updatePaymentRequest to throw not found",
        () ->
            when(paymentRequestService.updatePaymentRequest(
                    eq(id), org.mockito.ArgumentMatchers.any(UpdatePaymentRequestDto.class)))
                .thenThrow(new RuntimeException("Payment request not found")));
    UpdatePaymentRequestDto req = new UpdatePaymentRequestDto();
    req.setTitle("Nope");
    String body = objectMapper.writeValueAsString(req);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "PUT /api/v1/payments/requests/" + id,
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/payments/requests/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update payment request returns 400 for invalid update")
  @Severity(SeverityLevel.MINOR)
  void update_payment_request_bad_request_returns_400() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub updatePaymentRequest to throw 'Invalid update'",
        () ->
            when(paymentRequestService.updatePaymentRequest(
                    eq(id), org.mockito.ArgumentMatchers.any(UpdatePaymentRequestDto.class)))
                .thenThrow(new RuntimeException("Invalid update")));
    UpdatePaymentRequestDto req = new UpdatePaymentRequestDto();
    req.setTitle("Bad");
    String body = objectMapper.writeValueAsString(req);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "PUT /api/v1/payments/requests/" + id,
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/payments/requests/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Invalid update")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Process payment returns 201 with transaction payload")
  @Severity(SeverityLevel.CRITICAL)
  void process_payment_success_returns_201() throws Exception {
    UUID txId = UUID.randomUUID();
    Allure.step(
        "Stub processPayment to return transaction DTO",
        () ->
            when(paymentTransactionService.processPayment(
                    org.mockito.ArgumentMatchers.any(ProcessPaymentDto.class)))
                .thenReturn(transactionDto(txId)));
    ProcessPaymentDto req = new ProcessPaymentDto();
    req.setPaymentToken("tok_abc");
    req.setPaymentMethod(PaymentMethodType.CREDIT_CARD);
    req.setPaymentMethodDetails(Map.of("card", "4242"));
    String body = objectMapper.writeValueAsString(req);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "POST /api/v1/payments/transactions/process",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/payments/transactions/process")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment processed successfully")))
                    .andExpect(jsonPath("$.data.id", is(txId.toString())))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions/process")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Process payment returns 400 for gateway error")
  @Severity(SeverityLevel.MINOR)
  void process_payment_bad_request_returns_400() throws Exception {
    Allure.step(
        "Stub processPayment to throw 'Gateway error'",
        () ->
            when(paymentTransactionService.processPayment(
                    org.mockito.ArgumentMatchers.any(ProcessPaymentDto.class)))
                .thenThrow(new RuntimeException("Gateway error")));
    ProcessPaymentDto req = new ProcessPaymentDto();
    req.setPaymentToken("tok_bad");
    req.setPaymentMethod(PaymentMethodType.PAYPAL);
    req.setPaymentMethodDetails(Map.of("foo", "bar"));
    String body = objectMapper.writeValueAsString(req);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
    var result =
        Allure.step(
            "POST /api/v1/payments/transactions/process",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/payments/transactions/process")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Gateway error")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions/process")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Retry transaction returns 404 when transaction missing")
  @Severity(SeverityLevel.MINOR)
  void retry_transaction_not_found_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub retryTransaction to throw not found",
        () ->
            doThrow(new RuntimeException("Transaction not found"))
                .when(paymentTransactionService)
                .retryTransaction(eq(id)));

    var result =
        Allure.step(
            "PATCH /api/v1/payments/transactions/" + id + "/retry",
            () ->
                mockMvc
                    .perform(patch("/api/v1/payments/transactions/" + id + "/retry"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("not found")))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/payments/transactions/" + id + "/retry")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Cancel transaction returns 404 when transaction missing")
  @Severity(SeverityLevel.MINOR)
  void cancel_transaction_not_found_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub cancelTransaction to throw not found",
        () ->
            doThrow(new RuntimeException("Transaction not found"))
                .when(paymentTransactionService)
                .cancelTransaction(eq(id), anyString()));

    var result =
        Allure.step(
            "PATCH /api/v1/payments/transactions/" + id + "/cancel",
            () ->
                mockMvc
                    .perform(patch("/api/v1/payments/transactions/" + id + "/cancel"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("not found")))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/payments/transactions/" + id + "/cancel")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("List payment requests returns 200 with page payload")
  @Severity(SeverityLevel.NORMAL)
  void list_payment_requests_returns_200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentRequestDto> page =
        new org.springframework.data.domain.PageImpl<>(
            List.of(requestDto(UUID.randomUUID())), pageable, 1);
    when(paymentRequestService.getAllPaymentRequests(eq(pageable))).thenReturn(page);

    var result =
        Allure.step(
            "GET /api/v1/payments/requests",
            () ->
                mockMvc
                    .perform(
                        get("/api/v1/payments/requests").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment requests retrieved successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/requests")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Approve payment request returns 200 when updated")
  @Severity(SeverityLevel.NORMAL)
  void approve_payment_request_returns_200() throws Exception {
    UUID id = UUID.randomUUID();
    PaymentRequestDto dto = requestDto(id);
    when(paymentRequestService.updatePaymentRequest(eq(id), any())).thenReturn(dto);

    var result =
        Allure.step(
            "PATCH /api/v1/payments/requests/" + id + "/approve",
            () ->
                mockMvc
                    .perform(patch("/api/v1/payments/requests/" + id + "/approve"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment request approved successfully")))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/payments/requests/" + id + "/approve")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Approve payment request returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void approve_payment_request_not_found_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Stub updatePaymentRequest to throw not found",
        () ->
            when(paymentRequestService.updatePaymentRequest(eq(id), any()))
                .thenThrow(new RuntimeException("Payment request not found")));

    var result =
        Allure.step(
            "PATCH /api/v1/payments/requests/" + id + "/approve",
            () ->
                mockMvc
                    .perform(patch("/api/v1/payments/requests/" + id + "/approve"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("not found")))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/payments/requests/" + id + "/approve")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("List transactions returns 200 with page payload")
  @Severity(SeverityLevel.NORMAL)
  void list_transactions_returns_200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentTransactionDto> page =
        Allure.step(
            "Create page of payment transactions",
            () ->
                new org.springframework.data.domain.PageImpl<>(
                    List.of(transactionDto(UUID.randomUUID())), pageable, 1));
    Allure.step(
        "Stub getAllTransactions to return page of payment transactions",
        () -> when(paymentTransactionService.getAllTransactions(eq(pageable))).thenReturn(page));

    var result =
        Allure.step(
            "GET /api/v1/payments/transactions",
            () ->
                mockMvc
                    .perform(
                        get("/api/v1/payments/transactions").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(
                        jsonPath("$.message", is("Payment transactions retrieved successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Retry transaction returns 200 when retried")
  @Severity(SeverityLevel.NORMAL)
  void retry_transaction_success_returns_200() throws Exception {
    UUID id = UUID.randomUUID();
    // no exception means success
    var result =
        Allure.step(
            "PATCH /api/v1/payments/transactions/" + id + "/retry",
            () ->
                mockMvc
                    .perform(patch("/api/v1/payments/transactions/" + id + "/retry"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(
                        jsonPath(
                            "$.message", is("Payment transaction retry initiated successfully")))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/payments/transactions/" + id + "/retry")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
    verify(paymentTransactionService, times(1)).retryTransaction(eq(id));
  }

  @Test
  @Story("List refunds returns 200 with page payload")
  @Severity(SeverityLevel.NORMAL)
  void list_refunds_returns_200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentRefundDto> page =
        Allure.step(
            "Create page of payment refunds",
            () ->
                new org.springframework.data.domain.PageImpl<>(
                    List.of(new com.ahss.dto.response.PaymentRefundDto()), pageable, 1));
    Allure.step(
        "Stub getAllRefunds to return page of payment refunds",
        () -> when(paymentRefundService.getAllRefunds(eq(pageable))).thenReturn(page));

    var result =
        Allure.step(
            "GET /api/v1/payments/refunds",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/refunds").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment refunds retrieved successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/refunds")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get refund by id returns 200 when found")
  @Severity(SeverityLevel.MINOR)
  void get_refund_by_id_returns_200() throws Exception {
    UUID id = UUID.randomUUID();
    PaymentRefundDto dto = Allure.step("Create PaymentRefundDto", PaymentRefundDto::new);
    dto.setId(id);
    Allure.step(
        "Mock paymentRefundService.getRefundById",
        () -> when(paymentRefundService.getRefundById(eq(id))).thenReturn(Optional.of(dto)));

    var result =
        Allure.step(
            "GET /api/v1/payments/refunds/" + id,
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/refunds/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/refunds/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get refund by id returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void get_refund_by_id_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    Allure.step(
        "Mock paymentRefundService.getRefundById",
        () -> when(paymentRefundService.getRefundById(eq(id))).thenReturn(Optional.empty()));

    var result =
        Allure.step(
            "GET /api/v1/payments/refunds/" + id,
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/refunds/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/refunds/" + id)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Refunds by transaction returns 200 with page")
  @Severity(SeverityLevel.MINOR)
  void refunds_by_transaction_returns_200() throws Exception {
    UUID tx = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    org.springframework.data.domain.Page<com.ahss.dto.response.PaymentRefundDto> page =
        new org.springframework.data.domain.PageImpl<>(
            List.of(new com.ahss.dto.response.PaymentRefundDto()), pageable, 1);
    when(paymentRefundService.getRefundsByTransaction(eq(tx), eq(pageable))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/v1/payments/refunds/transaction/" + tx)
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.path", is("/api/v1/payments/refunds/transaction/" + tx)));
  }

  @Test
  @Story("Refunds by status returns 200 with page")
  @Severity(SeverityLevel.MINOR)
  void refunds_by_status_returns_200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentRefundDto> page =
        Allure.step(
            "Create page of payment refunds",
            () ->
                new org.springframework.data.domain.PageImpl<>(
                    List.of(new com.ahss.dto.response.PaymentRefundDto()), pageable, 1));
    Allure.step(
        "Stub getRefundsByStatus to return page of payment refunds",
        () ->
            when(paymentRefundService.getRefundsByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.PENDING), eq(pageable)))
                .thenReturn(page));

    mockMvc
        .perform(
            get("/api/v1/payments/refunds/status/PENDING").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.path", is("/api/v1/payments/refunds/status/PENDING")));
  }

  @Test
  @Story("Cancel refund returns 200 when cancelled")
  @Severity(SeverityLevel.MINOR)
  void cancel_refund_success_returns_200() throws Exception {
    UUID id = UUID.randomUUID();
    var result =
        Allure.step(
            "PATCH /api/v1/payments/refunds/" + id + "/cancel",
            () ->
                mockMvc
                    .perform(patch("/api/v1/payments/refunds/" + id + "/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Payment refund cancelled successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/refunds/" + id + "/cancel")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
    verify(paymentRefundService, times(1)).cancelRefund(eq(id), anyString());
  }

  @Test
  @Story("List audit logs returns 200 with page payload")
  @Severity(SeverityLevel.NORMAL)
  void list_audit_logs_returns_200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentAuditLogDto> page =
        Allure.step(
            "Create page of payment audit logs",
            () ->
                new org.springframework.data.domain.PageImpl<>(
                    List.of(new com.ahss.dto.response.PaymentAuditLogDto()), pageable, 1));
    Allure.step(
        "Stub getAllAuditLogs to return page of payment audit logs",
        () -> when(auditLogService.getAllAuditLogs(eq(pageable))).thenReturn(page));

    var result =
        Allure.step(
            "GET /api/v1/payments/audit-logs",
            () ->
                mockMvc
                    .perform(
                        get("/api/v1/payments/audit-logs").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(
                        jsonPath("$.message", is("Payment audit logs retrieved successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/audit-logs")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get audit log by id returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void get_audit_log_by_id_returns_404() throws Exception {
    UUID id = UUID.randomUUID();
    when(auditLogService.getAuditLogById(eq(id))).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/v1/payments/audit-logs/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", containsString("not found")))
        .andExpect(jsonPath("$.path", is("/api/v1/payments/audit-logs/" + id)));
  }

  @Test
  @Story("Audit logs by request returns 200 with page")
  @Severity(SeverityLevel.MINOR)
  void audit_logs_by_request_returns_200() throws Exception {
    UUID reqId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentAuditLogDto> page =
        Allure.step(
            "Create page of payment audit logs",
            () ->
                new org.springframework.data.domain.PageImpl<>(
                    List.of(new com.ahss.dto.response.PaymentAuditLogDto()), pageable, 1));
    Allure.step(
        "Stub getAuditLogsByPaymentRequest to return page of payment audit logs",
        () ->
            when(auditLogService.getAuditLogsByPaymentRequest(eq(reqId), eq(pageable)))
                .thenReturn(page));

    var result =
        Allure.step(
            "GET /api/v1/payments/audit-logs/request/" + reqId,
            () ->
                mockMvc
                    .perform(
                        get("/api/v1/payments/audit-logs/request/" + reqId)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/payments/audit-logs/request/" + reqId)))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Audit logs search returns 200 with page")
  @Severity(SeverityLevel.MINOR)
  void audit_logs_search_returns_200() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    Page<PaymentAuditLogDto> page =
        Allure.step(
            "Create page of payment audit logs",
            () ->
                new org.springframework.data.domain.PageImpl<>(
                    List.of(new com.ahss.dto.response.PaymentAuditLogDto()), pageable, 1));
    Allure.step(
        "Stub searchAuditLogs to return page of payment audit logs",
        () -> when(auditLogService.searchAuditLogs(eq("pay"), eq(pageable))).thenReturn(page));

    var result =
        Allure.step(
            "GET /api/v1/payments/audit-logs/search",
            () ->
                mockMvc
                    .perform(
                        get("/api/v1/payments/audit-logs/search")
                            .param("searchTerm", "pay")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/audit-logs/search"))));
  }

  @Test
  @Story("Payment request stats returns 200 with summary")
  @Severity(SeverityLevel.MINOR)
  void payment_request_stats_returns_200() throws Exception {
    Allure.step(
        "Stub countByStatus to return 2 pending payment requests",
        () ->
            when(paymentRequestService.countByStatus(eq(PaymentRequestStatus.PENDING)))
                .thenReturn(2L));
    Allure.step(
        "Stub countByStatus to return 3 approved payment requests",
        () ->
            when(paymentRequestService.countByStatus(eq(PaymentRequestStatus.APPROVED)))
                .thenReturn(3L));
    Allure.step(
        "Stub countByStatus to return 1 rejected payment requests",
        () ->
            when(paymentRequestService.countByStatus(eq(PaymentRequestStatus.REJECTED)))
                .thenReturn(1L));
    Allure.step(
        "Stub countByStatus to return 0 cancelled payment requests",
        () ->
            when(paymentRequestService.countByStatus(eq(PaymentRequestStatus.CANCELLED)))
                .thenReturn(0L));

    var result =
        Allure.step(
            "GET /api/v1/payments/stats/requests",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/stats/requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.totalRequests", is(6)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/stats/requests"))));
  }

  @Test
  @Story("Payment request stats returns 500 on error")
  @Severity(SeverityLevel.MINOR)
  void payment_request_stats_returns_500_on_error() throws Exception {
    Allure.step(
        "Stub countByStatus to throw RuntimeException",
        () ->
            when(paymentRequestService.countByStatus(any()))
                .thenThrow(new RuntimeException("boom")));

    var result =
        Allure.step(
            "GET /api/v1/payments/stats/requests",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/stats/requests"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Failed to retrieve")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/stats/requests"))));
  }

  @Test
  @Story("Transaction stats returns 200 with summary")
  @Severity(SeverityLevel.MINOR)
  void transaction_stats_returns_200() throws Exception {
    Allure.step(
        "Stub countByStatus to return 1 pending payment transaction",
        () ->
            when(paymentTransactionService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.PENDING)))
                .thenReturn(1L));
    Allure.step(
        "Stub countByStatus to return 4 successful payment transactions",
        () ->
            when(paymentTransactionService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.SUCCESS)))
                .thenReturn(4L));
    Allure.step(
        "Stub countByStatus to return 2 failed payment transactions",
        () ->
            when(paymentTransactionService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.FAILED)))
                .thenReturn(2L));
    Allure.step(
        "Stub countByStatus to return 1 cancelled payment transaction",
        () ->
            when(paymentTransactionService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.CANCELLED)))
                .thenReturn(1L));

    var result =
        Allure.step(
            "GET /api/v1/payments/stats/transactions",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/stats/transactions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.totalTransactions", is(8)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/stats/transactions"))));
  }

  @Test
  @Story("Refund stats returns 200 with summary")
  @Severity(SeverityLevel.MINOR)
  void refund_stats_returns_200() throws Exception {
    Allure.step(
        "Stub countByStatus to return 0 pending payment refunds",
        () ->
            when(paymentRefundService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.PENDING)))
                .thenReturn(0L));
    Allure.step(
        "Stub countByStatus to return 2 successful payment refunds",
        () ->
            when(paymentRefundService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.SUCCESS)))
                .thenReturn(2L));
    Allure.step(
        "Stub countByStatus to return 1 failed payment refund",
        () ->
            when(paymentRefundService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.FAILED)))
                .thenReturn(1L));
    Allure.step(
        "Stub countByStatus to return 1 cancelled payment refund",
        () ->
            when(paymentRefundService.countByStatus(
                    eq(com.ahss.enums.PaymentTransactionStatus.CANCELLED)))
                .thenReturn(1L));

    var result =
        Allure.step(
            "GET /api/v1/payments/stats/refunds",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/stats/refunds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.totalRefunds", is(4)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/stats/refunds"))));
  }

  @Test
  @Story("Audit log stats returns 200 with breakdown")
  @Severity(SeverityLevel.MINOR)
  void audit_log_stats_returns_200() throws Exception {
    Allure.step(
        "Stub getDistinctActions to return CREATE and UPDATE",
        () -> when(auditLogService.getDistinctActions()).thenReturn(List.of("CREATE", "UPDATE")));
    java.util.Map<String, Long> breakdown = java.util.Map.of("CREATE", 2L, "UPDATE", 3L);
    Allure.step(
        "Stub getActionCountBreakdown to return CREATE: 2, UPDATE: 3",
        () -> when(auditLogService.getActionCountBreakdown()).thenReturn(breakdown));

    var result =
        Allure.step(
            "GET /api/v1/payments/stats/audit-logs",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/stats/audit-logs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.distinctActions", is(2)))
                    .andExpect(jsonPath("$.data.totalAuditLogs", is(5)))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/stats/audit-logs"))));
  }

  @Test
  @Story("Audit log stats returns 500 on service error")
  @Severity(SeverityLevel.MINOR)
  void audit_log_stats_returns_500_on_error() throws Exception {
    Allure.step(
        "Stub getDistinctActions to throw RuntimeException",
        () -> when(auditLogService.getDistinctActions()).thenThrow(new RuntimeException("oops")));

    var result =
        Allure.step(
            "GET /api/v1/payments/stats/audit-logs",
            () ->
                mockMvc
                    .perform(get("/api/v1/payments/stats/audit-logs"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", containsString("Failed to retrieve")))
                    .andExpect(jsonPath("$.path", is("/api/v1/payments/stats/audit-logs"))));
  }
}
