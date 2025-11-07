package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Epic("Saga")
@Feature("Payment Callback Producer")
class PaymentCallbackProducerTest {

    @Test
    @DisplayName("send() uses correlation id as key and serializes payload")
    @Story("send() uses correlation id as key and serializes payload")
    void send_uses_correlation_id_as_key_and_serializes_payload() {
        KafkaTemplate<Object, Object> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentCallbackProducer producer = new PaymentCallbackProducer(kafkaTemplate, objectMapper, "test-topic");

        PaymentCallbackEvent event = new PaymentCallbackEvent();
        event.setCorrelationId("corr-123");
        event.setGatewayName("Stripe");

        producer.send(event);

        ArgumentCaptor<Object> keyCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate, times(1)).send(eq("test-topic"), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals("corr-123", keyCaptor.getValue());
        String json = (String) payloadCaptor.getValue();
        assertTrue(json.contains("\"correlationId\":\"corr-123\""));
        assertTrue(json.contains("\"gatewayName\":\"Stripe\""));
    }

    @Test
    @DisplayName("send() allows null key when correlation id is missing")
    @Story("send() allows null key when correlation id is missing")
    void send_allows_null_key_when_correlation_missing() {
        KafkaTemplate<Object, Object> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentCallbackProducer producer = new PaymentCallbackProducer(kafkaTemplate, objectMapper, "test-topic");

        PaymentCallbackEvent event = new PaymentCallbackEvent();
        event.setGatewayName("PayPal");

        producer.send(event);

        ArgumentCaptor<Object> keyCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate, times(1)).send(eq("test-topic"), keyCaptor.capture(), payloadCaptor.capture());

        assertNull(keyCaptor.getValue());
        String json = (String) payloadCaptor.getValue();
        assertTrue(json.contains("\"gatewayName\":\"PayPal\""));
    }
}