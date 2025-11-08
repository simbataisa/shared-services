package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentDomainEvent;
import io.qameta.allure.Allure;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
    "app.kafka.topics.payment-events=payment-events"
})
@org.springframework.test.context.ActiveProfiles("test")
@Epic("Saga")
@Feature("Payment Event Producer")
class PaymentEventProducerIntegrationTest {

  @Autowired private PaymentEventProducer eventProducer;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private KafkaTemplate<Object, Object> kafkaTemplate;

  @Test
  @DisplayName("Sends domain event to payment-events topic")
  @Story("sends domain event to payment-events topic")
  void sends_domain_event_to_payment_events_topic() {
    // Build a domain event
    String correlationId = "corr-123";
    PaymentDomainEvent evt =
        Allure.step(
            "Create domain event",
            () -> new PaymentDomainEvent("test.type", correlationId, Map.of("foo", "bar")));
    // Attach the event JSON being sent
    Allure.step(
        "Attach sent PaymentDomainEvent JSON",
        () -> Allure.addAttachment("Sent PaymentDomainEvent", "application/json", objectMapper.writeValueAsString(evt)));
    Allure.step(
        "Send domain event",
        () -> eventProducer.send(evt));
    // Verify KafkaTemplate.send was invoked with expected topic, key, and payload
    ArgumentCaptor<Object> payloadCaptor =
        Allure.step("Create payload captor", () -> ArgumentCaptor.forClass(Object.class));
    Allure.step(
        "Verify send called",
        () -> verify(kafkaTemplate, times(1)).send(eq("payment-events"), eq(correlationId), payloadCaptor.capture()));
    String payload = String.valueOf(payloadCaptor.getValue());
    Allure.addAttachment("Produced Kafka Payload (mocked)", "application/json", payload);
    assertTrue(payload.contains("\"type\":\"test.type\""));
    assertTrue(payload.contains("\"correlationId\":\"" + correlationId + "\""));
  }
}
