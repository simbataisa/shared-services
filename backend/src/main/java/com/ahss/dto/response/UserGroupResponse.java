package com.ahss.dto.response;

import com.ahss.dto.GroupModuleRoleDto;
import java.util.List;

public class UserGroupResponse {
    private Long userGroupId;
    private String name;
    private String description;
    private Integer memberCount;
    private List<GroupModuleRoleDto> roleAssignments;

    public UserGroupResponse() {}

    public UserGroupResponse(Long userGroupId, String name, String description, Integer memberCount) {
        this.userGroupId = userGroupId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
    }

    public UserGroupResponse(Long userGroupId, String name, String description, Integer memberCount, List<GroupModuleRoleDto> roleAssignments) {
        this.userGroupId = userGroupId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.roleAssignments = roleAssignments;
    }

    public Long getUserGroupId() { return userGroupId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getMemberCount() { return memberCount; }
    public List<GroupModuleRoleDto> getRoleAssignments() { return roleAssignments; }
    
    public void setUserGroupId(Long userGroupId) { this.userGroupId = userGroupId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public void setRoleAssignments(List<GroupModuleRoleDto> roleAssignments) { this.roleAssignments = roleAssignments; }
}