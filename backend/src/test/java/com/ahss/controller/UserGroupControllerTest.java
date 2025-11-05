package com.ahss.controller;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.request.UpdateUserGroupRequest;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.service.UserGroupService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserGroupController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("IAM")
@Feature("User Group Management")
@Owner("backend")
class UserGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserGroupService userGroupService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Story("Get user group by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_user_group_by_id_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for id=77", () ->
                when(userGroupService.getById(77L)).thenThrow(new IllegalArgumentException("User group not found"))
        );

        var result = Allure.step("GET /api/v1/user-groups/77", () ->
                mockMvc.perform(get("/api/v1/user-groups/77"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User group not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/user-groups/77")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update user group returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void update_user_group_not_found_returns_404() throws Exception {
        Allure.step("Stub update to throw not found for id=88", () ->
                when(userGroupService.update(eq(88L), any(UpdateUserGroupRequest.class)))
                        .thenThrow(new IllegalArgumentException("User group not found"))
        );

        UpdateUserGroupRequest req = new UpdateUserGroupRequest();
        req.setName("Managers");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);
        var result = Allure.step("PUT /api/v1/user-groups/88", () ->
                mockMvc.perform(put("/api/v1/user-groups/88")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User group not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/user-groups/88")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete user group returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void delete_user_group_not_found_returns_404() throws Exception {
        Allure.step("Stub delete to throw not found for id=99", () ->
                doThrow(new IllegalArgumentException("User group not found")).when(userGroupService).delete(99L)
        );

        var result = Allure.step("DELETE /api/v1/user-groups/99", () ->
                mockMvc.perform(delete("/api/v1/user-groups/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User group not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/user-groups/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get user group by ID returns 200 when found")
    @Severity(SeverityLevel.NORMAL)
    void get_user_group_by_id_found_returns_200() throws Exception {
        var resp = new UserGroupResponse(50L, "Team X", "desc", 1, 2, com.ahss.entity.UserGroupStatus.ACTIVE);
        when(userGroupService.getById(50L)).thenReturn(resp);

        var result = Allure.step("GET /api/v1/user-groups/50", () ->
                mockMvc.perform(get("/api/v1/user-groups/50"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("User group retrieved successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/user-groups/50")))
                        .andExpect(jsonPath("$.data.userGroupId", is(50)))
                        .andExpect(jsonPath("$.data.name", is("Team X")))
                        .andExpect(jsonPath("$.data.memberCount", is(1)))
                        .andExpect(jsonPath("$.data.roleCount", is(2)))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }
}