package com.ahss.config;

import com.ahss.security.JwtTokenProvider;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import({SecurityConfig.class, WebConfig.class})
@Epic("Security")
@Feature("Filter Chain")
@Owner("backend")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Story("Protected endpoint without token is blocked")
    @Severity(SeverityLevel.CRITICAL)
    void protected_endpoint_without_token_is_unauthorized_or_forbidden() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/protected-test"))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Allure.addAttachment("Unauthorized response status", MediaType.TEXT_PLAIN_VALUE,
                String.valueOf(result.getResponse().getStatus()));
        Allure.addAttachment("Unauthorized response body", MediaType.TEXT_PLAIN_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Protected endpoint with valid JWT is allowed")
    @Severity(SeverityLevel.CRITICAL)
    void protected_endpoint_with_valid_bearer_token_is_allowed() throws Exception {
        String token = Allure.step("Generate JWT token",
                () -> JwtTokenProvider.generateToken("tester@ahss.com"));
        MvcResult result = mockMvc.perform(get("/api/v1/protected-test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        Allure.addAttachment("Authorized response status", MediaType.TEXT_PLAIN_VALUE,
                String.valueOf(result.getResponse().getStatus()));
        Allure.addAttachment("Authorized response body", MediaType.TEXT_PLAIN_VALUE,
                result.getResponse().getContentAsString());
    }
}