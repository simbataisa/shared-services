package com.ahss.kafka.consumer;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.saga.PaymentSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCallbackConsumer {

    private final PaymentSagaOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    public PaymentCallbackConsumer(PaymentSagaOrchestrator orchestrator, ObjectMapper objectMapper) {
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-callbacks}", groupId = "${app.kafka.consumer.group}")
    public void onMessage(String message) {
        try {
            PaymentCallbackEvent event = objectMapper.readValue(message, PaymentCallbackEvent.class);
            orchestrator.handle(event);
        } catch (Exception e) {
            // In a real system, route to DLT
            e.printStackTrace();
        }
    }
}