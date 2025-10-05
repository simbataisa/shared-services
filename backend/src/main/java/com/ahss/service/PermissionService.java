package com.ahss.service;

import com.ahss.dto.PermissionDto;
import java.util.List;
import java.util.Optional;

public interface PermissionService {
    
    List<PermissionDto> getAllActivePermissions();
    
    Optional<PermissionDto> getPermissionById(Long id);
    
    PermissionDto createPermission(PermissionDto permissionDto);
    
    PermissionDto updatePermission(Long id, PermissionDto permissionDto);
    
    void deletePermission(Long id);
    
    void activatePermission(Long id);
    
    void deactivatePermission(Long id);
    
    boolean existsByName(String name);
}