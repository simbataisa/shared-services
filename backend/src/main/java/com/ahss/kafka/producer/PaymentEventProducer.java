package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentDomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer extends BaseJsonKafkaProducer {

    private final String eventsTopic;

    public PaymentEventProducer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.payment-events:payment-events}") String eventsTopic
    ) {
        super(kafkaTemplate, objectMapper);
        this.eventsTopic = eventsTopic;
    }

    public void send(PaymentDomainEvent event) {
        sendJson(eventsTopic, event.getCorrelationId(), event);
    }
}