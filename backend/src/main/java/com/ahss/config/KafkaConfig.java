package com.ahss.config;

import com.ahss.tracing.kafka.OtelKafkaProducerInterceptor;
import com.ahss.tracing.kafka.OtelKafkaConsumerInterceptor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaConfig {

    private static final TextMapGetter<Headers> KAFKA_HEADER_GETTER = new TextMapGetter<Headers>() {
        @Override
        public Iterable<String> keys(Headers headers) {
            List<String> keys = new ArrayList<>();
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

    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<Object, Object> producerFactory(KafkaProperties kafkaProperties) {
        // Merge Spring Boot Kafka producer properties, then ensure OTEL interceptor is
        // present
        Map<String, Object> props = new HashMap<>(
                kafkaProperties.buildProducerProperties(new DefaultSslBundleRegistry()));
        String interceptorClass = OtelKafkaProducerInterceptor.class.getName();
        Object existing = props.get(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG);
        switch (existing) {
            case null -> props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, List.of(interceptorClass));
            case List<?> list -> {
                if (!list.contains(interceptorClass)) {
                    List<Object> newList = new ArrayList<>(list);
                    newList.add(interceptorClass);
                    props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, newList);
                }
            }
            case String s -> {
                if (!s.contains(interceptorClass)) {
                    props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, s + "," + interceptorClass);
                }
            }
            default -> {
            }
        }
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(
                kafkaProperties.buildConsumerProperties(new DefaultSslBundleRegistry()));
        // Ensure String deserializers for simple JSON string payloads
        props.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Add OTEL consumer interceptor for distributed tracing
        String interceptorClass = OtelKafkaConsumerInterceptor.class.getName();
        Object existing = props.get(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG);
        switch (existing) {
            case null -> props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, List.of(interceptorClass));
            case List<?> list -> {
                if (!list.contains(interceptorClass)) {
                    List<Object> newList = new ArrayList<>(list);
                    newList.add(interceptorClass);
                    props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, newList);
                }
            }
            case String s -> {
                if (!s.contains(interceptorClass)) {
                    props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, s + "," + interceptorClass);
                }
            }
            default -> {
            }
        }

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setObservationEnabled(true);

        // Add record interceptor to extract and set trace context from Kafka headers
        factory.setRecordInterceptor((record, consumer) -> {
            try {
                // Extract context from Kafka headers
                Context extractedContext = GlobalOpenTelemetry.getPropagators()
                    .getTextMapPropagator()
                    .extract(Context.current(), record.headers(), KAFKA_HEADER_GETTER);

                if (extractedContext != null && extractedContext != Context.current()) {
                    // Make the context current for the processing of this record
                    // The scope will be closed automatically by Spring Kafka's observation mechanism
                    extractedContext.makeCurrent();
                }
            } catch (Exception e) {
                // Continue without trace context if extraction fails
            }
            return record;
        });

        return factory;
    }

    @Bean
    public NewTopic paymentCallbacksTopic(
            @Value("${app.kafka.topics.payment-callbacks:payment-callbacks}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentEventsTopic(
            @Value("${app.kafka.topics.payment-events:payment-events}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}