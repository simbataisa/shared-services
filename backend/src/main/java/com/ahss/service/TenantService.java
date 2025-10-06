package com.ahss.service;

import com.ahss.dto.TenantDto;
import com.ahss.entity.TenantStatus;
import com.ahss.entity.TenantType;
import java.util.List;
import java.util.Optional;

public interface TenantService {
    
    List<TenantDto> getAllTenants();
    
    List<TenantDto> getTenantsByStatus(TenantStatus status);
    
    List<TenantDto> getTenantsByType(TenantType type);
    
    Optional<TenantDto> getTenantById(Long id);
    
    Optional<TenantDto> getTenantByCode(String code);
    
    TenantDto createTenant(TenantDto tenantDto);
    
    TenantDto updateTenant(Long id, TenantDto tenantDto);
    
    void deleteTenant(Long id);
    
    void activateTenant(Long id);
    
    void deactivateTenant(Long id);
    
    void suspendTenant(Long id);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);
    
    List<TenantDto> searchTenants(String searchTerm);
}