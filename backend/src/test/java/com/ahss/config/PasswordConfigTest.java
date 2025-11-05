package com.ahss.config;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Security")
@Feature("Password Encoder")
@Owner("backend")
class PasswordConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PasswordConfig.class);

    @Test
    @Story("BCrypt encoder bean is exposed")
    @Severity(SeverityLevel.CRITICAL)
    void passwordEncoder_bean_is_present_and_is_bcrypt() {
        Allure.step("Start context with PasswordConfig", () -> {});
        contextRunner.run(context -> {
            Allure.step("Assert PasswordEncoder bean present",
                    () -> assertThat(context).hasSingleBean(PasswordEncoder.class));
            PasswordEncoder encoder = Allure.step("Fetch PasswordEncoder bean",
                    () -> context.getBean(PasswordEncoder.class));
            Allure.addAttachment("PasswordEncoder type", MediaType.TEXT_PLAIN_VALUE,
                    encoder.getClass().getName());
            assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        });
    }
}