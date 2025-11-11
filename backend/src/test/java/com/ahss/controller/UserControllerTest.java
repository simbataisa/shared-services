package com.ahss.controller;

import com.ahss.dto.UserDto;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
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

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  @Story("Get user by ID returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void get_user_by_id_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to return empty for id=99",
        () -> when(userService.getUserById(99L)).thenReturn(Optional.empty()));

    var result =
        Allure.step(
            "GET /api/v1/users/99",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/99")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Activate user returns 200 when successful")
  @Severity(SeverityLevel.NORMAL)
  void activate_user_success_returns_200() throws Exception {
    var result =
        Allure.step(
            "PATCH /api/v1/users/10/activate",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/10/activate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User activated successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/10/activate")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Activate user returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void activate_user_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to throw not found for id=88",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 88"))
                .when(userService)
                .activateUser(88L));
    var result =
        Allure.step(
            "PATCH /api/v1/users/88/activate",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/88/activate"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 88")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/88/activate")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Deactivate user returns 200 when successful")
  @Severity(SeverityLevel.NORMAL)
  void deactivate_user_success_returns_200() throws Exception {
    var result =
        Allure.step(
            "PATCH /api/v1/users/11/deactivate",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/11/deactivate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User deactivated successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/11/deactivate")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Deactivate user returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void deactivate_user_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to throw not found for id=89",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 89"))
                .when(userService)
                .deactivateUser(89L));
    var result =
        Allure.step(
            "PATCH /api/v1/users/89/deactivate",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/89/deactivate"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 89")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/89/deactivate")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Unlock user returns 200 when successful")
  @Severity(SeverityLevel.MINOR)
  void unlock_user_success_returns_200() throws Exception {
    var result =
        Allure.step(
            "PATCH /api/v1/users/12/unlock",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/12/unlock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User unlocked successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/12/unlock")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Unlock user returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void unlock_user_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to throw not found for id=90",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 90"))
                .when(userService)
                .unlockUser(90L));
    var result =
        Allure.step(
            "PATCH /api/v1/users/90/unlock",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/90/unlock"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 90")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/90/unlock")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Check username existence returns 200")
  @Severity(SeverityLevel.MINOR)
  void check_username_exists_returns_200() throws Exception {
    Allure.step(
        "Stub service existsByUsername('alice') -> true",
        () -> when(userService.existsByUsername("alice")).thenReturn(true));

    var result =
        Allure.step(
            "GET /api/v1/users/exists/username/alice",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/exists/username/alice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(true)))
                    .andExpect(jsonPath("$.message", is("Username existence checked")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/exists/username/alice")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Check email existence returns 200")
  @Severity(SeverityLevel.MINOR)
  void check_email_exists_returns_200() throws Exception {
    Allure.step(
        "Stub service existsByEmail('alice@example.com') -> false",
        () -> when(userService.existsByEmail("alice@example.com")).thenReturn(false));

    var result =
        Allure.step(
            "GET /api/v1/users/exists/email/alice@example.com",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/exists/email/alice@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(false)))
                    .andExpect(jsonPath("$.message", is("Email existence checked")))
                    .andExpect(
                        jsonPath("$.path", is("/api/v1/users/exists/email/alice@example.com")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get user by username returns 200 when found")
  @Severity(SeverityLevel.MINOR)
  void get_user_by_username_found_returns_200() throws Exception {
    com.ahss.dto.UserDto dto = new com.ahss.dto.UserDto();
    dto.setId(42L);
    dto.setUsername("alice");
    dto.setEmail("alice@example.com");
    Allure.step(
        "Stub service getUserByUsername -> Optional.of(dto)",
        () -> when(userService.getUserByUsername("alice")).thenReturn(Optional.of(dto)));

    var result =
        Allure.step(
            "GET /api/v1/users/username/alice",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/username/alice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User retrieved successfully")))
                    .andExpect(jsonPath("$.data.username", is("alice")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/username/alice")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get locked users returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void get_locked_users_returns_200() throws Exception {
    com.ahss.dto.UserDto u1 = new com.ahss.dto.UserDto();
    com.ahss.dto.UserDto u2 = new com.ahss.dto.UserDto();
    Allure.step(
        "Stub service to return 2 locked users",
        () -> when(userService.getLockedUsers()).thenReturn(java.util.List.of(u1, u2)));

    var result =
        Allure.step(
            "GET /api/v1/users/locked",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/locked"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Locked users retrieved successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(2)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/locked")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get unverified users returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void get_unverified_users_returns_200() throws Exception {
    com.ahss.dto.UserDto u1 = new com.ahss.dto.UserDto();
    Allure.step(
        "Stub service to return 1 unverified user",
        () -> when(userService.getUnverifiedUsers()).thenReturn(java.util.List.of(u1)));

    var result =
        Allure.step(
            "GET /api/v1/users/unverified",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/unverified"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Unverified users retrieved successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/unverified")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get inactive users returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void get_inactive_users_returns_200() throws Exception {
    com.ahss.dto.UserDto u1 = new com.ahss.dto.UserDto();
    Allure.step(
        "Stub service to return 1 inactive user",
        () -> when(userService.getInactiveUsers(any())).thenReturn(java.util.List.of(u1)));

    var result =
        Allure.step(
            "GET /api/v1/users/inactive",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/inactive"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Inactive users retrieved successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/inactive")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get inactive users with cutoffDate returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void get_inactive_users_with_cutoff_date_returns_200() throws Exception {
    com.ahss.dto.UserDto u1 = new com.ahss.dto.UserDto();
    com.ahss.dto.UserDto u2 = new com.ahss.dto.UserDto();
    Allure.step(
        "Stub service to return 2 inactive users",
        () -> when(userService.getInactiveUsers(any())).thenReturn(java.util.List.of(u1, u2)));

    var result =
        Allure.step(
            "GET /api/v1/users/inactive with cutoffDate",
            () ->
                mockMvc
                    .perform(
                        get("/api/v1/users/inactive").param("cutoffDate", "2024-01-01T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Inactive users retrieved successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(2)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/inactive")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get user by username returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void get_user_by_username_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to return empty for username=nouser",
        () -> when(userService.getUserByUsername("nouser")).thenReturn(Optional.empty()));

    var result =
        Allure.step(
            "GET /api/v1/users/username/nouser",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/username/nouser"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/username/nouser")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get user by email returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void get_user_by_email_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to return empty for email=no@user.com",
        () -> when(userService.getUserByEmail("no@user.com")).thenReturn(Optional.empty()));

    var result =
        Allure.step(
            "GET /api/v1/users/email/no@user.com",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/email/no@user.com"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/email/no@user.com")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Create user returns 400 on validation error")
  @Severity(SeverityLevel.MINOR)
  void create_user_bad_request_returns_400() throws Exception {
    // Build invalid DTO (empty username, invalid email, empty password)
    CreateUserRequest request = new CreateUserRequest();
    request.setUsername("12");
    request.setEmail("dennis@test.com");
    request.setPassword("Strong#Password");
    request.setFirstName("Dennis");
    request.setLastName("D");

    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);
    var result =
        Allure.step(
            "POST /api/v1/users with invalid DTO",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Username must be between 3 and 50 characters")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update user returns 400 on validation error")
  @Severity(SeverityLevel.MINOR)
  void update_user_bad_request_returns_400() throws Exception {
    // Build invalid DTO (only username set; missing required fields)
    UpdateUserRequest request =
        Allure.step("Create UpdateUserRequest with username=john", () -> new UpdateUserRequest());
    request.setUsername("john");
    request.setFirstName("john");
    request.setEmail("john@test.com");

    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);
    var result =
        Allure.step(
            "PUT /api/v1/users/77 with invalid DTO",
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/users/77")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.message", is("Last name is required")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/77")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Delete user returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void delete_user_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub deleteUser to throw not found for id=99",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 99"))
                .when(userService)
                .deleteUser(99L));

    var result =
        Allure.step(
            "DELETE /api/v1/users/99",
            () ->
                mockMvc
                    .perform(delete("/api/v1/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 99")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/99")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Verify email returns 200 when successful")
  @Severity(SeverityLevel.MINOR)
  void verify_email_success_returns_200() throws Exception {
    var result =
        Allure.step(
            "PATCH /api/v1/users/21/verify-email",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/21/verify-email"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Email verified successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/21/verify-email")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Verify email returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void verify_email_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to throw not found for id=77",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 77"))
                .when(userService)
                .verifyEmail(77L));
    var result =
        Allure.step(
            "PATCH /api/v1/users/77/verify-email",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/77/verify-email"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 77")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/77/verify-email")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Change password returns 200 when successful")
  @Severity(SeverityLevel.MINOR)
  void change_password_success_returns_200() throws Exception {
    java.util.Map<String, String> payload = java.util.Map.of("newPassword", "Str0ngP@ss!123");
    String json = mapper.writeValueAsString(payload);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "PATCH /api/v1/users/31/change-password",
            () ->
                mockMvc
                    .perform(
                        patch("/api/v1/users/31/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Password changed successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/31/change-password")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Change password returns 400 when missing newPassword")
  @Severity(SeverityLevel.MINOR)
  void change_password_missing_returns_400() throws Exception {
    Map<String, String> payload = new HashMap<>();
    String json = mapper.writeValueAsString(payload);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "PATCH /api/v1/users/32/change-password with missing newPassword",
            () ->
                mockMvc
                    .perform(
                        patch("/api/v1/users/32/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("New password is required")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/32/change-password")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Change password returns 400 when newPassword is empty")
  @Severity(SeverityLevel.MINOR)
  void change_password_empty_returns_400() throws Exception {
    Map<String, String> payload = java.util.Map.of("newPassword", "   ");
    String json = mapper.writeValueAsString(payload);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "PATCH /api/v1/users/33/change-password with empty newPassword",
            () ->
                mockMvc
                    .perform(
                        patch("/api/v1/users/33/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("New password is required")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/33/change-password")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Change password returns 404 when user not found")
  @Severity(SeverityLevel.MINOR)
  void change_password_not_found_returns_404() throws Exception {
    Map<String, String> payload = java.util.Map.of("newPassword", "NewStr0ngP@ss!");
    String json = mapper.writeValueAsString(payload);
    Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, json);

    Allure.step(
        "Stub service changePassword to throw not found",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 34"))
                .when(userService)
                .changePassword(eq(34L), anyString()));

    var result =
        Allure.step(
            "PATCH /api/v1/users/34/change-password for non-existent user",
            () ->
                mockMvc
                    .perform(
                        patch("/api/v1/users/34/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 34")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/34/change-password")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Lock user returns 200 when successful (no param)")
  @Severity(SeverityLevel.MINOR)
  void lock_user_success_no_param_returns_200() throws Exception {
    var result =
        Allure.step(
            "PATCH /api/v1/users/41/lock",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/41/lock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User locked successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/41/lock")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Lock user returns 200 when successful (with param)")
  @Severity(SeverityLevel.MINOR)
  void lock_user_success_with_param_returns_200() throws Exception {
    var result =
        Allure.step(
            "PATCH /api/v1/users/42/lock?lockUntil=2030-01-01T00:00:00",
            () ->
                mockMvc
                    .perform(
                        patch("/api/v1/users/42/lock").param("lockUntil", "2030-01-01T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User locked successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/42/lock")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Lock user returns 404 when missing")
  @Severity(SeverityLevel.MINOR)
  void lock_user_not_found_returns_404() throws Exception {
    Allure.step(
        "Stub service to throw not found for id=66",
        () ->
            doThrow(new IllegalArgumentException("User not found with id: 66"))
                .when(userService)
                .lockUser(eq(66L), any()));
    var result =
        Allure.step(
            "PATCH /api/v1/users/66/lock",
            () ->
                mockMvc
                    .perform(patch("/api/v1/users/66/lock"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 66")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/66/lock")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get all users returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void get_all_users_returns_200() throws Exception {
    UserDto u1 = new UserDto();
    u1.setId(1L);
    u1.setUsername("alice");
    u1.setEmail("alice@example.com");
    UserDto u2 = new UserDto();
    u2.setId(2L);
    u2.setUsername("bob");
    u2.setEmail("bob@example.com");
    Allure.step(
        "Stub service to return 2 active users",
        () -> when(userService.getAllActiveUsers()).thenReturn(java.util.List.of(u1, u2)));

    var result =
        Allure.step(
            "GET /api/v1/users",
            () ->
                mockMvc
                    .perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Users retrieved successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(2)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get users by status returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void get_users_by_status_returns_200() throws Exception {
    UserDto u1 = new UserDto();
    u1.setId(3L);
    u1.setUsername("carol");
    u1.setEmail("carol@example.com");
    Allure.step(
        "Stub service to return ACTIVE users",
        () ->
            when(userService.getUsersByStatus(com.ahss.entity.UserStatus.ACTIVE))
                .thenReturn(java.util.List.of(u1)));

    var result =
        Allure.step(
            "GET /api/v1/users/status/ACTIVE",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/status/ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Users retrieved successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/status/ACTIVE")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Search users returns 200 with list")
  @Severity(SeverityLevel.MINOR)
  void search_users_returns_200() throws Exception {
    UserDto u1 = new UserDto();
    u1.setId(4L);
    u1.setUsername("alice");
    u1.setEmail("alice@example.com");
    Allure.step(
        "Stub service to return one search result",
        () -> when(userService.searchUsers("ali")).thenReturn(java.util.List.of(u1)));

    var result =
        Allure.step(
            "GET /api/v1/users/search?query=ali",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/search").param("query", "ali"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Search completed successfully")))
                    .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/search")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get user by ID returns 200 when found")
  @Severity(SeverityLevel.MINOR)
  void get_user_by_id_found_returns_200() throws Exception {
    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(7L);
    dto.setUsername("bob");
    dto.setEmail("bob@example.com");
    Allure.step(
        "Stub service getUserById -> Optional.of(dto)",
        () -> when(userService.getUserById(7L)).thenReturn(java.util.Optional.of(dto)));

    var result =
        Allure.step(
            "GET /api/v1/users/7",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User retrieved successfully")))
                    .andExpect(jsonPath("$.data.username", is("bob")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/7")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Get user by email returns 200 when found")
  @Severity(SeverityLevel.MINOR)
  void get_user_by_email_found_returns_200() throws Exception {
    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(8L);
    dto.setUsername("eve");
    dto.setEmail("eve@example.com");
    Allure.step(
        "Stub service getUserByEmail -> Optional.of(dto)",
        () ->
            when(userService.getUserByEmail("eve@example.com"))
                .thenReturn(java.util.Optional.of(dto)));

    var result =
        Allure.step(
            "GET /api/v1/users/email/eve@example.com",
            () ->
                mockMvc
                    .perform(get("/api/v1/users/email/eve@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User retrieved successfully")))
                    .andExpect(jsonPath("$.data.username", is("eve")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/email/eve@example.com")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Delete user returns 200 when successful")
  @Severity(SeverityLevel.MINOR)
  void delete_user_success_returns_200() throws Exception {
    var result =
        Allure.step(
            "DELETE /api/v1/users/55",
            () ->
                mockMvc
                    .perform(delete("/api/v1/users/55"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User deleted successfully")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/55")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Assign roles returns 200 with user data")
  @Severity(SeverityLevel.MINOR)
  void assign_roles_success_returns_200() throws Exception {
    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(5L);
    dto.setUsername("alice");
    dto.setEmail("alice@example.com");
    Allure.step(
        "Stub service assignRoles -> dto",
        () -> when(userService.assignRoles(eq(5L), anyList())).thenReturn(dto));
    String json = mapper.writeValueAsString(java.util.List.of(1L, 2L));
    Allure.addAttachment("Request Body (roleIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "POST /api/v1/users/5/roles",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users/5/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Roles assigned successfully")))
                    .andExpect(jsonPath("$.data.username", is("alice")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/5/roles")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Assign roles returns 400 when service throws exception")
  @Severity(SeverityLevel.MINOR)
  void assign_roles_service_exception_returns_400() throws Exception {
    Allure.step(
        "Stub service assignRoles to throw exception",
        () ->
            when(userService.assignRoles(eq(85L), anyList()))
                .thenThrow(new IllegalArgumentException("User not found with id: 85")));
    String json = mapper.writeValueAsString(java.util.List.of(1L, 2L));
    Allure.addAttachment("Request Body (roleIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "POST /api/v1/users/85/roles",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users/85/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 85")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/85/roles")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Remove roles returns 200 with user data")
  @Severity(SeverityLevel.MINOR)
  void remove_roles_success_returns_200() throws Exception {
    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(5L);
    dto.setUsername("alice");
    dto.setEmail("alice@example.com");
    Allure.step(
        "Stub service removeRoles -> dto",
        () -> when(userService.removeRoles(eq(5L), anyList())).thenReturn(dto));
    String json = mapper.writeValueAsString(java.util.List.of(1L));
    Allure.addAttachment("Request Body (roleIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "DELETE /api/v1/users/5/roles",
            () ->
                mockMvc
                    .perform(
                        delete("/api/v1/users/5/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Roles removed successfully")))
                    .andExpect(jsonPath("$.data.username", is("alice")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/5/roles")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Remove roles returns 400 when service throws exception")
  @Severity(SeverityLevel.MINOR)
  void remove_roles_service_exception_returns_400() throws Exception {
    Allure.step(
        "Stub service removeRoles to throw exception",
        () ->
            when(userService.removeRoles(eq(86L), anyList()))
                .thenThrow(new IllegalArgumentException("Role not found")));
    String json = mapper.writeValueAsString(java.util.List.of(1L));
    Allure.addAttachment("Request Body (roleIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "DELETE /api/v1/users/86/roles",
            () ->
                mockMvc
                    .perform(
                        delete("/api/v1/users/86/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Role not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/86/roles")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Assign user groups returns 200 with user data")
  @Severity(SeverityLevel.MINOR)
  void assign_user_groups_success_returns_200() throws Exception {
    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(6L);
    dto.setUsername("bob");
    dto.setEmail("bob@example.com");
    Allure.step(
        "Stub service assignUserGroups -> dto",
        () -> when(userService.assignUserGroups(eq(6L), anyList())).thenReturn(dto));
    String json = mapper.writeValueAsString(java.util.List.of(10L));
    Allure.addAttachment("Request Body (userGroupIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "POST /api/v1/users/6/user-groups",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users/6/user-groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User groups assigned successfully")))
                    .andExpect(jsonPath("$.data.username", is("bob")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/6/user-groups")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Assign user groups returns 400 when service throws exception")
  @Severity(SeverityLevel.MINOR)
  void assign_user_groups_service_exception_returns_400() throws Exception {
    Allure.step(
        "Stub service assignUserGroups to throw exception",
        () ->
            when(userService.assignUserGroups(eq(87L), anyList()))
                .thenThrow(new IllegalArgumentException("User not found with id: 87")));
    String json = mapper.writeValueAsString(java.util.List.of(10L));
    Allure.addAttachment("Request Body (userGroupIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "POST /api/v1/users/87/user-groups",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users/87/user-groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 87")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/87/user-groups")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Remove user groups returns 200 with user data")
  @Severity(SeverityLevel.MINOR)
  void remove_user_groups_success_returns_200() throws Exception {
    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(6L);
    dto.setUsername("bob");
    dto.setEmail("bob@example.com");
    Allure.step(
        "Stub service removeUserGroups -> dto",
        () -> when(userService.removeUserGroups(eq(6L), anyList())).thenReturn(dto));
    String json = mapper.writeValueAsString(java.util.List.of(10L));
    Allure.addAttachment("Request Body (userGroupIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "DELETE /api/v1/users/6/user-groups",
            () ->
                mockMvc
                    .perform(
                        delete("/api/v1/users/6/user-groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User groups removed successfully")))
                    .andExpect(jsonPath("$.data.username", is("bob")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/6/user-groups")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Remove user groups returns 400 when service throws exception")
  @Severity(SeverityLevel.MINOR)
  void remove_user_groups_service_exception_returns_400() throws Exception {
    Allure.step(
        "Stub service removeUserGroups to throw exception",
        () ->
            when(userService.removeUserGroups(eq(88L), anyList()))
                .thenThrow(new IllegalArgumentException("User group not found")));
    String json = mapper.writeValueAsString(java.util.List.of(10L));
    Allure.addAttachment("Request Body (userGroupIds)", MediaType.APPLICATION_JSON_VALUE, json);

    var result =
        Allure.step(
            "DELETE /api/v1/users/88/user-groups",
            () ->
                mockMvc
                    .perform(
                        delete("/api/v1/users/88/user-groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User group not found")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/88/user-groups")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Create user returns 201 when successful")
  @Severity(SeverityLevel.NORMAL)
  void create_user_success_returns_201() throws Exception {
    CreateUserRequest request =
        Allure.step("Create CreateUserRequest", () -> new CreateUserRequest());
    request.setUsername("newuser");
    request.setEmail("newuser@example.com");
    request.setPassword("Str0ngP@ss!");
    request.setFirstName("New");
    request.setLastName("User");
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(100L);
    dto.setUsername("newuser");
    dto.setEmail("newuser@example.com");
    Allure.step(
        "Stub service createUser -> dto",
        () -> when(userService.createUser(any())).thenReturn(dto));

    var result =
        Allure.step(
            "POST /api/v1/users",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User created successfully")))
                    .andExpect(jsonPath("$.data.username", is("newuser")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Create user with roles returns 201 when successful")
  @Severity(SeverityLevel.NORMAL)
  void create_user_with_roles_success_returns_201() throws Exception {
    CreateUserRequest request =
        Allure.step("Create CreateUserRequest with roleIds", () -> new CreateUserRequest());
    request.setUsername("newuser2");
    request.setEmail("newuser2@example.com");
    request.setPassword("Str0ngP@ss!");
    request.setFirstName("New");
    request.setLastName("User");
    request.setRoleIds(java.util.List.of(1L, 2L));
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(101L);
    dto.setUsername("newuser2");
    dto.setEmail("newuser2@example.com");
    Allure.step(
        "Stub service createUser -> dto",
        () -> when(userService.createUser(any())).thenReturn(dto));
    Allure.step(
        "Stub service assignRoles -> dto",
        () ->
            when(userService.assignRoles(eq(101L), eq(java.util.List.of(1L, 2L)))).thenReturn(dto));

    var result =
        Allure.step(
            "POST /api/v1/users with roleIds",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User created successfully")))
                    .andExpect(jsonPath("$.data.username", is("newuser2")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Create user with user groups returns 201 when successful")
  @Severity(SeverityLevel.NORMAL)
  void create_user_with_user_groups_success_returns_201() throws Exception {
    CreateUserRequest request =
        Allure.step("Create CreateUserRequest with userGroupIds", () -> new CreateUserRequest());
    request.setUsername("newuser3");
    request.setEmail("newuser3@example.com");
    request.setPassword("Str0ngP@ss!");
    request.setFirstName("New");
    request.setLastName("User");
    request.setUserGroupIds(java.util.List.of(10L, 11L));
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(102L);
    dto.setUsername("newuser3");
    dto.setEmail("newuser3@example.com");
    Allure.step(
        "Stub service createUser -> dto",
        () -> when(userService.createUser(any())).thenReturn(dto));
    Allure.step(
        "Stub service assignUserGroups -> dto",
        () ->
            when(userService.assignUserGroups(eq(102L), eq(java.util.List.of(10L, 11L))))
                .thenReturn(dto));

    var result =
        Allure.step(
            "POST /api/v1/users with userGroupIds",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User created successfully")))
                    .andExpect(jsonPath("$.data.username", is("newuser3")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Create user returns 400 when service throws exception")
  @Severity(SeverityLevel.MINOR)
  void create_user_service_exception_returns_400() throws Exception {
    CreateUserRequest request =
        Allure.step("Create CreateUserRequest", () -> new CreateUserRequest());
    request.setUsername("duplicate");
    request.setEmail("duplicate@example.com");
    request.setPassword("Str0ngP@ss!");
    request.setFirstName("Duplicate");
    request.setLastName("User");
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    Allure.step(
        "Stub service createUser to throw exception",
        () ->
            when(userService.createUser(any()))
                .thenThrow(new IllegalArgumentException("Username already exists")));

    var result =
        Allure.step(
            "POST /api/v1/users with duplicate username",
            () ->
                mockMvc
                    .perform(
                        post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Username already exists")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update user returns 200 when successful")
  @Severity(SeverityLevel.NORMAL)
  void update_user_success_returns_200() throws Exception {
    UpdateUserRequest request =
        Allure.step("Create UpdateUserRequest", () -> new UpdateUserRequest());
    request.setUsername("john");
    request.setEmail("john@example.com");
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setPhoneNumber("123456789");
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(77L);
    dto.setUsername("john");
    dto.setEmail("john@example.com");
    Allure.step(
        "Stub service updateUser -> dto",
        () -> when(userService.updateUser(eq(77L), any())).thenReturn(dto));

    var result =
        Allure.step(
            "PUT /api/v1/users/77",
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/users/77")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User updated successfully")))
                    .andExpect(jsonPath("$.data.username", is("john")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/77")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update user returns 400 when service throws IllegalArgumentException")
  @Severity(SeverityLevel.MINOR)
  void update_user_service_exception_returns_400() throws Exception {
    UpdateUserRequest request =
        Allure.step("Create UpdateUserRequest", () -> new UpdateUserRequest());
    request.setUsername("john");
    request.setEmail("john@example.com");
    request.setFirstName("John");
    request.setLastName("Doe");
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    Allure.step(
        "Stub service updateUser to throw not found",
        () ->
            when(userService.updateUser(eq(71L), any()))
                .thenThrow(new IllegalArgumentException("User not found with id: 71")));

    var result =
        Allure.step(
            "PUT /api/v1/users/71",
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/users/71")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User not found with id: 71")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/71")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update user with roles returns 200 when successful")
  @Severity(SeverityLevel.NORMAL)
  void update_user_with_roles_success_returns_200() throws Exception {
    UpdateUserRequest request =
        Allure.step("Create UpdateUserRequest with roleIds", () -> new UpdateUserRequest());
    request.setUsername("john");
    request.setEmail("john@example.com");
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setRoleIds(java.util.List.of(3L, 4L));
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(78L);
    dto.setUsername("john");
    dto.setEmail("john@example.com");

    // Mock existing user with current roles
    UserDto existingUser = Allure.step("Create existing UserDto with roles", () -> new UserDto());
    existingUser.setId(78L);
    existingUser.setRoles(
        java.util.List.of(createRoleDto(1L, "ROLE_OLD1"), createRoleDto(2L, "ROLE_OLD2")));

    Allure.step(
        "Stub service updateUser -> dto",
        () -> when(userService.updateUser(eq(78L), any())).thenReturn(dto));
    Allure.step(
        "Stub service getUserById -> existing user",
        () -> when(userService.getUserById(78L)).thenReturn(Optional.of(existingUser)));
    Allure.step(
        "Stub service removeRoles -> void",
        () -> when(userService.removeRoles(eq(78L), anyList())).thenReturn(dto));
    Allure.step(
        "Stub service assignRoles -> dto",
        () ->
            when(userService.assignRoles(eq(78L), eq(java.util.List.of(3L, 4L)))).thenReturn(dto));

    var result =
        Allure.step(
            "PUT /api/v1/users/78 with roleIds",
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/users/78")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User updated successfully")))
                    .andExpect(jsonPath("$.data.username", is("john")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/78")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update user with empty roles returns 200 and removes all roles")
  @Severity(SeverityLevel.NORMAL)
  void update_user_with_empty_roles_success_returns_200() throws Exception {
    UpdateUserRequest request =
        Allure.step("Create UpdateUserRequest with empty roleIds", () -> new UpdateUserRequest());
    request.setUsername("john");
    request.setEmail("john@example.com");
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setRoleIds(java.util.List.of());
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(79L);
    dto.setUsername("john");
    dto.setEmail("john@example.com");

    // Mock existing user with current roles
    UserDto existingUser = Allure.step("Create existing UserDto with roles", () -> new UserDto());
    existingUser.setId(79L);
    existingUser.setRoles(java.util.List.of(createRoleDto(1L, "ROLE_OLD1")));

    Allure.step(
        "Stub service updateUser -> dto",
        () -> when(userService.updateUser(eq(79L), any())).thenReturn(dto));
    Allure.step(
        "Stub service getUserById -> existing user",
        () -> when(userService.getUserById(79L)).thenReturn(Optional.of(existingUser)));
    Allure.step(
        "Stub service removeRoles -> dto",
        () -> when(userService.removeRoles(eq(79L), anyList())).thenReturn(dto));

    var result =
        Allure.step(
            "PUT /api/v1/users/79 with empty roleIds",
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/users/79")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User updated successfully")))
                    .andExpect(jsonPath("$.data.username", is("john")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/79")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  @Test
  @Story("Update user with user groups returns 200 when successful")
  @Severity(SeverityLevel.NORMAL)
  void update_user_with_user_groups_success_returns_200() throws Exception {
    UpdateUserRequest request =
        Allure.step("Create UpdateUserRequest with userGroupIds", () -> new UpdateUserRequest());
    request.setUsername("jane");
    request.setEmail("jane@example.com");
    request.setFirstName("Jane");
    request.setLastName("Doe");
    request.setUserGroupIds(java.util.List.of(20L, 21L));
    String json = mapper.writeValueAsString(request);
    Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

    UserDto dto = Allure.step("Create UserDto", () -> new UserDto());
    dto.setId(80L);
    dto.setUsername("jane");
    dto.setEmail("jane@example.com");

    // Mock existing user with current user groups
    UserDto existingUser =
        Allure.step("Create existing UserDto with user groups", () -> new UserDto());
    existingUser.setId(80L);
    existingUser.setUserGroups(
        java.util.List.of(
            createUserGroupDto(10L, "GROUP_OLD1"), createUserGroupDto(11L, "GROUP_OLD2")));

    Allure.step(
        "Stub service updateUser -> dto",
        () -> when(userService.updateUser(eq(80L), any())).thenReturn(dto));
    Allure.step(
        "Stub service getUserById -> existing user",
        () -> when(userService.getUserById(80L)).thenReturn(Optional.of(existingUser)));
    Allure.step(
        "Stub service removeUserGroups -> void",
        () -> when(userService.removeUserGroups(eq(80L), anyList())).thenReturn(dto));
    Allure.step(
        "Stub service assignUserGroups -> dto",
        () ->
            when(userService.assignUserGroups(eq(80L), eq(java.util.List.of(20L, 21L))))
                .thenReturn(dto));

    var result =
        Allure.step(
            "PUT /api/v1/users/80 with userGroupIds",
            () ->
                mockMvc
                    .perform(
                        put("/api/v1/users/80")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("User updated successfully")))
                    .andExpect(jsonPath("$.data.username", is("jane")))
                    .andExpect(jsonPath("$.path", is("/api/v1/users/80")))
                    .andReturn());
    Allure.addAttachment(
        "Response Body",
        MediaType.APPLICATION_JSON_VALUE,
        result.getResponse().getContentAsString());
  }

  // Helper methods for creating DTOs
  private com.ahss.dto.RoleDto createRoleDto(Long id, String name) {
    com.ahss.dto.RoleDto roleDto = new com.ahss.dto.RoleDto();
    roleDto.setId(id);
    roleDto.setName(name);
    return roleDto;
  }

  private com.ahss.dto.UserGroupDto createUserGroupDto(Long id, String name) {
    com.ahss.dto.UserGroupDto groupDto = new com.ahss.dto.UserGroupDto();
    groupDto.setId(id);
    groupDto.setName(name);
    return groupDto;
  }
}
