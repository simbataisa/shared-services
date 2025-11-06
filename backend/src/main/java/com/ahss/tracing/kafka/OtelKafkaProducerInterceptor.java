package com.ahss.tracing.kafka;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Kafka Producer interceptor that injects W3C trace context (`traceparent`, `tracestate`) into Kafka headers.
 * This enables downstream consumers (and CloudEvents) to propagate distributed tracing.
 */
public class OtelKafkaProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

    private static final TextMapSetter<Headers> KAFKA_HEADER_SETTER = (headers, key, value) -> {
        if (headers != null && key != null && value != null) {
            headers.add(key, value.getBytes(StandardCharsets.UTF_8));
        }
    };

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        try {
            // Inject current context into Kafka headers
            GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), record.headers(), KAFKA_HEADER_SETTER);
        } catch (Exception ignored) {
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // no-op
    }
}