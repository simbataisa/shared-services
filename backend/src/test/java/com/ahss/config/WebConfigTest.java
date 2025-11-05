package com.ahss.config;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Security")
@Feature("CORS")
@Owner("backend")
class WebConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(WebConfig.class);

    @Test
    @Story("CORS config resolves allowed origins from properties")
    @Severity(SeverityLevel.NORMAL)
    void corsConfigurationSource_bean_is_present_and_configured_from_properties() {
        contextRunner
                .withPropertyValues("cors.allowed-origins=http://localhost:3000,http://localhost:4000")
                .run(context -> {
                    assertThat(context).hasSingleBean(CorsConfigurationSource.class);
                    CorsConfigurationSource source = context.getBean(CorsConfigurationSource.class);
                    assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
                    UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
                    var request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/any");
                    CorsConfiguration config = Allure.step("Resolve CorsConfiguration for request",
                            () -> urlSource.getCorsConfiguration(request));
                    assertThat(config).isNotNull();
                    List<String> origins = config.getAllowedOriginPatterns();
                    Allure.addAttachment("Allowed Origins", MediaType.TEXT_PLAIN_VALUE,
                            origins == null ? "null" : String.join(",", origins));
                    assertThat(origins).containsExactly("http://localhost:3000", "http://localhost:4000");
                });
    }

    @Test
    @Story("CORS config handles empty origins gracefully")
    @Severity(SeverityLevel.MINOR)
    void corsConfigurationSource_handles_empty_origins_gracefully() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(CorsConfigurationSource.class);
                    CorsConfigurationSource source = context.getBean(CorsConfigurationSource.class);
                    UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
                    var request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/any");
                    CorsConfiguration config = Allure.step("Resolve CorsConfiguration for request",
                            () -> urlSource.getCorsConfiguration(request));
                    List<String> origins = config.getAllowedOriginPatterns();
                    Allure.addAttachment("Allowed Origins (empty)", MediaType.TEXT_PLAIN_VALUE,
                            origins == null ? "null" : String.join(",", origins));
                    assertThat(origins).isNull();
                });
    }
}