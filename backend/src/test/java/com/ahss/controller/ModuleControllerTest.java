package com.ahss.controller;

import com.ahss.dto.ModuleDto;
import com.ahss.entity.ModuleStatus;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.ModuleService;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ModuleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("Catalogue")
@Feature("Module")
@Owner("backend")
class ModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModuleService moduleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void applyLabels() {
        // Using annotations; no runtime labels
    }

    private ModuleDto module(long id, long productId, String name) {
        ModuleDto dto = new ModuleDto();
        dto.setId(id);
        dto.setProductId(productId);
        dto.setName(name);
        dto.setDescription("desc");
        dto.setCode("MOD-" + id);
        dto.setModuleStatus(ModuleStatus.ACTIVE);
        return dto;
    }

    @Test
    @Story("List modules returns 200 with data")
    @Severity(SeverityLevel.NORMAL)
    void get_all_modules_returns_200_with_list() throws Exception {
        Allure.step("Stub service to return 2 active modules", () ->
                when(moduleService.getAllActiveModules())
                        .thenReturn(List.of(module(1L, 10L, "Payments"), module(2L, 10L, "Refunds")))
        );

        var result = Allure.step("GET /api/v1/modules", () ->
                mockMvc.perform(get("/api/v1/modules"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Modules retrieved successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules")))
                        .andExpect(jsonPath("$.data", hasSize(2)))
                        .andExpect(jsonPath("$.data[0].name", is("Payments")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
        Allure.step("Verify service interactions", () -> verify(moduleService, times(1)).getAllActiveModules());
    }

    @Test
    @Story("Get module by ID returns 200 when found")
    @Severity(SeverityLevel.NORMAL)
    void get_module_by_id_found_returns_200() throws Exception {
        Allure.step("Stub service to return module id=1", () ->
                when(moduleService.getModuleById(1L)).thenReturn(Optional.of(module(1L, 10L, "Payments")))
        );

        var result = Allure.step("GET /api/v1/modules/1", () ->
                mockMvc.perform(get("/api/v1/modules/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.data.id", is(1)))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/1")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get module by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_module_by_id_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for id=99", () ->
                when(moduleService.getModuleById(99L)).thenReturn(Optional.empty())
        );

        var result = Allure.step("GET /api/v1/modules/99", () ->
                mockMvc.perform(get("/api/v1/modules/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Module not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create module returns 201 on success")
    @Severity(SeverityLevel.NORMAL)
    void create_module_success_returns_201() throws Exception {
        ModuleDto created = module(5L, 10L, "Billing");
        Allure.step("Stub service to create module", () ->
                when(moduleService.createModule(any(ModuleDto.class))).thenReturn(created)
        );

        ModuleDto req = new ModuleDto();
        req.setName("Billing");
        req.setCode("BILL");
        req.setDescription("billing module");
        req.setProductId(10L);
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("POST /api/v1/modules", () ->
                mockMvc.perform(post("/api/v1/modules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Module created successfully")))
                        .andExpect(jsonPath("$.data.id", is(5)))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create module returns 400 on bad request")
    @Severity(SeverityLevel.MINOR)
    void create_module_bad_request_returns_400() throws Exception {
        Allure.step("Stub service to throw product not found error", () ->
                when(moduleService.createModule(any(ModuleDto.class))).thenThrow(new IllegalArgumentException("Product not found with id: 10"))
        );

        ModuleDto req = new ModuleDto();
        req.setName("Billing");
        req.setCode("BILL");
        req.setDescription("billing module");
        req.setProductId(10L);
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("POST /api/v1/modules (bad request)", () ->
                mockMvc.perform(post("/api/v1/modules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Product not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update module returns 200 on success")
    @Severity(SeverityLevel.NORMAL)
    void update_module_success_returns_200() throws Exception {
        ModuleDto updated = module(5L, 10L, "Billing-Updated");
        Allure.step("Stub service to update module id=5", () ->
                when(moduleService.updateModule(eq(5L), any(ModuleDto.class))).thenReturn(updated)
        );

        ModuleDto req = new ModuleDto();
        req.setName("Billing-Updated");
        req.setCode("BILL");
        req.setDescription("updated");
        req.setProductId(10L);
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("PUT /api/v1/modules/5", () ->
                mockMvc.perform(put("/api/v1/modules/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Module updated successfully")))
                        .andExpect(jsonPath("$.data.name", is("Billing-Updated")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/5")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update module returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void update_module_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for id=99", () ->
                when(moduleService.updateModule(eq(99L), any(ModuleDto.class))).thenThrow(new IllegalArgumentException("Module not found with id: 99"))
        );

        ModuleDto req = new ModuleDto();
        req.setName("X");
        req.setCode("X");
        req.setProductId(10L);
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("PUT /api/v1/modules/99 (not found)", () ->
                mockMvc.perform(put("/api/v1/modules/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Module not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete module returns 200 on success")
    @Severity(SeverityLevel.NORMAL)
    void delete_module_success_returns_200() throws Exception {
        Allure.step("Stub service to delete module id=7", () ->
                doNothing().when(moduleService).deleteModule(7L)
        );

        var result = Allure.step("DELETE /api/v1/modules/7", () ->
                mockMvc.perform(delete("/api/v1/modules/7"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Module deleted successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/7")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete module returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void delete_module_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for delete id=77", () ->
                doThrow(new IllegalArgumentException("Module not found with id: 77")).when(moduleService).deleteModule(77L)
        );

        var result = Allure.step("DELETE /api/v1/modules/77 (not found)", () ->
                mockMvc.perform(delete("/api/v1/modules/77"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Module not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/77")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Activate module returns 200 on success")
    @Severity(SeverityLevel.NORMAL)
    void activate_module_success_returns_200() throws Exception {
        Allure.step("Stub service to activate module id=5", () ->
                doNothing().when(moduleService).activateModule(5L)
        );

        var result = Allure.step("PUT /api/v1/modules/5/activate", () ->
                mockMvc.perform(put("/api/v1/modules/5/activate"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Module activated successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/5/activate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Deactivate module returns 200 on success")
    @Severity(SeverityLevel.NORMAL)
    void deactivate_module_success_returns_200() throws Exception {
        Allure.step("Stub service to deactivate module id=5", () ->
                doNothing().when(moduleService).deactivateModule(5L)
        );

        var result = Allure.step("PUT /api/v1/modules/5/deactivate", () ->
                mockMvc.perform(put("/api/v1/modules/5/deactivate"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Module deactivated successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/5/deactivate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Activate module returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void activate_module_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for activate id=55", () ->
                doThrow(new IllegalArgumentException("Module not found with id: 55")).when(moduleService).activateModule(55L)
        );

        var result = Allure.step("PUT /api/v1/modules/55/activate (not found)", () ->
                mockMvc.perform(put("/api/v1/modules/55/activate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Module not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/55/activate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Deactivate module returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void deactivate_module_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for deactivate id=56", () ->
                doThrow(new IllegalArgumentException("Module not found with id: 56")).when(moduleService).deactivateModule(56L)
        );

        var result = Allure.step("PUT /api/v1/modules/56/deactivate (not found)", () ->
                mockMvc.perform(put("/api/v1/modules/56/deactivate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Module not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/modules/56/deactivate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }
}