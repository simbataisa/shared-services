package com.ahss.tracing.kafka;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Kafka Consumer interceptor that extracts W3C trace context (`traceparent`, `tracestate`) from Kafka headers
 * and stores it in a ThreadLocal for the consumer to use.
 *
 * This enables distributed tracing across Kafka message boundaries, allowing consumers to continue
 * the trace started by producers.
 *
 * Note: This interceptor sets up the trace context which is then accessed by the actual @KafkaListener
 * through the Spring Kafka observation mechanism.
 */
public class OtelKafkaConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

    private static final Logger log = LoggerFactory.getLogger(OtelKafkaConsumerInterceptor.class);

    // ThreadLocal to store extracted context for each thread processing messages
    private static final ThreadLocal<Context> EXTRACTED_CONTEXT = new ThreadLocal<>();

    private static final TextMapGetter<Headers> KAFKA_HEADER_GETTER = new TextMapGetter<Headers>() {
        @Override
        public Iterable<String> keys(Headers headers) {
            java.util.List<String> keys = new java.util.ArrayList<>();
            if (headers != null) {
                headers.forEach(header -> keys.add(header.key()));
            }
            return keys;
        }

        @Override
        public String get(Headers headers, String key) {
            if (headers == null || key == null) {
                return null;
            }
            Header header = headers.lastHeader(key);
            if (header == null || header.value() == null) {
                return null;
            }
            return new String(header.value(), StandardCharsets.UTF_8);
        }
    };

    /**
     * Get the extracted context for the current thread.
     * This is used by the KafkaListenerAspect to set up the trace context.
     */
    public static Context getExtractedContext() {
        return EXTRACTED_CONTEXT.get();
    }

    /**
     * Clear the extracted context for the current thread.
     */
    public static void clearExtractedContext() {
        EXTRACTED_CONTEXT.remove();
    }

    @Override
    public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
        // For batch consumption, we extract context from the first record
        // In a real scenario, you might want to handle each record individually
        if (!records.isEmpty()) {
            ConsumerRecord<K, V> firstRecord = records.iterator().next();
            try {
                // Extract context from Kafka headers
                Context extractedContext = GlobalOpenTelemetry.getPropagators()
                    .getTextMapPropagator()
                    .extract(Context.current(), firstRecord.headers(), KAFKA_HEADER_GETTER);

                // Store in ThreadLocal for the listener to use
                EXTRACTED_CONTEXT.set(extractedContext);

                log.debug("Extracted trace context from Kafka headers for topic: {}", firstRecord.topic());
            } catch (Exception e) {
                log.warn("Failed to extract trace context from Kafka headers", e);
            }
        }
        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        // Clean up ThreadLocal after commit
        EXTRACTED_CONTEXT.remove();
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

