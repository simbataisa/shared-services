package com.ahss.service;

import com.ahss.dto.RoleDto;
import java.util.List;
import java.util.Optional;

public interface RoleService {
    
    List<RoleDto> getAllActiveRoles();
    
    Optional<RoleDto> getRoleById(Long id);
    
    RoleDto createRole(RoleDto roleDto);
    
    RoleDto updateRole(Long id, RoleDto roleDto);
    
    void deleteRole(Long id);
    
    void activateRole(Long id);
    
    void deactivateRole(Long id);
    
    boolean existsByName(String name);
    
    RoleDto assignPermissions(Long roleId, List<Long> permissionIds);
    
    RoleDto removePermissions(Long roleId, List<Long> permissionIds);
}