package com.ahss.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Minimal base producer that serializes an event to JSON and sends to Kafka.
 * Concrete producers should delegate to {@link #sendJson(String, String, Object)}.
 */
abstract class BaseJsonKafkaProducer {

    protected final KafkaTemplate<Object, Object> kafkaTemplate;
    protected final ObjectMapper objectMapper;

    protected BaseJsonKafkaProducer(KafkaTemplate<Object, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    protected void sendJson(String topic, String key, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event to JSON", e);
        }
    }
}