package com.ahss.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssignRoleToGroupRequest {
    @NotNull(message = "Module ID is required")
    private Long moduleId;
    
    @NotNull(message = "Role IDs are required")
    private List<Long> roleIds;

    // Constructors
    public AssignRoleToGroupRequest() {}

    public AssignRoleToGroupRequest(Long moduleId, List<Long> roleIds) {
        this.moduleId = moduleId;
        this.roleIds = roleIds;
    }

    // Getters and Setters
    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}