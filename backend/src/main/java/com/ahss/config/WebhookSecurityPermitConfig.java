package com.ahss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class WebhookSecurityPermitConfig {

    @Bean
    public WebSecurityCustomizer webhookSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/api/integrations/webhooks/stripe",
                        "/api/integrations/webhooks/paypal",
                        "/api/integrations/webhooks/bank-transfer");
    }
}