package com.ahss.config;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Security")
@Feature("OpenAPI")
@Owner("backend")
class OpenApiConfigTest {

    @Test
    @Story("Global OpenAPI customizer adds bearer auth")
    @Severity(SeverityLevel.NORMAL)
    void globalSecurityCustomizer_adds_bearer_auth_to_all_operations() {
        OpenApiConfig config = new OpenApiConfig();
        OpenApiCustomizer customizer = config.globalSecurityCustomizer();

        OpenAPI openApi = new OpenAPI();
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        pathItem.setGet(operation);
        paths.addPathItem("/test", pathItem);
        openApi.setPaths(paths);

        Allure.step("Apply global security customizer", () -> customizer.customise(openApi));

        SecurityRequirement expected = new SecurityRequirement().addList("bearerAuth");
        Allure.addAttachment("Operation Security", MediaType.TEXT_PLAIN_VALUE,
                String.valueOf(operation.getSecurity()));
        assertThat(operation.getSecurity()).containsExactly(expected);
    }

    @Test
    @Story("Global customizer avoids duplicate security entries")
    @Severity(SeverityLevel.MINOR)
    void globalSecurityCustomizer_does_not_add_duplicate_security_requirement() {
        OpenApiConfig config = new OpenApiConfig();
        OpenApiCustomizer customizer = config.globalSecurityCustomizer();

        OpenAPI openApi = new OpenAPI();
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        SecurityRequirement existing = new SecurityRequirement().addList("bearerAuth");
        operation.addSecurityItem(existing);
        pathItem.setGet(operation);
        paths.addPathItem("/test", pathItem);
        openApi.setPaths(paths);

        Allure.step("Apply global security customizer", () -> customizer.customise(openApi));

        assertThat(operation.getSecurity()).hasSize(1);
    }
}