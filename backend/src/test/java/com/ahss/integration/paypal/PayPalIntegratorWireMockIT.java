package com.ahss.integration.paypal;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PayPalIntegratorWireMockIT.ProxyRestTemplateConfig.class)
@TestPropertySource(properties = {
        "paypal.paymentApiUrl=http://example.com/v2/checkout/orders",
        "paypal.refundApiUrl=http://example.com/v2/payments/captures/{capture_id}/refund"
})
@ActiveProfiles("test")
@Epic("Payment Channel Integration")
@Feature("PayPal Integration")
class PayPalIntegratorWireMockIT {

    @Autowired
    private PayPalIntegrator payPalIntegrator;

    @Autowired
    private RestTemplate restTemplate;

    @TestConfiguration
    static class ProxyRestTemplateConfig {
        @Bean
        @Primary
        RestTemplate restTemplate() {
            RestTemplate template = new RestTemplate();
            java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters = new java.util.ArrayList<>();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            org.springframework.http.converter.json.MappingJackson2HttpMessageConverter json = new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter();
            json.setObjectMapper(mapper);
            converters.add(json);
            converters.add(new org.springframework.http.converter.StringHttpMessageConverter());
            converters.add(new org.springframework.http.converter.FormHttpMessageConverter());
            template.setMessageConverters(converters);
            return template;
        }

        @Bean
        PayPalIntegrator payPalIntegrator(
                RestTemplate restTemplate,
                @Value("${paypal.paymentApiUrl:https://api-m.paypal.com/v2/checkout/orders}") String paymentApiUrl,
                @Value("${paypal.refundApiUrl:https://api-m.paypal.com/v2/payments/captures/{capture_id}/refund}") String refundApiUrl) {
            return new PayPalIntegrator(restTemplate, paymentApiUrl, refundApiUrl);
        }
    }

    @Test
    @DisplayName("initiatePayment() returns created response for valid request")
    @Story("Initiates payment for valid request")
    void initiatePayment_returnsCreatedResponse_viaWireMock() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://example.com/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body("{\"id\":\"order_abc\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        PaymentRequestDto request = new PaymentRequestDto();
        request.setId(java.util.UUID.randomUUID());
        request.setAmount(new BigDecimal("99.99"));
        request.setCurrency("USD");

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(java.util.UUID.randomUUID());
        tx.setAmount(new BigDecimal("99.99"));
        tx.setCurrency("USD");

        var resp = payPalIntegrator.initiatePayment(request, tx);

        assertTrue(resp.isSuccess());
        assertEquals("CREATED", resp.getStatus());
        assertEquals("PayPal", resp.getGatewayName());
        assertEquals(tx.getAmount(), resp.getAmount());
        assertEquals(tx.getCurrency(), resp.getCurrency());
        server.verify();
    }

    @Test
    @DisplayName("processRefund() returns refunded response for valid request")
    @Story("Processes refund for valid request")
    void processRefund_returnsRefundedResponse_viaWireMock() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        // Match the expanded path
        server.expect(requestTo("http://example.com/v2/payments/captures/CAPTURE_123/refund"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .body("{\"id\":\"refund_xyz\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(java.util.UUID.randomUUID());
        tx.setAmount(new BigDecimal("10.00"));
        tx.setCurrency("USD");
        tx.setExternalTransactionId("CAPTURE_123");

        var resp = payPalIntegrator.processRefund(tx, new BigDecimal("10.00"));

        assertTrue(resp.isSuccess());
        assertEquals("REFUNDED", resp.getStatus());
        assertEquals("PayPal", resp.getGatewayName());
        assertEquals(tx.getAmount(), resp.getAmount());
        assertEquals(tx.getCurrency(), resp.getCurrency());
        server.verify();
    }
}