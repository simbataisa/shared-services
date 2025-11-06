package com.ahss.config;

import com.ahss.tracing.kafka.OtelKafkaProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

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
}