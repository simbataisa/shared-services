package com.ahss.kafka.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Minimal base consumer providing JSON utilities for Kafka message handling.
 * Subclasses should implement their own @KafkaListener methods and business logic.
 */
abstract class BaseJsonKafkaConsumer {

    protected final ObjectMapper objectMapper;

    protected BaseJsonKafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected JsonNode readTree(String message) throws Exception {
        return objectMapper.readTree(message);
    }

    protected <T> T readValue(String message, Class<T> type) throws Exception {
        return objectMapper.readValue(message, type);
    }

    protected Map<String, Object> toMap(JsonNode node) {
        return objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
    }

    protected String text(JsonNode node, String field) {
        return text(node, field, "");
    }

    protected String text(JsonNode node, String field, String defaultVal) {
        if (node == null) return defaultVal;
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : defaultVal;
    }

    protected Long longVal(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        return v != null && v.isNumber() ? v.asLong() : null;
    }
}