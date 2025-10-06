package com.ahss.controller;

import com.ahss.dto.TenantDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.entity.TenantStatus;
import com.ahss.entity.TenantType;
import com.ahss.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenants")
@CrossOrigin(origins = "http://localhost:5173")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    // Get all tenants
    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantDto>>> getAllTenants() {
        List<TenantDto> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants"));
    }

    // Get tenants by status
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TenantDto>>> getTenantsByStatus(@PathVariable TenantStatus status) {
        List<TenantDto> tenants = tenantService.getTenantsByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants/status/" + status));
    }

    // Get tenants by type
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<TenantDto>>> getTenantsByType(@PathVariable TenantType type) {
        List<TenantDto> tenants = tenantService.getTenantsByType(type);
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants/type/" + type));
    }

    // Get tenant by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantDto>> getTenantById(@PathVariable Long id) {
        Optional<TenantDto> tenant = tenantService.getTenantById(id);
        if (tenant.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(tenant.get(), "Tenant retrieved successfully", "/api/v1/tenants/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, "Tenant not found", "/api/v1/tenants/" + id));
        }
    }

    // Get tenant by code
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<TenantDto>> getTenantByCode(@PathVariable String code) {
        Optional<TenantDto> tenant = tenantService.getTenantByCode(code);
        if (tenant.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(tenant.get(), "Tenant retrieved successfully", "/api/v1/tenants/code/" + code));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ok(null, "Tenant not found", "/api/v1/tenants/code/" + code));
        }
    }

    // Search tenants
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TenantDto>>> searchTenants(@RequestParam String query) {
        List<TenantDto> tenants = tenantService.searchTenants(query);
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants/search"));
    }

    // Create tenant
    @PostMapping
    public ResponseEntity<ApiResponse<TenantDto>> createTenant(@Valid @RequestBody TenantDto tenantDto) {
        try {
            TenantDto createdTenant = tenantService.createTenant(tenantDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdTenant, "Tenant created successfully", "/api/v1/tenants"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants"));
        }
    }

    // Update tenant
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantDto>> updateTenant(@PathVariable Long id, 
                                                              @Valid @RequestBody TenantDto tenantDto) {
        try {
            TenantDto updatedTenant = tenantService.updateTenant(id, tenantDto);
            return ResponseEntity.ok(ApiResponse.ok(updatedTenant, "Tenant updated successfully", "/api/v1/tenants/" + id));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id));
            }
        }
    }

    // Delete tenant
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable Long id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Tenant deleted successfully", "/api/v1/tenants/" + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id));
        }
    }

    // Activate tenant
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateTenant(@PathVariable Long id) {
        try {
            tenantService.activateTenant(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Tenant activated successfully", "/api/v1/tenants/" + id + "/activate"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id + "/activate"));
        }
    }

    // Deactivate tenant
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateTenant(@PathVariable Long id) {
        try {
            tenantService.deactivateTenant(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Tenant deactivated successfully", "/api/v1/tenants/" + id + "/deactivate"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id + "/deactivate"));
        }
    }

    // Suspend tenant
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendTenant(@PathVariable Long id) {
        try {
            tenantService.suspendTenant(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Tenant suspended successfully", "/api/v1/tenants/" + id + "/suspend"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id + "/suspend"));
        }
    }

    // Check if tenant code exists
    @GetMapping("/exists/code/{code}")
    public ResponseEntity<ApiResponse<Boolean>> checkTenantCodeExists(@PathVariable String code) {
        boolean exists = tenantService.existsByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(exists, "Tenant code existence checked", "/api/v1/tenants/exists/code/" + code));
    }
}