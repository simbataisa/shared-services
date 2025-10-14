package com.ahss.dto.response;

import com.ahss.entity.RoleStatus;

import java.time.LocalDateTime;

public class RoleResponse {
    
    private Long id;
    private String name;
    private String description;
    private RoleStatus roleStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public RoleResponse() {}

    // Constructor with basic fields
    public RoleResponse(Long id, String name, String description, RoleStatus roleStatus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.roleStatus = roleStatus;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoleStatus getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(RoleStatus roleStatus) {
        this.roleStatus = roleStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}