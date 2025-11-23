package com.ahss.integration.stripe;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(classes = StripeIntegratorWireMockIT.ProxyRestTemplateConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Import({ StripeIntegratorWireMockIT.ProxyRestTemplateConfig.class })
@Epic("Payment Channel Integration")
@Feature("Stripe Integration")
class StripeIntegratorWireMockIT {

    @Autowired
    private StripeIntegrator stripeIntegrator;

    @Autowired
    private WireMockServer wireMockServer;

    private static final String STUBS_OUTPUT_DIR = "target/stubs/stripe";

    // TestRestTemplate not required when webEnvironment is NONE

    @AfterEach
    void saveContractStubs() throws IOException {
        Path stubsPath = Paths.get(STUBS_OUTPUT_DIR);
        Files.createDirectories(stubsPath);

        // Get all stub mappings from WireMock
        var stubMappings = wireMockServer.getStubMappings();

        int stubIndex = 0;
        for (StubMapping stub : stubMappings) {
            // Generate unique filename for each stub
            String filename = String.format("stub_%d_%s.json",
                stubIndex++,
                System.currentTimeMillis());

            Path stubFile = stubsPath.resolve(filename);

            // Write stub mapping as JSON
            String stubJson = com.github.tomakehurst.wiremock.stubbing.StubMapping.buildJsonStringFor(stub);
            Files.writeString(stubFile, stubJson);
        }

        System.out.println("✓ Generated " + stubMappings.size() + " contract stub(s) in: " + stubsPath.toAbsolutePath());

        // Reset WireMock after saving stubs for next test
        wireMockServer.resetAll();
    }

    @Configuration
    static class ProxyRestTemplateConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
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

        @Bean
        StripeIntegrator stripeIntegrator(
                RestTemplate restTemplate,
                @org.springframework.beans.factory.annotation.Value("${wiremock.server.port}") int wiremockPort,
                ObjectMapper objectMapper) {
            String baseUrl = "http://localhost:" + wiremockPort;
            return new StripeIntegrator(
                    restTemplate,
                    baseUrl + "/v1/tokens",
                    baseUrl + "/v1/charges",
                    baseUrl + "/v1/refunds",
                    "mock_stripe_key",
                    objectMapper);
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