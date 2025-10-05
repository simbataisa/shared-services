package com.ahss.service.impl;

import com.ahss.dto.ModuleDto;
import com.ahss.entity.Module;
import com.ahss.entity.Product;
import com.ahss.repository.ModuleRepository;
import com.ahss.repository.ProductRepository;
import com.ahss.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDto> getAllActiveModules() {
        return moduleRepository.findAll()
                .stream()
                .filter(module -> module.getModuleStatus() == com.ahss.entity.ModuleStatus.ACTIVE)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDto> getModulesByProductId(Long productId) {
        return moduleRepository.findActiveByProductId(productId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ModuleDto> getModuleById(Long id) {
        return moduleRepository.findById(id)
                .filter(module -> module.getModuleStatus() == com.ahss.entity.ModuleStatus.ACTIVE)
                .map(this::convertToDto);
    }

    @Override
    public ModuleDto createModule(ModuleDto moduleDto) {
        // Verify product exists and is active
        Product product = productRepository.findById(moduleDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + moduleDto.getProductId()));
        
        if (product.getProductStatus() != com.ahss.entity.ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot create module for inactive product");
        }
        
        if (moduleRepository.existsActiveByProductIdAndName(moduleDto.getProductId(), moduleDto.getName())) {
            throw new IllegalArgumentException("Module with name '" + moduleDto.getName() + "' already exists for this product");
        }
        
        Module module = convertToEntity(moduleDto, product);
        module.setModuleStatus(com.ahss.entity.ModuleStatus.ACTIVE);
        Module savedModule = moduleRepository.save(module);
        return convertToDto(savedModule);
    }

    @Override
    public ModuleDto updateModule(Long id, ModuleDto moduleDto) {
        Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + id));
        
        if (existingModule.getModuleStatus() != com.ahss.entity.ModuleStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot update inactive module");
        }
        
        // Check if name is being changed and if new name already exists
        if (!existingModule.getName().equals(moduleDto.getName()) && 
            moduleRepository.existsActiveByProductIdAndName(existingModule.getProduct().getId(), moduleDto.getName())) {
            throw new IllegalArgumentException("Module with name '" + moduleDto.getName() + "' already exists for this product");
        }
        
        existingModule.setName(moduleDto.getName());
        existingModule.setDescription(moduleDto.getDescription());
        
        Module updatedModule = moduleRepository.save(existingModule);
        return convertToDto(updatedModule);
    }

    @Override
    public void deleteModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + id));
        
        module.setModuleStatus(com.ahss.entity.ModuleStatus.INACTIVE);
        moduleRepository.save(module);
    }

    @Override
    public void activateModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + id));
        
        module.setModuleStatus(com.ahss.entity.ModuleStatus.ACTIVE);
        moduleRepository.save(module);
    }

    @Override
    public void deactivateModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + id));
        
        module.setModuleStatus(com.ahss.entity.ModuleStatus.INACTIVE);
        moduleRepository.save(module);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndProductId(String name, Long productId) {
        return moduleRepository.existsActiveByProductIdAndName(productId, name);
    }

    private ModuleDto convertToDto(Module module) {
        ModuleDto dto = new ModuleDto();
        dto.setId(module.getId());
        dto.setName(module.getName());
        dto.setDescription(module.getDescription());
        dto.setIsActive(module.getModuleStatus() == com.ahss.entity.ModuleStatus.ACTIVE);
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        dto.setProductId(module.getProduct().getId());
        dto.setProductName(module.getProduct().getName());
        return dto;
    }

    private Module convertToEntity(ModuleDto dto, Product product) {
        Module module = new Module();
        module.setName(dto.getName());
        module.setDescription(dto.getDescription());
        module.setProduct(product);
        return module;
    }
}