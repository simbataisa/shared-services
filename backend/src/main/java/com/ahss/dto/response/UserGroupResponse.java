package com.ahss.dto.response;

public class UserGroupResponse {
    private Long userGroupId;
    private String name;
    private String description;
    private Integer memberCount;

    public UserGroupResponse() {}

    public UserGroupResponse(Long userGroupId, String name, String description, Integer memberCount) {
        this.userGroupId = userGroupId;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
    }

    public Long getUserGroupId() { return userGroupId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getMemberCount() { return memberCount; }
}