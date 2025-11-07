package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentDomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsTopic;

    public PaymentEventProducer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.payment-events:payment-events}") String eventsTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.eventsTopic = eventsTopic;
    }

    public void send(PaymentDomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(eventsTopic, event.getCorrelationId(), payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize PaymentDomainEvent", e);
        }
    }
}