package com.ahss.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,
    DEPRECATED;
    
    @JsonCreator
    public static RoleStatus fromString(String value) {
        if (value == null) {
            return ACTIVE; // Default value
        }
        try {
            return RoleStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE; // Default fallback
        }
    }
    
    @JsonValue
    public String toValue() {
        return this.name();
    }
}