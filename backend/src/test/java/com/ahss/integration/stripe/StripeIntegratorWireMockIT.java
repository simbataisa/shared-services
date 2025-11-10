package com.ahss.integration.stripe;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.integration.stripe.StripeIntegrator;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

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
import com.ahss.SharedServicesApplication;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(classes = StripeIntegratorWireMockIT.ProxyRestTemplateConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "stripe.paymentApiUrl=http://localhost:${wiremock.server.port}/v1/charges",
        "stripe.tokenizationApiUrl=http://localhost:${wiremock.server.port}/v1/tokens"
})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Import({ StripeIntegratorWireMockIT.ProxyRestTemplateConfig.class, StripeIntegrator.class })
@Epic("Payment Channel Integration")
@Feature("Stripe Integration")
class StripeIntegratorWireMockIT {

    @Autowired
    private StripeIntegrator stripeIntegrator;

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
    @DisplayName("initiatePayment_returnsAuthorizedResponse_viaWireMock")
    @Story("Initiate Payment")
    void initiatePayment_returnsAuthorizedResponse_viaWireMock() {
        stubFor(post(urlPathEqualTo("/v1/charges"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"ch_123\",\"status\":\"AUTHORIZED\",\"amount\":42.00,\"currency\":\"USD\",\"success\":true}")));

        PaymentRequestDto request = new PaymentRequestDto();
        request.setId(java.util.UUID.randomUUID());
        request.setAmount(new BigDecimal("42.00"));
        request.setCurrency("USD");

        PaymentTransactionDto tx = new PaymentTransactionDto();
        tx.setId(java.util.UUID.randomUUID());
        tx.setAmount(new BigDecimal("42.00"));
        tx.setCurrency("USD");

        var resp = stripeIntegrator.initiatePayment(request, tx);

        assertTrue(resp.isSuccess());
        assertEquals("AUTHORIZED", resp.getStatus());
        assertEquals("Stripe", resp.getGatewayName());
        assertEquals(tx.getAmount(), resp.getAmount());
        assertEquals(tx.getCurrency(), resp.getCurrency());

        verify(postRequestedFor(urlPathEqualTo("/v1/charges")));
    }

    @Test
    @DisplayName("tokenizeCard_returnsTokenizedResponse_viaWireMock")
    @Story("Tokenize Card")
    void tokenizeCard_returnsTokenizedResponse_viaWireMock() {
        stubFor(post(urlPathEqualTo("/v1/tokens"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\":\"tok_456\",\"tokenType\":\"card\",\"success\":true}")));

        var resp = stripeIntegrator.tokenizeCard(new Object());

        assertTrue(resp.isSuccess());
        assertEquals("TOKENIZED", resp.getStatus());
        assertEquals("Stripe", resp.getGatewayName());

        verify(postRequestedFor(urlPathEqualTo("/v1/tokens")));
    }
}