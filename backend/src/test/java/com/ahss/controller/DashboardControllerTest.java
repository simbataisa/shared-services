package com.ahss.controller;

import com.ahss.dto.UserDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.UserService;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("Observability")
@Feature("Dashboard")
@Owner("backend")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @Story("Get dashboard stats returns 200 with success envelope")
    @Severity(SeverityLevel.MINOR)
    void get_dashboard_stats_success_returns_200() throws Exception {
        Allure.step("Stub UserService to return a single active user", () ->
                when(userService.getAllActiveUsers()).thenReturn(List.of(new UserDto()))
        );

        MvcResult result = Allure.step("GET /api/v1/dashboard/stats (expect 200)", () ->
                mockMvc.perform(get("/api/v1/dashboard/stats")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Dashboard statistics retrieved successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/dashboard/stats")))
                        .andReturn()
        );

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get dashboard stats error returns 500 with error envelope")
    @Severity(SeverityLevel.NORMAL)
    void get_dashboard_stats_error_returns_500() throws Exception {
        Allure.step("Stub UserService to throw RuntimeException('DB error')", () ->
                when(userService.getAllActiveUsers()).thenThrow(new RuntimeException("DB error"))
        );

        MvcResult result = Allure.step("GET /api/v1/dashboard/stats (expect 500)", () ->
                mockMvc.perform(get("/api/v1/dashboard/stats")
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Failed to retrieve dashboard statistics")))
                        .andExpect(jsonPath("$.path", is("/api/v1/dashboard/stats")))
                        .andReturn()
        );

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }
}