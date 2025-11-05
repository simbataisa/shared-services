package com.ahss.service.impl;

import com.ahss.dto.ModuleDto;
import com.ahss.entity.Module;
import com.ahss.entity.ModuleStatus;
import com.ahss.entity.Product;
import com.ahss.repository.ModuleRepository;
import com.ahss.repository.ProductRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Catalogue")
@Feature("Module Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ModuleServiceImpl.class)
public class ModuleServiceImplTest {

    @MockBean
    private ModuleRepository moduleRepository;
    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ModuleServiceImpl service;

    @Test
    @Story("Return only ACTIVE modules")
    @Severity(SeverityLevel.CRITICAL)
    void getAllActiveModules_filtersInactive() {
        Product prod = Allure.step("Create mock product", () -> mock(Product.class));
        Allure.step("Stub product id to 1L", () -> when(prod.getId()).thenReturn(1L));
        Allure.step("Stub product name to 'Demo'", () -> when(prod.getName()).thenReturn("Demo"));

        Module active = Allure.step("Create mock active module", () -> mock(Module.class));
        Allure.step("Stub module status to ACTIVE",
                () -> when(active.getModuleStatus()).thenReturn(ModuleStatus.ACTIVE));
        Allure.step("Stub module product to mock product", () -> when(active.getProduct()).thenReturn(prod));
        Allure.step("Stub module permissions to empty list",
                () -> when(active.getPermissions()).thenReturn(Collections.emptyList()));

        Module inactive = Allure.step("Create mock inactive module", () -> mock(Module.class));
        Allure.step("Stub inactive module status to INACTIVE",
                () -> when(inactive.getModuleStatus()).thenReturn(ModuleStatus.INACTIVE));

        Allure.step("Stub module repository to return active and inactive modules",
                () -> when(moduleRepository.findAll()).thenReturn(Arrays.asList(active, inactive)));

        Allure.step("Fetch all active modules and ensure filter works");
        List<ModuleDto> result = service.getAllActiveModules();
        Allure.addAttachment("Result count", String.valueOf(result.size()));
        assertEquals(1, result.size());
    }

    @Test
    @Story("List modules by product id")
    @Severity(SeverityLevel.NORMAL)
    void getModulesByProductId_returnsDtos() {
        Product prod = Allure.step("Create mock product", () -> mock(Product.class));
        Allure.step("Stub product id to 10L", () -> when(prod.getId()).thenReturn(10L));
        Allure.step("Stub product name to 'Demo'", () -> when(prod.getName()).thenReturn("Demo"));

        Module m1 = Allure.step("Create mock module 1", () -> mock(Module.class));
        Allure.step("Stub module 1 product to mock product", () -> when(m1.getProduct()).thenReturn(prod));
        Module m2 = Allure.step("Create mock module 2", () -> mock(Module.class));
        Allure.step("Stub module 2 product to mock product", () -> when(m2.getProduct()).thenReturn(prod));

        Allure.step("Stub module repository to return active modules for product 10",
                () -> when(moduleRepository.findActiveByProductId(10L, ModuleStatus.ACTIVE))
                        .thenReturn(Arrays.asList(m1, m2)));

        Allure.step("Fetch modules for product 10");
        List<ModuleDto> result = service.getModulesByProductId(10L);
        Allure.addAttachment("Result count", String.valueOf(result.size()));
        assertEquals(2, result.size());
    }

    @Test
    @Story("Get module by id when ACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void getModuleById_returnsDtoWhenActive() {
        Product prod = Allure.step("Create mock product", () -> mock(Product.class));
        Allure.step("Stub product id to 5L", () -> when(prod.getId()).thenReturn(5L));
        Allure.step("Stub product name to 'Prod'", () -> when(prod.getName()).thenReturn("Prod"));

        Module module = Allure.step("Create mock module", () -> mock(Module.class));
        Allure.step("Stub module id to 5L", () -> when(module.getId()).thenReturn(5L));
        Allure.step("Stub module name to 'Analytics'", () -> when(module.getName()).thenReturn("Analytics"));
        Allure.step("Stub module status to ACTIVE",
                () -> when(module.getModuleStatus()).thenReturn(ModuleStatus.ACTIVE));
        Allure.step("Stub module product to mock product", () -> when(module.getProduct()).thenReturn(prod));
        Allure.step("Stub module permissions to empty list",
                () -> when(module.getPermissions()).thenReturn(Collections.emptyList()));
        Allure.step("Stub module repository to return mock module when id is 5L",
                () -> when(moduleRepository.findById(5L)).thenReturn(Optional.of(module)));

        Allure.step("Fetch ACTIVE module by id 5");
        Optional<ModuleDto> result = service.getModuleById(5L);
        assertTrue(result.isPresent());
        assertEquals("Analytics", result.get().getName());
    }

    @Test
    @Story("Get module by id returns empty when INACTIVE")
    @Severity(SeverityLevel.TRIVIAL)
    void getModuleById_returnsEmptyWhenInactive() {
        Product prod = Allure.step("Create mock product", () -> new Product());
        prod.setId(6L);
        prod.setName("Prod");

        Module module = Allure.step("Create mock module", () -> new Module());
        module.setId(6L);
        module.setName("Reports");
        module.setModuleStatus(ModuleStatus.INACTIVE);
        module.setProduct(prod);

        Allure.step("Stub module repository to return mock module when id is 6L",
                () -> when(moduleRepository.findById(6L)).thenReturn(Optional.of(module)));

        Allure.step("Fetch INACTIVE module by id 6");
        Optional<ModuleDto> result = service.getModuleById(6L);
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Update module name when ACTIVE")
    @Severity(SeverityLevel.CRITICAL)
    void updateModule_changesName_andSaves() {
        Product prod = Allure.step("Create mock product", () -> new Product());
        prod.setId(20L);
        prod.setName("Prod");

        Module existing = Allure.step("Create mock existing module", () -> new Module());
        existing.setId(20L);
        existing.setName("Old");
        existing.setModuleStatus(ModuleStatus.ACTIVE);
        existing.setProduct(prod);

        Allure.step("Stub module repository to return mock existing module when id is 20L",
                () -> when(moduleRepository.findById(20L)).thenReturn(Optional.of(existing)));
        Allure.step("Stub module repository to return false when checking name existence for product 20L",
                () -> when(moduleRepository.existsActiveByProductIdAndName(20L, "New", ModuleStatus.ACTIVE))
                        .thenReturn(false));
        Allure.step("Stub module repository to save module and return it",
                () -> when(moduleRepository.save(any(Module.class))).thenAnswer(inv -> inv.getArgument(0)));

        ModuleDto dto = Allure.step("Create mock module dto", () -> new ModuleDto());
        dto.setName("New");
        dto.setDescription("desc");

        Allure.step("Update module to new name");
        ModuleDto updated = service.updateModule(20L, dto);
        assertEquals("New", updated.getName());
    }

    @Test
    @Story("Prevent updates to INACTIVE modules")
    @Severity(SeverityLevel.CRITICAL)
    void updateModule_throwsWhenInactive() {
        Module existing = Allure.step("Create mock inactive module", () -> new Module());
        existing.setId(55L);
        existing.setModuleStatus(ModuleStatus.INACTIVE);
        Allure.step("Stub module repository to return mock inactive module when id is 55L",
                () -> when(moduleRepository.findById(55L)).thenReturn(Optional.of(existing)));

        Allure.step("Attempt to update an inactive module");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateModule(55L, new ModuleDto()));
        Allure.addAttachment("Exception message", ex.getMessage());
        assertTrue(ex.getMessage().contains("Cannot update inactive module"));
    }

    @Test
    @Story("Check module name existence by product")
    @Severity(SeverityLevel.TRIVIAL)
    void existsByNameAndProductId_returnsTrue() {
        Allure.step("Stub module repository to return true when checking name existence for product 99L",
                () -> when(moduleRepository.existsActiveByProductIdAndName(99L, "Analytics", ModuleStatus.ACTIVE))
                        .thenReturn(true));
        Allure.step("Check if module name 'Analytics' exists for product 99L");
        assertTrue(service.existsByNameAndProductId("Analytics", 99L));
    }

    @Test
    @Story("Activate module")
    @Severity(SeverityLevel.NORMAL)
    void activateModule_setsStatusActive() {
        Module module = Allure.step("Create mock inactive module", () -> new Module());
        module.setId(30L);
        module.setModuleStatus(ModuleStatus.INACTIVE);
        Allure.step("Stub module repository to return mock inactive module when id is 30L",
                () -> when(moduleRepository.findById(30L)).thenReturn(Optional.of(module)));

        Allure.step("Activate module 30");
        service.activateModule(30L);
        Allure.step("Verify module repository saves with ACTIVE status");
        verify(moduleRepository).save(argThat(m -> m.getModuleStatus() == ModuleStatus.ACTIVE));
    }

    @Test
    @Story("Reject creating module for INACTIVE product")
    @Severity(SeverityLevel.CRITICAL)
    void createModule_throwsWhenProductInactive() {
        Product prod = Allure.step("Create mock inactive product", () -> new Product());
        prod.setId(100L);
        prod.setName("Prod");
        prod.setProductStatus(com.ahss.entity.ProductStatus.INACTIVE);
        Allure.step("Stub product repository to return mock inactive product when id is 100L",
                () -> when(productRepository.findById(100L)).thenReturn(Optional.of(prod)));

        ModuleDto dto = Allure.step("Create mock module dto", () -> new ModuleDto());
        dto.setProductId(100L);
        dto.setName("Reports");

        Allure.step("Attempt to create module for inactive product");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createModule(dto));
        Allure.addAttachment("Exception message", ex.getMessage());
        assertTrue(ex.getMessage().contains("inactive product"));
    }
}