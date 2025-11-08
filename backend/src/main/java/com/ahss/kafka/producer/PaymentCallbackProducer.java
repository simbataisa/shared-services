package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentCallbackProducer extends BaseJsonKafkaProducer {

    private final String callbacksTopic;

    public PaymentCallbackProducer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.payment-callbacks:payment-callbacks}") String callbacksTopic
    ) {
        super(kafkaTemplate, objectMapper);
        this.callbacksTopic = callbacksTopic;
    }

    public void send(PaymentCallbackEvent event) {
        String key = event.getCorrelationId() != null ? event.getCorrelationId() : null;
        sendJson(callbacksTopic, key, event);
    }
}