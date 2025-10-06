package com.ahss.controller;

import com.ahss.dto.GroupModuleRoleDto;
import com.ahss.dto.request.AssignRoleToGroupRequest;
import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.request.UpdateUserGroupRequest;
import com.ahss.dto.response.ApiResponse;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.service.GroupModuleRoleService;
import com.ahss.service.UserGroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-groups")
public class UserGroupController {
    private final UserGroupService service;
    private final GroupModuleRoleService groupModuleRoleService;

    public UserGroupController(UserGroupService service, GroupModuleRoleService groupModuleRoleService) { 
        this.service = service; 
        this.groupModuleRoleService = groupModuleRoleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserGroupResponse>> create(@Valid @RequestBody CreateUserGroupRequest request) {
        UserGroupResponse resp = service.create(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(resp, "Permission group created successfully", "/api/v1/user-groups"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserGroupResponse>>> list(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Page<UserGroupResponse> resp = service.list(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(resp, "Operation successful", "/api/v1/user-groups"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserGroupResponse>> getById(@PathVariable Long id) {
        try {
            UserGroupResponse resp = service.getById(id);
            return ResponseEntity.ok(ApiResponse.ok(resp, "User group retrieved successfully", "/api/v1/user-groups/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.ok(null, e.getMessage(), "/api/v1/user-groups/" + id));
        }
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "User group deleted successfully", "/api/v1/user-groups/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.ok(null, e.getMessage(), "/api/v1/user-groups/" + id));
        }
    }

    // Role assignment endpoints
    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<java.util.List<GroupModuleRoleDto>>> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleToGroupRequest request) {
        try {
            java.util.List<GroupModuleRoleDto> assignments = groupModuleRoleService.assignRolesToGroup(
                    id, request.getModuleId(), request.getRoleIds());
            return ResponseEntity.ok(ApiResponse.ok(assignments, 
                    "Roles assigned to user group successfully", 
                    "/api/v1/user-groups/" + id + "/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.ok(null, e.getMessage(), 
                    "/api/v1/user-groups/" + id + "/roles"));
        }
    }

    @DeleteMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Void>> removeRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleToGroupRequest request) {
        try {
            groupModuleRoleService.removeRolesFromGroup(id, request.getModuleId(), request.getRoleIds());
            return ResponseEntity.ok(ApiResponse.ok(null, 
                    "Roles removed from user group successfully", 
                    "/api/v1/user-groups/" + id + "/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.ok(null, e.getMessage(), 
                    "/api/v1/user-groups/" + id + "/roles"));
        }
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<java.util.List<GroupModuleRoleDto>>> getRoleAssignments(@PathVariable Long id) {
        try {
            java.util.List<GroupModuleRoleDto> assignments = groupModuleRoleService.getActiveRoleAssignmentsByUserGroup(id);
            return ResponseEntity.ok(ApiResponse.ok(assignments, 
                    "Role assignments retrieved successfully", 
                    "/api/v1/user-groups/" + id + "/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.ok(null, e.getMessage(), 
                    "/api/v1/user-groups/" + id + "/roles"));
        }
    }

    @DeleteMapping("/{id}/modules/{moduleId}/roles")
    public ResponseEntity<ApiResponse<Void>> removeAllRolesForModule(
            @PathVariable Long id,
            @PathVariable Long moduleId) {
        try {
            groupModuleRoleService.removeAllRolesFromGroupForModule(id, moduleId);
            return ResponseEntity.ok(ApiResponse.ok(null, 
                    "All roles removed from user group for module successfully", 
                    "/api/v1/user-groups/" + id + "/modules/" + moduleId + "/roles"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.ok(null, e.getMessage(), 
                    "/api/v1/user-groups/" + id + "/modules/" + moduleId + "/roles"));
        }
    }
}