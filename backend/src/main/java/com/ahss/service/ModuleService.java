package com.ahss.service;

import com.ahss.dto.ModuleDto;
import java.util.List;
import java.util.Optional;

public interface ModuleService {
    
    List<ModuleDto> getAllActiveModules();
    
    List<ModuleDto> getModulesByProductId(Long productId);
    
    Optional<ModuleDto> getModuleById(Long id);
    
    ModuleDto createModule(ModuleDto moduleDto);
    
    ModuleDto updateModule(Long id, ModuleDto moduleDto);
    
    void deleteModule(Long id);
    
    void activateModule(Long id);
    
    void deactivateModule(Long id);
    
    boolean existsByNameAndProductId(String name, Long productId);
}