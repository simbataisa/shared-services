package com.ahss.service;

import com.ahss.dto.GroupModuleRoleDto;
import java.util.List;

public interface GroupModuleRoleService {
    
    /**
     * Assign roles to a user group for a specific module
     */
    List<GroupModuleRoleDto> assignRolesToGroup(Long userGroupId, Long moduleId, List<Long> roleIds);
    
    /**
     * Remove roles from a user group for a specific module
     */
    void removeRolesFromGroup(Long userGroupId, Long moduleId, List<Long> roleIds);
    
    /**
     * Remove all roles from a user group for a specific module
     */
    void removeAllRolesFromGroupForModule(Long userGroupId, Long moduleId);
    
    /**
     * Get all role assignments for a user group
     */
    List<GroupModuleRoleDto> getRoleAssignmentsByUserGroup(Long userGroupId);
    
    /**
     * Get all role assignments for a user group with active roles only
     */
    List<GroupModuleRoleDto> getActiveRoleAssignmentsByUserGroup(Long userGroupId);
    
    /**
     * Get all role assignments for a specific module
     */
    List<GroupModuleRoleDto> getRoleAssignmentsByModule(Long moduleId);
    
    /**
     * Get all role assignments for a specific role
     */
    List<GroupModuleRoleDto> getRoleAssignmentsByRole(Long roleId);
    
    /**
     * Check if a user group has a specific role for a module
     */
    boolean hasRoleAssignment(Long userGroupId, Long moduleId, Long roleId);
    
    /**
     * Get all user groups that have a specific role for a module
     */
    List<GroupModuleRoleDto> getUserGroupsWithRole(Long moduleId, Long roleId);
}