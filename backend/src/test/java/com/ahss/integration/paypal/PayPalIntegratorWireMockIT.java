package com.ahss.integration.paypal;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentTransactionDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PayPalIntegratorWireMockIT.ProxyRestTemplateConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Import({ PayPalIntegratorWireMockIT.ProxyRestTemplateConfig.class })
@Epic("Payment Channel Integration")
@Feature("PayPal Integration")
class PayPalIntegratorWireMockIT {

  @Autowired private PayPalIntegrator payPalIntegrator;

  @Autowired private RestTemplate restTemplate;

  @org.springframework.context.annotation.Configuration
  static class ProxyRestTemplateConfig {

    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper();
    }

    @Bean
    RestTemplate restTemplate() {
      RestTemplate template = new RestTemplate();
      java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters =
          new java.util.ArrayList<>();
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper()
              .configure(
                  com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
              .configure(
                  com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                  false);
      org.springframework.http.converter.json.MappingJackson2HttpMessageConverter json =
          new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter();
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
        @Value("${wiremock.server.port}") int wiremockPort,
        ObjectMapper objectMapper) {
      String baseUrl = "http://localhost:" + wiremockPort;
      return new PayPalIntegrator(
          restTemplate,
          baseUrl + "/paypal/v2/checkout/orders",
          baseUrl + "/paypal/v2/checkout/refunds",
          baseUrl + "/paypal/v1/oauth2/token",
          "mock_client_id",
          "mock_client_secret",
          objectMapper);
    }
  }

  @Test
  @DisplayName("initiatePayment() returns created response for valid request")
  @Story("Initiates payment for valid request")
  void initiatePayment_returnsCreatedResponse_viaWireMock() {
    stubFor(post(urlPathEqualTo("/paypal/v2/checkout/orders"))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"id\":\"order_abc\"}")));

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

    verify(postRequestedFor(urlPathEqualTo("/paypal/v2/checkout/orders")));
  }

  @Test
  @DisplayName("processRefund() returns refunded response for valid request")
  @Story("Processes refund for valid request")
  void processRefund_returnsRefundedResponse_viaWireMock() {
    // WireMock stub - note the URL template variable gets expanded by RestTemplate
    stubFor(post(urlPathMatching("/paypal/v2/checkout/refunds"))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"id\":\"refund_xyz\"}")));

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

    verify(postRequestedFor(urlPathMatching("/paypal/v2/checkout/refunds")));
  }
}
