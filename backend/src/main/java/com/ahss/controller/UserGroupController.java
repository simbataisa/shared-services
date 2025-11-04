package com.ahss.controller;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.request.UpdateUserGroupRequest;
import com.ahss.dto.response.ApiResponse;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.service.UserGroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Use fully qualified Swagger annotations to avoid import issues

@RestController
@RequestMapping("/api/v1/user-groups")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User Groups", description = "Manage user permission groups")
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
public class UserGroupController {
    private final UserGroupService service;

    public UserGroupController(UserGroupService service) { 
        this.service = service; 
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create user group", description = "Create a new permission group")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User group created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<UserGroupResponse>> create(@Valid @RequestBody CreateUserGroupRequest request) {
        UserGroupResponse resp = service.create(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(resp, "Permission group created successfully", "/api/v1/user-groups"));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List user groups", description = "Paginated list of user groups")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operation successful")
    })
    public ResponseEntity<ApiResponse<Page<UserGroupResponse>>> list(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Page<UserGroupResponse> resp = service.list(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(resp, "Operation successful", "/api/v1/user-groups"));
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get user group by ID", description = "Retrieve a user group by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User group retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User group not found")
    })
    public ResponseEntity<ApiResponse<UserGroupResponse>> getById(@PathVariable Long id) {
        try {
            UserGroupResponse resp = service.getById(id);
            return ResponseEntity.ok(ApiResponse.ok(resp, "User group retrieved successfully", "/api/v1/user-groups/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.ok(null, e.getMessage(), "/api/v1/user-groups/" + id));
        }
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update user group", description = "Update a user group by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User group updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User group not found")
    })
    public ResponseEntity<ApiResponse<UserGroupResponse>> update(@PathVariable Long id, 
                                                               @Valid @RequestBody UpdateUserGroupRequest request) {
        try {
            UserGroupResponse resp = service.update(id, request);
            return ResponseEntity.ok(ApiResponse.ok(resp, "User group updated successfully", "/api/v1/user-groups/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.ok(null, e.getMessage(), "/api/v1/user-groups/" + id));
        }
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete user group", description = "Delete a user group by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User group deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User group not found")
    })
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "User group deleted successfully", "/api/v1/user-groups/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.ok(null, e.getMessage(), "/api/v1/user-groups/" + id));
        }
    }
}