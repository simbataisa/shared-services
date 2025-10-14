package com.ahss.service.impl;

import com.ahss.dto.PermissionDto;
import com.ahss.dto.RoleDto;
import com.ahss.entity.Module;
import com.ahss.entity.Permission;
import com.ahss.entity.Role;
import com.ahss.repository.ModuleRepository;
import com.ahss.repository.PermissionRepository;
import com.ahss.repository.RoleRepository;
import com.ahss.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllActiveRoles() {
        return roleRepository.findAll()
                .stream()
                .filter(role -> role.getRoleStatus() != null && role.getRoleStatus().name().equals("ACTIVE"))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDto> getRoleById(Long id) {
        return roleRepository.findWithPermissions(id)
                .filter(role -> role.getRoleStatus() != null && role.getRoleStatus().name().equals("ACTIVE"))
                .map(this::convertToDto);
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDto.getName() + "' already exists");
        }
        
        Role role = convertToEntity(roleDto);
        Role savedRole = roleRepository.save(role);
        return convertToDto(savedRole);
    }

    @Override
    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        if (existingRole.getRoleStatus() == null || !existingRole.getRoleStatus().name().equals("ACTIVE")) {
            throw new IllegalArgumentException("Cannot update inactive role");
        }
        
        // Check if name is being changed and if new name already exists
        if (!existingRole.getName().equals(roleDto.getName()) && 
            roleRepository.existsByName(roleDto.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDto.getName() + "' already exists");
        }
        
        existingRole.setName(roleDto.getName());
        existingRole.setDescription(roleDto.getDescription());
        
        Role updatedRole = roleRepository.save(existingRole);
        return convertToDto(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        role.setRoleStatus(com.ahss.entity.RoleStatus.INACTIVE);
        roleRepository.save(role);
    }

    @Override
    public void activateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        role.setRoleStatus(com.ahss.entity.RoleStatus.ACTIVE);
        roleRepository.save(role);
    }

    @Override
    public void deactivateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        role.setRoleStatus(com.ahss.entity.RoleStatus.INACTIVE);
        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    public RoleDto assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findWithPermissions(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        
        if (role.getRoleStatus() == null || !role.getRoleStatus().name().equals("ACTIVE")) {
            throw new IllegalArgumentException("Cannot assign permissions to inactive role");
        }
        
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("One or more permissions not found");
        }
        
        // Check if all permissions are active - since Permission no longer has active field, skip this check
        // All permissions are considered active by default
        
        // Filter out permissions that are already assigned to avoid duplicates
        List<Permission> newPermissions = permissions.stream()
                .filter(permission -> !role.getPermissions().contains(permission))
                .collect(Collectors.toList());
        
        role.getPermissions().addAll(newPermissions);
        Role updatedRole = roleRepository.save(role);
        return convertToDto(updatedRole);
    }

    @Override
    public RoleDto removePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findWithPermissions(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        
        if (role.getRoleStatus() == null || !role.getRoleStatus().name().equals("ACTIVE")) {
            throw new IllegalArgumentException("Cannot remove permissions from inactive role");
        }
        
        List<Permission> permissionsToRemove = permissionRepository.findAllById(permissionIds);
        role.getPermissions().removeAll(permissionsToRemove);
        
        Role updatedRole = roleRepository.save(role);
        return convertToDto(updatedRole);
    }

    private RoleDto convertToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setRoleStatus(role.getRoleStatus());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        
        // Convert permissions
        if (role.getPermissions() != null) {
            List<PermissionDto> permissionDtos = role.getPermissions().stream()
                    .map(this::convertPermissionToDto)
                    .collect(Collectors.toList());
            dto.setPermissions(permissionDtos);
        }
        
        return dto;
    }

    private PermissionDto convertPermissionToDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setResourceType(permission.getResourceType());
        dto.setAction(permission.getAction());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        
        // Include module information if permission has a module
        if (permission.getModule() != null) {
            dto.setModuleId(permission.getModule().getId());
            dto.setModuleName(permission.getModule().getName());
        }
        
        return dto;
    }

    private Role convertToEntity(RoleDto dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        
        // Set roleStatus from DTO if provided, otherwise default to ACTIVE
        if (dto.getRoleStatus() != null) {
            role.setRoleStatus(dto.getRoleStatus());
        } else {
            role.setRoleStatus(com.ahss.entity.RoleStatus.ACTIVE);
        }
        
        return role;
    }
}