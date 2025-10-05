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
    private ModuleRepository moduleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllActiveRoles() {
        return roleRepository.findAll()
                .stream()
                .filter(role -> role.getRoleStatus() == com.ahss.entity.RoleStatus.ACTIVE)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getRolesByModuleId(Long moduleId) {
        return roleRepository.findByModuleId(moduleId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDto> getRoleById(Long id) {
        return roleRepository.findWithPermissions(id)
                .filter(role -> role.getRoleStatus() == com.ahss.entity.RoleStatus.ACTIVE)
                .map(this::convertToDto);
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        // Verify module exists and is active
        Module module = moduleRepository.findById(roleDto.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + roleDto.getModuleId()));
        
        if (module.getModuleStatus() != com.ahss.entity.ModuleStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot create role for inactive module");
        }
        
        if (roleRepository.existsByModuleIdAndName(roleDto.getModuleId(), roleDto.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDto.getName() + "' already exists for this module");
        }
        
        Role role = convertToEntity(roleDto, module);
        Role savedRole = roleRepository.save(role);
        return convertToDto(savedRole);
    }

    @Override
    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        
        if (existingRole.getRoleStatus() != com.ahss.entity.RoleStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot update inactive role");
        }
        
        // Check if name is being changed and if new name already exists
        if (!existingRole.getName().equals(roleDto.getName()) && 
            roleRepository.existsByModuleIdAndName(existingRole.getModule().getId(), roleDto.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDto.getName() + "' already exists for this module");
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
    public boolean existsByNameAndModuleId(String name, Long moduleId) {
        return roleRepository.existsByModuleIdAndName(moduleId, name);
    }

    @Override
    public RoleDto assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findWithPermissions(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        
        if (role.getRoleStatus() != com.ahss.entity.RoleStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot assign permissions to inactive role");
        }
        
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("One or more permissions not found");
        }
        
        // Check if all permissions are active - since Permission no longer has active field, skip this check
        // All permissions are considered active by default
        
        role.getPermissions().addAll(permissions);
        Role updatedRole = roleRepository.save(role);
        return convertToDto(updatedRole);
    }

    @Override
    public RoleDto removePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findWithPermissions(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        
        if (role.getRoleStatus() != com.ahss.entity.RoleStatus.ACTIVE) {
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
        dto.setModuleId(role.getModule().getId());
        dto.setModuleName(role.getModule().getName());
        
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
        dto.setIsActive(true); // Since Permission no longer has active field, default to true
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        return dto;
    }

    private Role convertToEntity(RoleDto dto, Module module) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setModule(module);
        
        // Set roleStatus from DTO if provided, otherwise default to ACTIVE
        if (dto.getRoleStatus() != null) {
            role.setRoleStatus(dto.getRoleStatus());
        } else {
            role.setRoleStatus(com.ahss.entity.RoleStatus.ACTIVE);
        }
        
        return role;
    }
}