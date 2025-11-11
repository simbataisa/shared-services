package com.ahss.controller;

import com.ahss.dto.TenantDto;
import com.ahss.entity.TenantStatus;
import com.ahss.service.TenantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TenantController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("Catalogue")
@Feature("Tenant")
@Owner("backend")
class TenantControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TenantService tenantService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void applyLabels() {
    // Using annotations; no runtime labels
  }

  @Test
  @DisplayName("GET /tenants/{id} returns 404 when missing")
  @Story("Get tenant by ID - not found")
  @Severity(SeverityLevel.MINOR)
  void get_tenant_by_id_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to return empty for id=99",
        () -> when(tenantService.getTenantById(99L)).thenReturn(Optional.empty()));

    var result =
        Allure.step(
            "GET /api/v1/tenants/99",
            () ->
                mockMvc
                    .perform(get("/api/v1/tenants/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Tenant not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/tenants/99")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get tenant by code returns 404 when missing")
  void get_tenant_by_code_not_found_returns_404() throws Exception {
    when(tenantService.getTenantByCode("NOPE")).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/v1/tenants/code/NOPE"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.message", is("Tenant not found")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/code/NOPE")));
  }

  @Test
  @Story("Create tenant returns 400 on validation error")
  void create_tenant_bad_request_returns_400() throws Exception {
    // Validation will fail before service is called; response body will be empty
    TenantDto dto = new TenantDto();
    dto.setName("ACME");
    dto.setCode("1");
    String body = objectMapper.writeValueAsString(dto);
    mockMvc
        .perform(post("/api/v1/tenants").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("Tenant code must be between 2 and 50 characters")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants")));
  }

  @Test
  @Story("Update tenant returns 404 when missing")
  void update_tenant_not_found_returns_404() throws Exception {
    when(tenantService.updateTenant(eq(77L), any(TenantDto.class)))
        .thenThrow(new RuntimeException("Tenant not found with id: 77"));
    TenantDto dto = new TenantDto();
    dto.setName("ACME");
    dto.setCode("ACME");
    String body = objectMapper.writeValueAsString(dto);
    mockMvc
        .perform(put("/api/v1/tenants/77").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", containsString("Tenant not found")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/77")));
  }

  @Test
  @Story("Update tenant returns 400 on invalid data")
  void update_tenant_bad_request_returns_400() throws Exception {
    when(tenantService.updateTenant(eq(78L), any(TenantDto.class)))
        .thenThrow(new RuntimeException("Invalid data"));
    TenantDto dto = new TenantDto();
    dto.setName("ACME");
    dto.setCode("ACME");
    String body = objectMapper.writeValueAsString(dto);
    mockMvc
        .perform(put("/api/v1/tenants/78").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("Invalid data")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/78")));
  }

  @Test
  @Story("Delete tenant returns 404 when missing")
  void delete_tenant_not_found_returns_404() throws Exception {
    doThrow(new RuntimeException("Tenant not found with id: 90"))
        .when(tenantService)
        .deleteTenant(90L);

    mockMvc
        .perform(delete("/api/v1/tenants/90"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", containsString("Tenant not found")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/90")));
  }

  @Test
  @Story("Update tenant status returns 400 when status missing")
  void update_status_missing_status_returns_400() throws Exception {
    String body = objectMapper.writeValueAsString(Map.of());
    mockMvc
        .perform(
            patch("/api/v1/tenants/5/status").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", is("Status is required")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/5/status")));
  }

  @Test
  @Story("Update tenant status returns 400 with invalid value")
  void update_status_invalid_value_returns_400() throws Exception {
    String body = objectMapper.writeValueAsString(Map.of("status", "WRONG"));
    mockMvc
        .perform(
            patch("/api/v1/tenants/5/status").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", containsString("Invalid status")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/5/status")));
  }

  @Test
  @Story("Update tenant status returns 404 when tenant missing")
  void update_status_not_found_returns_404() throws Exception {
    // When status ACTIVE triggers activateTenant, simulate not found
    doThrow(new RuntimeException("Tenant not found with id: 5"))
        .when(tenantService)
        .activateTenant(5L);
    String body = objectMapper.writeValueAsString(Map.of("status", "ACTIVE"));
    mockMvc
        .perform(
            patch("/api/v1/tenants/5/status").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.message", containsString("Tenant not found")))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/5/status")));
  }

  @Test
  @Story("Activate/Deactivate/Suspend tenant returns 404 when missing")
  void activate_deactivate_suspend_not_found_return_404() throws Exception {
    doThrow(new RuntimeException("Tenant not found with id: 11"))
        .when(tenantService)
        .activateTenant(11L);
    doThrow(new RuntimeException("Tenant not found with id: 12"))
        .when(tenantService)
        .deactivateTenant(12L);
    doThrow(new RuntimeException("Tenant not found with id: 13"))
        .when(tenantService)
        .suspendTenant(13L);

    mockMvc
        .perform(patch("/api/v1/tenants/11/activate"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/11/activate")));

    mockMvc
        .perform(patch("/api/v1/tenants/12/deactivate"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/12/deactivate")));

    mockMvc
        .perform(patch("/api/v1/tenants/13/suspend"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.path", is("/api/v1/tenants/13/suspend")));
  }
}
