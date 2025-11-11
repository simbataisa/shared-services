package com.ahss.controller;

import com.ahss.dto.PermissionDto;
import com.ahss.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("IAM")
@Feature("Permission Management")
@Owner("backend")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void applyLabels() {
        // Using annotations; no runtime labels
    }

    @Test
    @Story("Get permission by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_permission_by_id_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for id=42",
                () -> when(permissionService.getPermissionById(42L)).thenReturn(Optional.empty()));

        var result = Allure.step("GET /api/v1/permissions/42", () -> mockMvc.perform(get("/api/v1/permissions/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Permission not found")))
                .andExpect(jsonPath("$.path", is("/api/v1/permissions/42")))
                .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create permission returns 400 on validation error")
    @Severity(SeverityLevel.MINOR)
    void create_permission_bad_request_returns_400() throws Exception {
        // Validation will fail before service is called; response body will be empty
        PermissionDto dto = new PermissionDto();
        dto.setName("");
        String body = objectMapper.writeValueAsString(dto);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("POST /api/v1/permissions", () -> mockMvc.perform(post("/api/v1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Permission name is required")))
                .andExpect(jsonPath("$.path", is("/api/v1/permissions")))                .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update permission returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void update_permission_not_found_returns_404() throws Exception {
        Allure.step("Stub updatePermission to throw not found for id=77",
                () -> when(permissionService.updatePermission(eq(77L), any(PermissionDto.class)))
                        .thenThrow(new IllegalArgumentException("Permission not found with id: 77")));
        PermissionDto dto = new PermissionDto();
        dto.setName("perm");
        String body = objectMapper.writeValueAsString(dto);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("PUT /api/v1/permissions/77", () -> mockMvc.perform(put("/api/v1/permissions/77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Permission not found with id: 77")))
                .andExpect(jsonPath("$.path", is("/api/v1/permissions/77")))
                .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete permission returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void delete_permission_not_found_returns_404() throws Exception {
        Allure.step("Stub deletePermission to throw not found for id=88",
                () -> doThrow(new IllegalArgumentException("Permission not found with id: 88")).when(permissionService)
                        .deletePermission(88L));

        var result = Allure.step("DELETE /api/v1/permissions/88",
                () -> mockMvc.perform(delete("/api/v1/permissions/88"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Permission not found with id: 88")))
                        .andExpect(jsonPath("$.path", is("/api/v1/permissions/88")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Activate permission returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void activate_permission_not_found_returns_404() throws Exception {
        Allure.step("Stub activatePermission to throw not found for id=55",
                () -> doThrow(new IllegalArgumentException("Permission not found with id: 55")).when(permissionService)
                        .activatePermission(55L));

        var result = Allure.step("PUT /api/v1/permissions/55/activate",
                () -> mockMvc.perform(put("/api/v1/permissions/55/activate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Permission not found with id: 55")))
                        .andExpect(jsonPath("$.path", is("/api/v1/permissions/55/activate")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Deactivate permission returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void deactivate_permission_not_found_returns_404() throws Exception {
        Allure.step("Stub deactivatePermission to throw not found for id=56",
                () -> doThrow(new IllegalArgumentException("Permission not found with id: 56")).when(permissionService)
                        .deactivatePermission(56L));

        var result = Allure.step("PUT /api/v1/permissions/56/deactivate",
                () -> mockMvc.perform(put("/api/v1/permissions/56/deactivate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Permission not found with id: 56")))
                        .andExpect(jsonPath("$.path", is("/api/v1/permissions/56/deactivate")))
                        .andReturn());
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }
}