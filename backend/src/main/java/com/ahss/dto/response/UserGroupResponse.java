package com.ahss.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ahss.entity.UserGroupStatus;

public class UserGroupResponse {
    private Long userGroupId;
    private String name;
    private String description;
    private Integer memberCount;
    
    @JsonProperty("roleCount")
    private Integer roleCount;
    
    private UserGroupStatus userGroupStatus;

    public UserGroupResponse() {}

    public UserGroupResponse(Long userGroupId, String name, String description, Integer memberCount) {
        this.userGroupId = userGroupId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
    }

    public UserGroupResponse(Long userGroupId, String name, String description, Integer memberCount, Integer roleCount) {
        this.userGroupId = userGroupId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.roleCount = roleCount;
    }

    public UserGroupResponse(Long userGroupId, String name, String description, Integer memberCount, Integer roleCount, UserGroupStatus userGroupStatus) {
        this.userGroupId = userGroupId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.roleCount = roleCount;
        this.userGroupStatus = userGroupStatus;
    }

    public Long getUserGroupId() { return userGroupId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getMemberCount() { return memberCount; }
    
    @JsonProperty("roleCount")
    public Integer getRoleCount() { return roleCount; }
    
    public void setUserGroupId(Long userGroupId) { this.userGroupId = userGroupId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    
    @JsonProperty("roleCount")
    public void setRoleCount(Integer roleCount) { this.roleCount = roleCount; }
    
    public UserGroupStatus getUserGroupStatus() { return userGroupStatus; }
    public void setUserGroupStatus(UserGroupStatus userGroupStatus) { this.userGroupStatus = userGroupStatus; }
}