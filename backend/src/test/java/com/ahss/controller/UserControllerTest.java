package com.ahss.controller;

import com.ahss.dto.request.CreateUserRequest;
import com.ahss.dto.request.UpdateUserRequest;
import com.ahss.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("IAM")
@Feature("User Management")
@Owner("backend")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @Story("Get user by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_user_by_id_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for id=99", () ->
                when(userService.getUserById(99L)).thenReturn(Optional.empty())
        );

        var result = Allure.step("GET /api/v1/users/99", () ->
                mockMvc.perform(get("/api/v1/users/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/users/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get user by username returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_user_by_username_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for username=nouser", () ->
                when(userService.getUserByUsername("nouser")).thenReturn(Optional.empty())
        );

        var result = Allure.step("GET /api/v1/users/username/nouser", () ->
                mockMvc.perform(get("/api/v1/users/username/nouser"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/users/username/nouser")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get user by email returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_user_by_email_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for email=no@user.com", () ->
                when(userService.getUserByEmail("no@user.com")).thenReturn(Optional.empty())
        );

        var result = Allure.step("GET /api/v1/users/email/no@user.com", () ->
                mockMvc.perform(get("/api/v1/users/email/no@user.com"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/users/email/no@user.com")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create user returns 400 on validation error")
    @Severity(SeverityLevel.MINOR)
    void create_user_bad_request_returns_400() throws Exception {
        // Build invalid DTO (empty username, invalid email, empty password)
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("");
        request.setEmail("bad");
        request.setPassword("");

        String json = mapper.writeValueAsString(request);
        Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);
        var result = Allure.step("POST /api/v1/users with invalid DTO", () ->
                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string(""))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update user returns 400 on validation error")
    @Severity(SeverityLevel.MINOR)
    void update_user_bad_request_returns_400() throws Exception {
        // Build invalid DTO (only username set; missing required fields)
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("john");

        String json = mapper.writeValueAsString(request);
        Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);
        var result = Allure.step("PUT /api/v1/users/77 with invalid DTO", () ->
                mockMvc.perform(put("/api/v1/users/77")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string(""))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete user returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void delete_user_not_found_returns_404() throws Exception {
        Allure.step("Stub deleteUser to throw not found for id=99", () ->
                doThrow(new IllegalArgumentException("User not found with id: 99")).when(userService).deleteUser(99L)
        );

        var result = Allure.step("DELETE /api/v1/users/99", () ->
                mockMvc.perform(delete("/api/v1/users/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User not found with id: 99")))
                        .andExpect(jsonPath("$.path", is("/api/v1/users/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }
}
