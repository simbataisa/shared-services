package com.ahss.service.impl;

import com.ahss.dto.PermissionDto;
import com.ahss.entity.Permission;
import com.ahss.repository.PermissionRepository;
import com.ahss.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllActivePermissions() {
        return permissionRepository.findAllOrderByName()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissionDto> getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public PermissionDto createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsByName(permissionDto.getName())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDto.getName() + "' already exists");
        }
        
        Permission permission = convertToEntity(permissionDto);
        Permission savedPermission = permissionRepository.save(permission);
        return convertToDto(savedPermission);
    }

    @Override
    public PermissionDto updatePermission(Long id, PermissionDto permissionDto) {
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!existingPermission.getName().equals(permissionDto.getName()) && 
            permissionRepository.existsByName(permissionDto.getName())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDto.getName() + "' already exists");
        }
        
        existingPermission.setName(permissionDto.getName());
        existingPermission.setDescription(permissionDto.getDescription());
        
        Permission updatedPermission = permissionRepository.save(existingPermission);
        return convertToDto(updatedPermission);
    }

    @Override
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));
        
        permissionRepository.delete(permission);
    }

    @Override
    public void activatePermission(Long id) {
        // Since there's no active field, this method is no longer needed
        throw new UnsupportedOperationException("Permission activation is not supported");
    }

    @Override
    public void deactivatePermission(Long id) {
        // Since there's no active field, this method is no longer needed
        throw new UnsupportedOperationException("Permission deactivation is not supported");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    private PermissionDto convertToDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setIsActive(true); // Since there's no active field, assume all permissions are active
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        
        // Set module information if available
        if (permission.getModule() != null) {
            dto.setModuleId(permission.getModule().getId());
            dto.setModuleName(permission.getModule().getName());
        }
        
        return dto;
    }

    private Permission convertToEntity(PermissionDto dto) {
        Permission permission = new Permission();
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        return permission;
    }
}