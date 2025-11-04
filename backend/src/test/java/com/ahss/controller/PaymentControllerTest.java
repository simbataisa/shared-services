package com.ahss.controller;

import com.ahss.dto.request.CreatePaymentRequestDto;
import com.ahss.dto.request.ProcessPaymentDto;
import com.ahss.dto.request.UpdatePaymentRequestDto;
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

        @Autowired
        private MockMvc mockMvc;

    @MockBean
    private PaymentRequestService paymentRequestService;
    @MockBean
    private PaymentTransactionService paymentTransactionService;
    @MockBean
    private PaymentRefundService paymentRefundService;
        @MockBean
        private PaymentAuditLogService auditLogService;

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
                when(paymentRequestService
                                .createPaymentRequest(org.mockito.ArgumentMatchers.any(CreatePaymentRequestDto.class)))
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
        var result = Allure.step("POST /api/v1/payments/requests", () -> mockMvc
                .perform(post("/api/v1/payments/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Payment request created successfully")))
                .andExpect(jsonPath("$.data.title", is("Test Payment")))
                .andExpect(jsonPath("$.data.amount", is(123.45)))
                .andExpect(jsonPath("$.path", is("/api/v1/payments/requests")))
                .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create payment request returns 400 for invalid tenant")
    @Severity(SeverityLevel.MINOR)
        void create_payment_request_bad_request_returns_400() throws Exception {
                Allure.step("Stub service to throw 'Invalid tenant'", () ->
                                when(paymentRequestService
                                                .createPaymentRequest(org.mockito.ArgumentMatchers
                                                                .any(CreatePaymentRequestDto.class)))
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
        var result = Allure.step("POST /api/v1/payments/requests", () ->
                mockMvc.perform(post("/api/v1/payments/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Invalid tenant")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/requests")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get payment request by ID returns 200 when found")
    @Severity(SeverityLevel.NORMAL)
    void get_payment_request_by_id_found_returns_200() throws Exception {
        UUID id = UUID.randomUUID();
        Allure.step("Stub service to return payment request for id=" + id, () ->
                when(paymentRequestService.getPaymentRequestById(eq(id)))
                        .thenReturn(Optional.of(requestDto(id))));

        var result = Allure.step("GET /api/v1/payments/requests/" + id, () ->
                mockMvc.perform(get("/api/v1/payments/requests/" + id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.data.id", is(id.toString())))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get payment request by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_payment_request_by_id_not_found_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        Allure.step("Stub service to return empty for id=" + id, () ->
                when(paymentRequestService.getPaymentRequestById(eq(id)))
                        .thenReturn(Optional.empty()));

        var result = Allure.step("GET /api/v1/payments/requests/" + id, () ->
                mockMvc.perform(get("/api/v1/payments/requests/" + id))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Payment request not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update payment request returns 200 and payload")
    @Severity(SeverityLevel.NORMAL)
        void update_payment_request_success_returns_200() throws Exception {
                UUID id = UUID.randomUUID();
                PaymentRequestDto updated = requestDto(id);
                updated.setTitle("Updated Title");
                Allure.step("Stub updatePaymentRequest to return updated DTO", () ->
                                when(paymentRequestService.updatePaymentRequest(eq(id),
                                                org.mockito.ArgumentMatchers.any(UpdatePaymentRequestDto.class)))
                                                .thenReturn(updated));
                UpdatePaymentRequestDto req = new UpdatePaymentRequestDto();
                req.setTitle("Updated Title");
                req.setAmount(new BigDecimal("200.00"));
                String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("PUT /api/v1/payments/requests/" + id, () ->
                mockMvc.perform(put("/api/v1/payments/requests/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Payment request updated successfully")))
                        .andExpect(jsonPath("$.data.title", is("Updated Title")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update payment request returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
        void update_payment_request_not_found_returns_404() throws Exception {
                UUID id = UUID.randomUUID();
                Allure.step("Stub updatePaymentRequest to throw not found", () ->
                                when(paymentRequestService.updatePaymentRequest(eq(id),
                                                org.mockito.ArgumentMatchers.any(UpdatePaymentRequestDto.class)))
                                                .thenThrow(new RuntimeException("Payment request not found")));
                UpdatePaymentRequestDto req = new UpdatePaymentRequestDto();
                req.setTitle("Nope");
                String body = objectMapper.writeValueAsString(req);
                Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
                var result = Allure.step("PUT /api/v1/payments/requests/" + id, () ->
                                mockMvc.perform(put("/api/v1/payments/requests/" + id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(body))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update payment request returns 400 for invalid update")
    @Severity(SeverityLevel.MINOR)
        void update_payment_request_bad_request_returns_400() throws Exception {
                UUID id = UUID.randomUUID();
                Allure.step("Stub updatePaymentRequest to throw 'Invalid update'", () ->
                                when(paymentRequestService.updatePaymentRequest(eq(id),
                                                org.mockito.ArgumentMatchers.any(UpdatePaymentRequestDto.class)))
                                                .thenThrow(new RuntimeException("Invalid update")));
                UpdatePaymentRequestDto req = new UpdatePaymentRequestDto();
                req.setTitle("Bad");
                String body = objectMapper.writeValueAsString(req);
                Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
                var result = Allure.step("PUT /api/v1/payments/requests/" + id, () ->
                                mockMvc.perform(put("/api/v1/payments/requests/" + id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Invalid update")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/requests/" + id)))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Process payment returns 201 with transaction payload")
    @Severity(SeverityLevel.CRITICAL)
        void process_payment_success_returns_201() throws Exception {
                UUID txId = UUID.randomUUID();
                Allure.step("Stub processPayment to return transaction DTO", () ->
                                when(paymentTransactionService
                                                .processPayment(org.mockito.ArgumentMatchers
                                                                .any(ProcessPaymentDto.class)))
                                                .thenReturn(transactionDto(txId)));
                ProcessPaymentDto req = new ProcessPaymentDto();
                req.setPaymentToken("tok_abc");
                req.setPaymentMethod(PaymentMethodType.CREDIT_CARD);
                req.setPaymentMethodDetails(Map.of("card", "4242"));
                String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("POST /api/v1/payments/transactions/process", () ->
                mockMvc.perform(post("/api/v1/payments/transactions/process")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Payment processed successfully")))
                        .andExpect(jsonPath("$.data.id", is(txId.toString())))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions/process")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Process payment returns 400 for gateway error")
    @Severity(SeverityLevel.MINOR)
        void process_payment_bad_request_returns_400() throws Exception {
                Allure.step("Stub processPayment to throw 'Gateway error'", () ->
                                when(paymentTransactionService
                                                .processPayment(org.mockito.ArgumentMatchers
                                                                .any(ProcessPaymentDto.class)))
                                                .thenThrow(new RuntimeException("Gateway error")));
                ProcessPaymentDto req = new ProcessPaymentDto();
                req.setPaymentToken("tok_bad");
                req.setPaymentMethod(PaymentMethodType.PAYPAL);
                req.setPaymentMethodDetails(Map.of("foo", "bar"));
                String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("POST /api/v1/payments/transactions/process", () ->
                mockMvc.perform(post("/api/v1/payments/transactions/process")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Gateway error")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions/process")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Retry transaction returns 404 when transaction missing")
    @Severity(SeverityLevel.MINOR)
    void retry_transaction_not_found_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        Allure.step("Stub retryTransaction to throw not found", () ->
                doThrow(new RuntimeException("Transaction not found"))
                        .when(paymentTransactionService).retryTransaction(eq(id)));

        var result = Allure.step("PATCH /api/v1/payments/transactions/" + id + "/retry", () ->
                mockMvc.perform(patch("/api/v1/payments/transactions/" + id + "/retry"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions/" + id + "/retry")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Cancel transaction returns 404 when transaction missing")
    @Severity(SeverityLevel.MINOR)
    void cancel_transaction_not_found_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        Allure.step("Stub cancelTransaction to throw not found", () ->
                doThrow(new RuntimeException("Transaction not found")).when(paymentTransactionService)
                        .cancelTransaction(eq(id), anyString()));

        var result = Allure.step("PATCH /api/v1/payments/transactions/" + id + "/cancel", () ->
                mockMvc.perform(patch("/api/v1/payments/transactions/" + id + "/cancel"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/payments/transactions/" + id + "/cancel")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }
}