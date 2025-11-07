package com.ahss.config;

import com.ahss.tracing.kafka.OtelKafkaProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaConfig {

    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<Object, Object> producerFactory(KafkaProperties kafkaProperties) {
        // Merge Spring Boot Kafka producer properties, then ensure OTEL interceptor is present
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        String interceptorClass = OtelKafkaProducerInterceptor.class.getName();
        Object existing = props.get(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG);
        if (existing == null) {
            props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, List.of(interceptorClass));
        } else if (existing instanceof List) {
            List<?> list = (List<?>) existing;
            if (!list.contains(interceptorClass)) {
                List<Object> newList = new ArrayList<>(list);
                newList.add(interceptorClass);
                props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, newList);
            }
        } else if (existing instanceof String) {
            String s = (String) existing;
            if (!s.contains(interceptorClass)) {
                props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, s + "," + interceptorClass);
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
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        // Ensure String deserializers for simple JSON string payloads
        props.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public NewTopic paymentCallbacksTopic(
            @Value("${app.kafka.topics.payment-callbacks:payment-callbacks}") String name
    ) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentEventsTopic(
            @Value("${app.kafka.topics.payment-events:payment-events}") String name
    ) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}