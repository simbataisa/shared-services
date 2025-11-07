package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentDomainEvent;
import com.ahss.kafka.producer.PaymentEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Epic("Saga")
@Feature("Payment Event Producer")
class PaymentEventProducerTest {

    @Test
    @DisplayName("send() serializes event and publishes to configured topic with key")
    @Story("send() serializes and publishes event to configured topic with correlation id as key")
    void send_serializes_and_publishes() throws Exception {
        KafkaTemplate<Object, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        String topic = "unit-payment-events";
        PaymentEventProducer producer = new PaymentEventProducer(kafkaTemplate, objectMapper, topic);

        String correlationId = "unit-corr-1";
        PaymentDomainEvent evt = new PaymentDomainEvent("unit.type", correlationId, Map.of("a", 1));
        String payload = "{\"type\":\"unit.type\",\"correlationId\":\"" + correlationId + "\"}";

        Allure.addAttachment("DomainEvent (unit)", "application/json", payload);
        when(objectMapper.writeValueAsString(eq(evt))).thenReturn(payload);

        producer.send(evt);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate, times(1)).send(eq(topic), eq(correlationId), payloadCaptor.capture());
        assertEquals(payload, payloadCaptor.getValue());
    }

    @Test
    @DisplayName("send() throws RuntimeException when serialization fails")
    @Story("send() throws RuntimeException when serialization fails")
    void send_throws_when_serialization_fails() throws Exception {
        KafkaTemplate<Object, Object> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        PaymentEventProducer producer = new PaymentEventProducer(kafkaTemplate, objectMapper, "unit-payment-events");

        PaymentDomainEvent evt = new PaymentDomainEvent("bad.type", "cid-err", Map.of());
        JsonProcessingException jpe = new JsonProcessingException("boom") {
        };
        when(objectMapper.writeValueAsString(eq(evt))).thenThrow(jpe);

        Allure.step("Expect RuntimeException on serialization failure", () -> {
            assertThrows(RuntimeException.class, () -> producer.send(evt));
        });
        verify(kafkaTemplate, times(0)).send(any(), any(), any());
    }
}