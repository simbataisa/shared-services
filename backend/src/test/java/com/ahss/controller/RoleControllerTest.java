package com.ahss.controller;

import com.ahss.dto.RoleDto;
import com.ahss.service.RoleService;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("IAM")
@Feature("Role Management")
@Owner("backend")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void applyLabels() {
        // Using annotations; no runtime labels
    }

    @Test
    @Story("Get role by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_role_by_id_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for id=99", () ->
                when(roleService.getRoleById(99L)).thenReturn(Optional.empty())
        );

        var result = Allure.step("GET /api/v1/roles/99", () ->
                mockMvc.perform(get("/api/v1/roles/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Role not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create role returns 400 on validation error")
    @Severity(SeverityLevel.MINOR)
    void create_role_bad_request_returns_400() throws Exception {
        // Validation will fail before service is called; response body will be empty
        RoleDto dto = new RoleDto();
        dto.setName("");
        String body = objectMapper.writeValueAsString(dto);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("POST /api/v1/roles", () ->
                mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Role name is required")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update role returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void update_role_not_found_returns_404() throws Exception {
        Allure.step("Stub updateRole to throw not found for id=77", () ->
                when(roleService.updateRole(eq(77L), any(RoleDto.class)))
                        .thenThrow(new IllegalArgumentException("Role not found with id: 77"))
        );

        RoleDto dto = new RoleDto();
        dto.setName("Manager");
        String body = objectMapper.writeValueAsString(dto);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("PUT /api/v1/roles/77", () ->
                mockMvc.perform(put("/api/v1/roles/77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Role not found with id: 77")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/77")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete role returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void delete_role_not_found_returns_404() throws Exception {
        Allure.step("Stub deleteRole to throw not found for id=88", () ->
                doThrow(new IllegalArgumentException("Role not found with id: 88")).when(roleService).deleteRole(88L)
        );

        var result = Allure.step("DELETE /api/v1/roles/88", () ->
                mockMvc.perform(delete("/api/v1/roles/88"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Role not found with id: 88")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/88")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Activate role returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void activate_role_not_found_returns_404() throws Exception {
        Allure.step("Stub activateRole to throw not found for id=55", () ->
                doThrow(new IllegalArgumentException("Role not found with id: 55")).when(roleService).activateRole(55L)
        );

        var result = Allure.step("PATCH /api/v1/roles/55/activate", () ->
                mockMvc.perform(patch("/api/v1/roles/55/activate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Role not found with id: 55")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/55/activate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Deactivate role returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void deactivate_role_not_found_returns_404() throws Exception {
        Allure.step("Stub deactivateRole to throw not found for id=56", () ->
                doThrow(new IllegalArgumentException("Role not found with id: 56")).when(roleService).deactivateRole(56L)
        );

        var result = Allure.step("PATCH /api/v1/roles/56/deactivate", () ->
                mockMvc.perform(patch("/api/v1/roles/56/deactivate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Role not found with id: 56")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/56/deactivate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Assign permissions returns 400 on invalid input")
    @Severity(SeverityLevel.MINOR)
    void assign_permissions_bad_request_returns_400() throws Exception {
        Allure.step("Stub assignPermissions to throw invalid input for role=10", () ->
                when(roleService.assignPermissions(eq(10L), any(List.class)))
                        .thenThrow(new IllegalArgumentException("Invalid permissions"))
        );

        String body = "[1,2,3]";
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("PUT /api/v1/roles/10/permissions", () ->
                mockMvc.perform(put("/api/v1/roles/10/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Invalid permissions")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/10/permissions")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Remove permissions returns 400 on invalid input")
    @Severity(SeverityLevel.MINOR)
    void remove_permissions_bad_request_returns_400() throws Exception {
        Allure.step("Stub removePermissions to throw invalid input for role=10", () ->
                when(roleService.removePermissions(eq(10L), any(List.class)))
                        .thenThrow(new IllegalArgumentException("Invalid permissions"))
        );

        String body = "[1,2,3]";
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("DELETE /api/v1/roles/10/permissions", () ->
                mockMvc.perform(delete("/api/v1/roles/10/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Invalid permissions")))
                        .andExpect(jsonPath("$.path", is("/api/v1/roles/10/permissions")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }
}