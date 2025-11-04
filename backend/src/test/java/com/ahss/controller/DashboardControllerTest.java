package com.ahss.controller;

import com.ahss.dto.UserDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void get_dashboard_stats_success_returns_200() throws Exception {
        when(userService.getAllActiveUsers()).thenReturn(List.of(new UserDto()));

        mockMvc.perform(get("/api/v1/dashboard/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Dashboard statistics retrieved successfully")))
                .andExpect(jsonPath("$.path", is("/api/v1/dashboard/stats")));
    }

    @Test
    void get_dashboard_stats_error_returns_500() throws Exception {
        when(userService.getAllActiveUsers()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/v1/dashboard/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Failed to retrieve dashboard statistics")))
                .andExpect(jsonPath("$.path", is("/api/v1/dashboard/stats")));
    }
}