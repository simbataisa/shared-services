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
// Use fully qualified Swagger annotations to avoid import conflicts
// Use fully qualified Swagger RequestBody in annotations to avoid conflict with Spring's RequestBody

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tenants")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tenants", description = "Manage tenant organizations")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
        content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.ahss.dto.response.ApiResponse.class)))
})
public class TenantController {

    @Autowired
    private TenantService tenantService;

    // Get all tenants
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List tenants", description = "Retrieve all tenants")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenants retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TenantDto>>> getAllTenants() {
        List<TenantDto> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants"));
    }

    // Get tenants by status
    @GetMapping("/status/{status}")
    @io.swagger.v3.oas.annotations.Operation(summary = "List tenants by status", description = "Retrieve tenants filtered by status")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenants retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TenantDto>>> getTenantsByStatus(@PathVariable TenantStatus status) {
        List<TenantDto> tenants = tenantService.getTenantsByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants/status/" + status));
    }

    // Get tenants by type
    @GetMapping("/type/{type}")
    @io.swagger.v3.oas.annotations.Operation(summary = "List tenants by type", description = "Retrieve tenants filtered by type")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenants retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TenantDto>>> getTenantsByType(@PathVariable TenantType type) {
        List<TenantDto> tenants = tenantService.getTenantsByType(type);
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants/type/" + type));
    }

    // Get tenant by ID
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get tenant by ID", description = "Retrieve tenant details by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get tenant by code", description = "Retrieve tenant details by unique code")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Search tenants", description = "Full-text search tenants by name or code")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenants retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TenantDto>>> searchTenants(@RequestParam String query) {
        List<TenantDto> tenants = tenantService.searchTenants(query);
        return ResponseEntity.ok(ApiResponse.ok(tenants, "Tenants retrieved successfully", "/api/v1/tenants/search"));
    }

    // Create tenant
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create tenant", description = "Create a new tenant",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(name = "CreateTenant",
                    value = "{\n  \"name\": \"ACME Corp\",\n  \"code\": \"ACME\",\n  \"type\": \"BUSINESS_IN\",\n  \"status\": \"ACTIVE\"\n}"))))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tenant created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Update tenant", description = "Update an existing tenant by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete tenant", description = "Delete tenant by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTenant(@PathVariable Long id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Tenant deleted successfully", "/api/v1/tenants/" + id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id));
        }
    }

    // Update tenant status
    @PatchMapping("/{id}/status")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update tenant status", description = "Update tenant status to ACTIVE, INACTIVE, or SUSPENDED")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant status updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public ResponseEntity<ApiResponse<Void>> updateTenantStatus(@PathVariable Long id, @RequestBody Map<String, String> statusRequest) {
        try {
            String status = statusRequest.get("status");
            if (status == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.notOk(null, "Status is required", "/api/v1/tenants/" + id + "/status"));
            }
            
            TenantStatus tenantStatus;
            try {
                tenantStatus = TenantStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.notOk(null, "Invalid status: " + status, "/api/v1/tenants/" + id + "/status"));
            }
            
            switch (tenantStatus) {
                case ACTIVE:
                    tenantService.activateTenant(id);
                    break;
                case INACTIVE:
                    tenantService.deactivateTenant(id);
                    break;
                case SUSPENDED:
                    tenantService.suspendTenant(id);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.notOk(null, "Unsupported status: " + status, "/api/v1/tenants/" + id + "/status"));
            }
            
            return ResponseEntity.ok(ApiResponse.ok(null, "Tenant status updated successfully", "/api/v1/tenants/" + id + "/status"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/tenants/" + id + "/status"));
        }
    }

    // Activate tenant
    @PatchMapping("/{id}/activate")
    @io.swagger.v3.oas.annotations.Operation(summary = "Activate tenant", description = "Activate a tenant by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant activated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Deactivate tenant", description = "Deactivate a tenant by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant deactivated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Suspend tenant", description = "Suspend a tenant by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant suspended successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Check tenant code exists", description = "Verify whether a tenant code already exists")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Existence checked successfully")
    })
    public ResponseEntity<ApiResponse<Boolean>> checkTenantCodeExists(@PathVariable String code) {
        boolean exists = tenantService.existsByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(exists, "Tenant code existence checked", "/api/v1/tenants/exists/code/" + code));
    }
}