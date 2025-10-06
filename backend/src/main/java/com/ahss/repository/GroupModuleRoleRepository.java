package com.ahss.repository;

import com.ahss.entity.GroupModuleRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupModuleRoleRepository extends JpaRepository<GroupModuleRole, Long> {
    
    /**
     * Find all role assignments for a specific user group
     */
    List<GroupModuleRole> findByUserGroupId(Long userGroupId);
    
    /**
     * Find all role assignments for a specific module
     */
    List<GroupModuleRole> findByModuleId(Long moduleId);
    
    /**
     * Find all role assignments for a specific role
     */
    List<GroupModuleRole> findByRoleId(Long roleId);
    
    /**
     * Find a specific assignment by user group, module, and role
     */
    Optional<GroupModuleRole> findByUserGroupIdAndModuleIdAndRoleId(
        Long userGroupId, Long moduleId, Long roleId);
    
    /**
     * Check if a specific assignment exists
     */
    boolean existsByUserGroupIdAndModuleIdAndRoleId(
        Long userGroupId, Long moduleId, Long roleId);
    
    /**
     * Delete all assignments for a specific user group
     */
    void deleteByUserGroupId(Long userGroupId);
    
    /**
     * Delete all assignments for a specific module
     */
    void deleteByModuleId(Long moduleId);
    
    /**
     * Delete all assignments for a specific role
     */
    void deleteByRoleId(Long roleId);
    
    /**
     * Delete a specific assignment
     */
    void deleteByUserGroupIdAndModuleIdAndRoleId(
        Long userGroupId, Long moduleId, Long roleId);
    
    /**
     * Find all role assignments with user group, module, and role details
     */
    @Query("SELECT gmr FROM GroupModuleRole gmr " +
           "JOIN FETCH gmr.userGroup " +
           "JOIN FETCH gmr.module " +
           "JOIN FETCH gmr.role " +
           "WHERE gmr.userGroup.id = :userGroupId")
    List<GroupModuleRole> findByUserGroupIdWithDetails(@Param("userGroupId") Long userGroupId);
    
    /**
     * Find all role assignments for a user group with active roles only
     */
    @Query("SELECT gmr FROM GroupModuleRole gmr " +
           "JOIN FETCH gmr.userGroup " +
           "JOIN FETCH gmr.module " +
           "JOIN FETCH gmr.role r " +
           "WHERE gmr.userGroup.id = :userGroupId " +
           "AND r.roleStatus = 'ACTIVE'")
    List<GroupModuleRole> findByUserGroupIdWithActiveRoles(@Param("userGroupId") Long userGroupId);
}