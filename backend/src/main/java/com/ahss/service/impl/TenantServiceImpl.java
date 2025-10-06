package com.ahss.service.impl;

import com.ahss.dto.TenantDto;
import com.ahss.entity.Tenant;
import com.ahss.entity.TenantStatus;
import com.ahss.entity.TenantType;
import com.ahss.repository.TenantRepository;
import com.ahss.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDto> getTenantsByStatus(TenantStatus status) {
        return tenantRepository.findByStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDto> getTenantsByType(TenantType type) {
        return tenantRepository.findByType(type)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantDto> getTenantById(Long id) {
        return tenantRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantDto> getTenantByCode(String code) {
        return tenantRepository.findByCode(code)
                .map(this::convertToDto);
    }

    @Override
    public TenantDto createTenant(TenantDto tenantDto) {
        if (existsByCode(tenantDto.getCode())) {
            throw new RuntimeException("Tenant with code '" + tenantDto.getCode() + "' already exists");
        }
        
        Tenant tenant = convertToEntity(tenantDto);
        tenant = tenantRepository.save(tenant);
        return convertToDto(tenant);
    }

    @Override
    public TenantDto updateTenant(Long id, TenantDto tenantDto) {
        Tenant existingTenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
        
        if (existsByCodeAndIdNot(tenantDto.getCode(), id)) {
            throw new RuntimeException("Tenant with code '" + tenantDto.getCode() + "' already exists");
        }
        
        existingTenant.setCode(tenantDto.getCode());
        existingTenant.setName(tenantDto.getName());
        existingTenant.setType(tenantDto.getType());
        existingTenant.setOrganizationId(tenantDto.getOrganizationId());
        existingTenant.setStatus(tenantDto.getStatus());
        
        existingTenant = tenantRepository.save(existingTenant);
        return convertToDto(existingTenant);
    }

    @Override
    public void deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) {
            throw new RuntimeException("Tenant not found with id: " + id);
        }
        tenantRepository.deleteById(id);
    }

    @Override
    public void activateTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
        tenant.setStatus(TenantStatus.ACTIVE);
        tenantRepository.save(tenant);
    }

    @Override
    public void deactivateTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
        tenant.setStatus(TenantStatus.INACTIVE);
        tenantRepository.save(tenant);
    }

    @Override
    public void suspendTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return tenantRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return tenantRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDto> searchTenants(String searchTerm) {
        return tenantRepository.searchTenants(searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TenantDto convertToDto(Tenant tenant) {
        TenantDto dto = new TenantDto();
        dto.setId(tenant.getId());
        dto.setCode(tenant.getCode());
        dto.setName(tenant.getName());
        dto.setType(tenant.getType());
        dto.setOrganizationId(tenant.getOrganizationId());
        dto.setStatus(tenant.getStatus());
        dto.setCreatedAt(tenant.getCreatedAt());
        dto.setUpdatedAt(tenant.getUpdatedAt());
        dto.setCreatedBy(tenant.getCreatedBy());
        dto.setUpdatedBy(tenant.getUpdatedBy());
        return dto;
    }

    private Tenant convertToEntity(TenantDto dto) {
        Tenant tenant = new Tenant();
        tenant.setCode(dto.getCode());
        tenant.setName(dto.getName());
        tenant.setType(dto.getType());
        tenant.setOrganizationId(dto.getOrganizationId());
        tenant.setStatus(dto.getStatus() != null ? dto.getStatus() : TenantStatus.ACTIVE);
        return tenant;
    }
}