package com.ahss.controller;

import com.ahss.dto.RoleDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Use fully qualified Swagger annotations to avoid name conflicts with com.ahss.dto.response.ApiResponse

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/roles")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Roles", description = "Manage roles within modules")
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
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all roles", description = "Retrieve all active roles")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllActiveRoles();
        return ResponseEntity.ok(ApiResponse.ok(roles, "Roles retrieved successfully", "/api/v1/roles"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(@PathVariable Long id) {
        Optional<RoleDto> role = roleService.getRoleById(id);
        if (role.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(role.get(), "Role retrieved successfully", "/api/v1/roles/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Role not found", "/api/v1/roles/" + id));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@Valid @RequestBody RoleDto roleDto) {
        try {
            RoleDto createdRole = roleService.createRole(roleDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdRole, "Role created successfully", "/api/v1/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@PathVariable Long id, 
                                             @Valid @RequestBody RoleDto roleDto) {
        try {
            RoleDto updatedRole = roleService.updateRole(id, roleDto);
            return ResponseEntity.ok(ApiResponse.ok(updatedRole, "Role updated successfully", "/api/v1/roles/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles/" + id));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Role deleted successfully", "/api/v1/roles/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles/" + id));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateRole(@PathVariable Long id) {
        try {
            roleService.activateRole(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Role activated successfully", "/api/v1/roles/" + id + "/activate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles/" + id + "/activate"));
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateRole(@PathVariable Long id) {
        try {
            roleService.deactivateRole(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Role deactivated successfully", "/api/v1/roles/" + id + "/deactivate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles/" + id + "/deactivate"));
        }
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleDto>> assignPermissions(@PathVariable Long id, 
                                                    @RequestBody List<Long> permissionIds) {
        try {
            RoleDto updatedRole = roleService.assignPermissions(id, permissionIds);
            return ResponseEntity.ok(ApiResponse.ok(updatedRole, "Permissions assigned successfully", "/api/v1/roles/" + id + "/permissions"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles/" + id + "/permissions"));
        }
    }

    @DeleteMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleDto>> removePermissions(@PathVariable Long id, 
                                                    @RequestBody List<Long> permissionIds) {
        try {
            RoleDto updatedRole = roleService.removePermissions(id, permissionIds);
            return ResponseEntity.ok(ApiResponse.ok(updatedRole, "Permissions removed successfully", "/api/v1/roles/" + id + "/permissions"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/roles/" + id + "/permissions"));
        }
    }
}