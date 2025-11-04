package com.ahss.controller;

import com.ahss.dto.UserDto;
import com.ahss.dto.request.CreateUserRequest;
import com.ahss.dto.request.UpdateUserRequest;
import com.ahss.dto.response.ApiResponse;
import com.ahss.dto.response.RoleResponse;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.dto.response.UserResponse;
import com.ahss.entity.UserStatus;
import com.ahss.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Use fully qualified Swagger annotations to avoid confusion and import issues

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Users", description = "Manage users, status, roles, and groups")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class)))
})
public class UserController {

    @Autowired
    private UserService userService;

    // Get all active users
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List users", description = "Retrieve all active users")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserDto> users = userService.getAllActiveUsers();
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(userResponses, "Users retrieved successfully", "/api/v1/users"));
    }

    // Get users by status
    @GetMapping("/status/{status}")
    @io.swagger.v3.oas.annotations.Operation(summary = "List users by status", description = "Retrieve users filtered by status")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByStatus(@PathVariable UserStatus status) {
        List<UserDto> users = userService.getUsersByStatus(status);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(userResponses, "Users retrieved successfully", "/api/v1/users/status/" + status));
    }

    // Get user by ID
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        Optional<UserDto> user = userService.getUserById(id);
        if (user.isPresent()) {
            UserResponse userResponse = convertToResponse(user.get());
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "User retrieved successfully", "/api/v1/users/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, "User not found", "/api/v1/users/" + id));
        }
    }

    // Get user by username
    @GetMapping("/username/{username}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get user by username", description = "Retrieve user details by username")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        Optional<UserDto> user = userService.getUserByUsername(username);
        if (user.isPresent()) {
            UserResponse userResponse = convertToResponse(user.get());
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "User retrieved successfully", "/api/v1/users/username/" + username));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, "User not found", "/api/v1/users/username/" + username));
        }
    }

    // Get user by email
    @GetMapping("/email/{email}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get user by email", description = "Retrieve user details by email")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        Optional<UserDto> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            UserResponse userResponse = convertToResponse(user.get());
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "User retrieved successfully", "/api/v1/users/email/" + email));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, "User not found", "/api/v1/users/email/" + email));
        }
    }

    // Search users
    @GetMapping("/search")
    @io.swagger.v3.oas.annotations.Operation(summary = "Search users", description = "Full-text search users by username or email")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String query) {
        List<UserDto> users = userService.searchUsers(query);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(userResponses, "Search completed successfully", "/api/v1/users/search"));
    }

    // Create user
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create user", description = "Create a new user",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(name = "CreateUser",
                    value = "{\n  \"username\": \"jdoe\",\n  \"email\": \"jdoe@example.com\",\n  \"password\": \"Str0ngP@ss!\",\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"roleIds\": [1,2],\n  \"userGroupIds\": [3]\n}"))))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserDto userDto = convertCreateRequestToDto(request);
            UserDto createdUser = userService.createUser(userDto);
            
            // Assign roles if provided
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                createdUser = userService.assignRoles(createdUser.getId(), request.getRoleIds());
            }
            
            // Assign user groups if provided
            if (request.getUserGroupIds() != null && !request.getUserGroupIds().isEmpty()) {
                createdUser = userService.assignUserGroups(createdUser.getId(), request.getUserGroupIds());
            }
            
            UserResponse userResponse = convertToResponse(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(userResponse, "User created successfully", "/api/v1/users"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users"));
        }
    }

    // Update user
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update user", description = "Update an existing user by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, 
                                                               @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserDto userDto = convertUpdateRequestToDto(request);
            UserDto updatedUser = userService.updateUser(id, userDto);
            
            // Update roles if provided
            if (request.getRoleIds() != null) {
                // First get current user to compare roles
                Optional<UserDto> currentUser = userService.getUserById(id);
                if (currentUser.isPresent()) {
                    // Remove all current roles and assign new ones
                    List<Long> currentRoleIds = currentUser.get().getRoles().stream()
                            .map(role -> role.getId())
                            .collect(Collectors.toList());
                    if (!currentRoleIds.isEmpty()) {
                        userService.removeRoles(id, currentRoleIds);
                    }
                    if (!request.getRoleIds().isEmpty()) {
                        updatedUser = userService.assignRoles(id, request.getRoleIds());
                    }
                }
            }
            
            // Update user groups if provided
            if (request.getUserGroupIds() != null) {
                // First get current user to compare user groups
                Optional<UserDto> currentUser = userService.getUserById(id);
                if (currentUser.isPresent()) {
                    // Remove all current user groups and assign new ones
                    List<Long> currentUserGroupIds = currentUser.get().getUserGroups().stream()
                            .map(userGroup -> userGroup.getId())
                            .collect(Collectors.toList());
                    if (!currentUserGroupIds.isEmpty()) {
                        userService.removeUserGroups(id, currentUserGroupIds);
                    }
                    if (!request.getUserGroupIds().isEmpty()) {
                        updatedUser = userService.assignUserGroups(id, request.getUserGroupIds());
                    }
                }
            }
            
            UserResponse userResponse = convertToResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "User updated successfully", "/api/v1/users/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id));
        }
    }

    // Delete user (soft delete)
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete user", description = "Soft delete a user by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "User deleted successfully", "/api/v1/users/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id));
        }
    }

    // Activate user
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "User activated successfully", "/api/v1/users/" + id + "/activate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/activate"));
        }
    }

    // Deactivate user
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "User deactivated successfully", "/api/v1/users/" + id + "/deactivate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/deactivate"));
        }
    }

    // Lock user
    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id, 
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lockUntil) {
        try {
            LocalDateTime lockTime = lockUntil != null ? lockUntil : LocalDateTime.now().plusHours(24);
            userService.lockUser(id, lockTime);
            return ResponseEntity.ok(ApiResponse.ok(null, "User locked successfully", "/api/v1/users/" + id + "/lock"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/lock"));
        }
    }

    // Unlock user
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        try {
            userService.unlockUser(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "User unlocked successfully", "/api/v1/users/" + id + "/unlock"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/unlock"));
        }
    }

    // Verify email
    @PatchMapping("/{id}/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable Long id) {
        try {
            userService.verifyEmail(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Email verified successfully", "/api/v1/users/" + id + "/verify-email"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/verify-email"));
        }
    }

    // Change password
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id, 
                                                           @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.ok(null, "New password is required", "/api/v1/users/" + id + "/change-password"));
            }
            userService.changePassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.ok(null, "Password changed successfully", "/api/v1/users/" + id + "/change-password"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/change-password"));
        }
    }

    // Assign roles
    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(@PathVariable Long id, 
                                                                @RequestBody List<Long> roleIds) {
        try {
            UserDto updatedUser = userService.assignRoles(id, roleIds);
            UserResponse userResponse = convertToResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "Roles assigned successfully", "/api/v1/users/" + id + "/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/roles"));
        }
    }

    // Remove roles
    @DeleteMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoles(@PathVariable Long id, 
                                                                @RequestBody List<Long> roleIds) {
        try {
            UserDto updatedUser = userService.removeRoles(id, roleIds);
            UserResponse userResponse = convertToResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "Roles removed successfully", "/api/v1/users/" + id + "/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/roles"));
        }
    }

    // Assign user groups
    @PostMapping("/{id}/user-groups")
    public ResponseEntity<ApiResponse<UserResponse>> assignUserGroups(@PathVariable Long id, 
                                                                     @RequestBody List<Long> userGroupIds) {
        try {
            UserDto updatedUser = userService.assignUserGroups(id, userGroupIds);
            UserResponse userResponse = convertToResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "User groups assigned successfully", "/api/v1/users/" + id + "/user-groups"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/user-groups"));
        }
    }

    // Remove user groups
    @DeleteMapping("/{id}/user-groups")
    public ResponseEntity<ApiResponse<UserResponse>> removeUserGroups(@PathVariable Long id, 
                                                                     @RequestBody List<Long> userGroupIds) {
        try {
            UserDto updatedUser = userService.removeUserGroups(id, userGroupIds);
            UserResponse userResponse = convertToResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.ok(userResponse, "User groups removed successfully", "/api/v1/users/" + id + "/user-groups"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ok(null, e.getMessage(), "/api/v1/users/" + id + "/user-groups"));
        }
    }

    // Get locked users
    @GetMapping("/locked")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getLockedUsers() {
        List<UserDto> users = userService.getLockedUsers();
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(userResponses, "Locked users retrieved successfully", "/api/v1/users/locked"));
    }

    // Get unverified users
    @GetMapping("/unverified")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUnverifiedUsers() {
        List<UserDto> users = userService.getUnverifiedUsers();
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(userResponses, "Unverified users retrieved successfully", "/api/v1/users/unverified"));
    }

    // Get inactive users
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getInactiveUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        LocalDateTime cutoff = cutoffDate != null ? cutoffDate : LocalDateTime.now().minusDays(30);
        List<UserDto> users = userService.getInactiveUsers(cutoff);
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(userResponses, "Inactive users retrieved successfully", "/api/v1/users/inactive"));
    }

    // Check if username exists
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.ok(exists, "Username existence checked", "/api/v1/users/exists/username/" + username));
    }

    // Check if email exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(exists, "Email existence checked", "/api/v1/users/exists/email/" + email));
    }

    // Helper methods for conversion
    private UserResponse convertToResponse(UserDto dto) {
        UserResponse response = new UserResponse();
        response.setId(dto.getId());
        response.setUsername(dto.getUsername());
        response.setEmail(dto.getEmail());
        response.setFirstName(dto.getFirstName());
        response.setLastName(dto.getLastName());
        response.setPhoneNumber(dto.getPhoneNumber());
        response.setUserStatus(dto.getUserStatus());
        response.setEmailVerified(dto.getEmailVerified());
        response.setLastLogin(dto.getLastLogin());
        response.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        response.setAccountLockedUntil(dto.getAccountLockedUntil());
        response.setPasswordChangedAt(dto.getPasswordChangedAt());
        response.setCreatedAt(dto.getCreatedAt());
        response.setUpdatedAt(dto.getUpdatedAt());
        response.setCreatedBy(dto.getCreatedBy());
        response.setUpdatedBy(dto.getUpdatedBy());
        
        // Convert roles
        if (dto.getRoles() != null) {
            List<RoleResponse> roleResponses = dto.getRoles().stream()
                    .map(this::convertRoleToResponse)
                    .collect(Collectors.toList());
            response.setRoles(roleResponses);
        }
        
        // Convert user groups
        if (dto.getUserGroups() != null) {
            List<UserGroupResponse> userGroupResponses = dto.getUserGroups().stream()
                    .map(this::convertUserGroupToResponse)
                    .collect(Collectors.toList());
            response.setUserGroups(userGroupResponses);
        }
        
        return response;
    }

    private RoleResponse convertRoleToResponse(com.ahss.dto.RoleDto roleDto) {
        RoleResponse response = new RoleResponse();
        response.setId(roleDto.getId());
        response.setName(roleDto.getName());
        response.setDescription(roleDto.getDescription());
        response.setRoleStatus(roleDto.getRoleStatus());
        response.setCreatedAt(roleDto.getCreatedAt());
        response.setUpdatedAt(roleDto.getUpdatedAt());
        return response;
    }

    private UserGroupResponse convertUserGroupToResponse(com.ahss.dto.UserGroupDto userGroupDto) {
        return new UserGroupResponse(
                userGroupDto.getId(),
                userGroupDto.getName(),
                userGroupDto.getDescription(),
                userGroupDto.getUsers() != null ? userGroupDto.getUsers().size() : 0,
                userGroupDto.getRoles() != null ? userGroupDto.getRoles().size() : 0
        );
    }

    private UserDto convertCreateRequestToDto(CreateUserRequest request) {
        UserDto dto = new UserDto();
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setPassword(request.getPassword());
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setUserStatus(request.getUserStatus());
        dto.setEmailVerified(request.getEmailVerified());
        dto.setCreatedBy(request.getCreatedBy());
        return dto;
    }

    private UserDto convertUpdateRequestToDto(UpdateUserRequest request) {
        UserDto dto = new UserDto();
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setUserStatus(request.getUserStatus());
        dto.setUpdatedBy(request.getUpdatedBy());
        return dto;
    }
}