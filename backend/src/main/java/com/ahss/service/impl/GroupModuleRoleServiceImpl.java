package com.ahss.service.impl;

import com.ahss.dto.GroupModuleRoleDto;
import com.ahss.entity.GroupModuleRole;
import com.ahss.entity.Module;
import com.ahss.entity.Role;
import com.ahss.entity.UserGroup;
import com.ahss.repository.GroupModuleRoleRepository;
import com.ahss.repository.ModuleRepository;
import com.ahss.repository.RoleRepository;
import com.ahss.repository.UserGroupRepository;
import com.ahss.service.GroupModuleRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupModuleRoleServiceImpl implements GroupModuleRoleService {

    @Autowired
    private GroupModuleRoleRepository groupModuleRoleRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<GroupModuleRoleDto> assignRolesToGroup(Long userGroupId, Long moduleId, List<Long> roleIds) {
        // Validate user group exists
        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new IllegalArgumentException("User group not found with id: " + userGroupId));

        // Validate module exists
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));

        // Verify all roles exist
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }

        List<GroupModuleRole> assignments = new ArrayList<>();
        for (Role role : roles) {
            // Check if assignment already exists
            if (!groupModuleRoleRepository.existsByUserGroupIdAndModuleIdAndRoleId(
                    userGroupId, moduleId, role.getId())) {
                GroupModuleRole assignment = new GroupModuleRole(userGroup, module, role);
                assignments.add(groupModuleRoleRepository.save(assignment));
            }
        }

        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeRolesFromGroup(Long userGroupId, Long moduleId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            groupModuleRoleRepository.deleteByUserGroupIdAndModuleIdAndRoleId(
                    userGroupId, moduleId, roleId);
        }
    }

    @Override
    public void removeAllRolesFromGroupForModule(Long userGroupId, Long moduleId) {
        List<GroupModuleRole> assignments = groupModuleRoleRepository.findByUserGroupId(userGroupId)
                .stream()
                .filter(assignment -> assignment.getModule().getId().equals(moduleId))
                .collect(Collectors.toList());
        
        groupModuleRoleRepository.deleteAll(assignments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupModuleRoleDto> getRoleAssignmentsByUserGroup(Long userGroupId) {
        List<GroupModuleRole> assignments = groupModuleRoleRepository.findByUserGroupIdWithDetails(userGroupId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupModuleRoleDto> getActiveRoleAssignmentsByUserGroup(Long userGroupId) {
        List<GroupModuleRole> assignments = groupModuleRoleRepository.findByUserGroupIdWithActiveRoles(userGroupId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupModuleRoleDto> getRoleAssignmentsByModule(Long moduleId) {
        List<GroupModuleRole> assignments = groupModuleRoleRepository.findByModuleId(moduleId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupModuleRoleDto> getRoleAssignmentsByRole(Long roleId) {
        List<GroupModuleRole> assignments = groupModuleRoleRepository.findByRoleId(roleId);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRoleAssignment(Long userGroupId, Long moduleId, Long roleId) {
        return groupModuleRoleRepository.existsByUserGroupIdAndModuleIdAndRoleId(
                userGroupId, moduleId, roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupModuleRoleDto> getUserGroupsWithRole(Long moduleId, Long roleId) {
        List<GroupModuleRole> assignments = groupModuleRoleRepository.findByRoleId(roleId)
                .stream()
                .filter(assignment -> assignment.getModule().getId().equals(moduleId))
                .collect(Collectors.toList());
        
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private GroupModuleRoleDto convertToDto(GroupModuleRole entity) {
        GroupModuleRoleDto dto = new GroupModuleRoleDto();
        dto.setId(entity.getId());
        dto.setUserGroupId(entity.getUserGroup().getId());
        dto.setUserGroupName(entity.getUserGroup().getName());
        dto.setModuleId(entity.getModule().getId());
        dto.setModuleName(entity.getModule().getName());
        dto.setRoleId(entity.getRole().getId());
        dto.setRoleName(entity.getRole().getName());
        dto.setRoleDescription(entity.getRole().getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }
}