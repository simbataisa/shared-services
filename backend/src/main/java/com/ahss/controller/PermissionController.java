package com.ahss.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Use fully qualified Swagger annotations to avoid import issues

import com.ahss.dto.PermissionDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.PermissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/permissions")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Permissions", description = "Manage permissions and actions")
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
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all permissions", description = "Retrieve all active permissions")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.getAllActivePermissions();
        return ResponseEntity.ok(ApiResponse.ok(permissions, "Permissions retrieved successfully", "/api/v1/permissions"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionDto>> getPermissionById(@PathVariable Long id) {
        Optional<PermissionDto> permission = permissionService.getPermissionById(id);
        if (permission.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(permission.get(), "Permission retrieved successfully", "/api/v1/permissions/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Permission not found", "/api/v1/permissions/" + id));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionDto>> createPermission(@Valid @RequestBody PermissionDto permissionDto) {
        try {
            PermissionDto createdPermission = permissionService.createPermission(permissionDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdPermission, "Permission created successfully", "/api/v1/permissions"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/permissions"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionDto>> updatePermission(@PathVariable Long id, 
                                                         @Valid @RequestBody PermissionDto permissionDto) {
        try {
            PermissionDto updatedPermission = permissionService.updatePermission(id, permissionDto);
            return ResponseEntity.ok(ApiResponse.ok(updatedPermission, "Permission updated successfully", "/api/v1/permissions/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/permissions/" + id));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Permission deleted successfully", "/api/v1/permissions/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/permissions/" + id));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activatePermission(@PathVariable Long id) {
        try {
            permissionService.activatePermission(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Permission activated successfully", "/api/v1/permissions/" + id + "/activate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/permissions/" + id + "/activate"));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivatePermission(@PathVariable Long id) {
        try {
            permissionService.deactivatePermission(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Permission deactivated successfully", "/api/v1/permissions/" + id + "/deactivate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/permissions/" + id + "/deactivate"));
        }
    }
}
