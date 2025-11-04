package com.ahss.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
    // OpenAPI security scheme configuration for JWT Bearer authorization

    // Apply bearerAuth globally so operations show security requirements
    @Bean
    public OpenApiCustomizer globalSecurityCustomizer() {
        return openApi -> {
            SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        boolean alreadyPresent = operation.getSecurity() != null &&
                            operation.getSecurity().stream().anyMatch(req -> req.containsKey("bearerAuth"));
                        if (!alreadyPresent) {
                            operation.addSecurityItem(securityRequirement);
                        }
                    })
                );
            }
        };
    }
}