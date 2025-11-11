package com.ahss.kafka.producer;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
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
        log.info("Payment callback received: {}", event);
        String key = event.getCorrelationId() != null ? event.getCorrelationId() : null;
        sendJson(callbacksTopic, key, event);
    }
}