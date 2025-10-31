package com.ahss.controller;

import com.ahss.dto.ModuleDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ModuleDto>>> getAllModules() {
        List<ModuleDto> modules = moduleService.getAllActiveModules();
        return ResponseEntity.ok(ApiResponse.ok(modules, "Modules retrieved successfully", "/api/v1/modules"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ModuleDto>>> getModulesByProductId(@PathVariable Long productId) {
        List<ModuleDto> modules = moduleService.getModulesByProductId(productId);
        return ResponseEntity.ok(ApiResponse.ok(modules, "Modules retrieved successfully", "/api/v1/modules/product/" + productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleDto>> getModuleById(@PathVariable Long id) {
        Optional<ModuleDto> module = moduleService.getModuleById(id);
        if (module.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(module.get(), "Module retrieved successfully", "/api/v1/modules/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Module not found", "/api/v1/modules/" + id));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ModuleDto>> createModule(@Valid @RequestBody ModuleDto moduleDto) {
        try {
            ModuleDto createdModule = moduleService.createModule(moduleDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdModule, "Module created successfully", "/api/v1/modules"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/modules"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleDto>> updateModule(@PathVariable Long id, 
                                                 @Valid @RequestBody ModuleDto moduleDto) {
        try {
            ModuleDto updatedModule = moduleService.updateModule(id, moduleDto);
            return ResponseEntity.ok(ApiResponse.ok(updatedModule, "Module updated successfully", "/api/v1/modules/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/modules/" + id));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteModule(@PathVariable Long id) {
        try {
            moduleService.deleteModule(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Module deleted successfully", "/api/v1/modules/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/modules/" + id));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateModule(@PathVariable Long id) {
        try {
            moduleService.activateModule(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Module activated successfully", "/api/v1/modules/" + id + "/activate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/modules/" + id + "/activate"));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateModule(@PathVariable Long id) {
        try {
            moduleService.deactivateModule(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Module deactivated successfully", "/api/v1/modules/" + id + "/deactivate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/modules/" + id + "/deactivate"));
        }
    }
}