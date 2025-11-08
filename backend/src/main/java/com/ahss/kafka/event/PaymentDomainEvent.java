package com.ahss.kafka.event;

import java.time.LocalDateTime;
import java.util.Map;

public class PaymentDomainEvent {
    private String type;
    private String correlationId;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;

    public PaymentDomainEvent() {}

    public PaymentDomainEvent(String type, String correlationId, Map<String, Object> payload) {
        this.type = type;
        this.correlationId = correlationId;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}