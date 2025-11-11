package com.ahss.integration.bank;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.ahss.integration.bank.BankTransferIntegrator;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SpringBootTest(classes = BankTransferIntegratorWireMockIT.ProxyRestTemplateConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "bankTransfer.transferApiUrl=http://localhost:${wiremock.server.port}/v1/transfers",
        "bankTransfer.verifyApiUrl=http://localhost:${wiremock.server.port}/v1/accounts/verify"
})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Import({ BankTransferIntegratorWireMockIT.ProxyRestTemplateConfig.class, BankTransferIntegrator.class })
@Epic("Payment Channel Integration")
@Feature("Bank Transfer Integration")
class BankTransferIntegratorWireMockIT {

    @Autowired
    private BankTransferIntegrator bankTransferIntegrator;

    // TestRestTemplate not required when webEnvironment is NONE

    @Configuration
    static class ProxyRestTemplateConfig {
        @Bean
        @Primary
        RestTemplate restTemplate() {
            RestTemplate template = new RestTemplate();
            // Ensure JSON serialization support in minimal context
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
    }

    @Test
    @DisplayName("initiatePayment() returns initiated response for valid request via WireMock")
    @Story("Initiate Payment for BANK_TRANSFER")
    void initiatePayment_returnsInitiatedResponse_viaWireMock() {
        stubFor(post(urlPathEqualTo("/v1/transfers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"bt_tx_789\",\"status\":\"TRANSFER.INITIATED\",\"success\":true,\"externalTransactionId\":\"bt_tx_789\"}")));

        PaymentRequestDto request = new PaymentRequestDto();
        request.setId(java.util.UUID.randomUUID());
        request.setAmount(new BigDecimal("12.34"));
        request.setCurrency("USD");

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(java.util.UUID.randomUUID());
        tx.setAmount(new BigDecimal("12.34"));
        tx.setCurrency("USD");

        var resp = bankTransferIntegrator.initiatePayment(request, tx);

        assertTrue(resp.isSuccess());
        assertEquals("TRANSFER.INITIATED", resp.getStatus());
        assertEquals("BankTransfer", resp.getGatewayName());
        assertEquals(tx.getAmount(), resp.getAmount());
        assertEquals(tx.getCurrency(), resp.getCurrency());

        verify(postRequestedFor(urlPathEqualTo("/v1/transfers")));
    }
}