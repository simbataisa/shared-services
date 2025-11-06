package com.ahss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Configuration
public class ObservabilityConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Spring Boot auto-customizes RestTemplate for observation when Micrometer Tracing is present
        return builder.build();
    }
}