package com.ahss.service.impl;

import com.ahss.dto.ProductDto;
import com.ahss.entity.Product;
import com.ahss.entity.ProductStatus;
import com.ahss.repository.ProductRepository;
import com.ahss.service.ModuleService;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Catalogue")
@Feature("Product Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ProductServiceImpl.class)
public class ProductServiceImplTest {

    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private ModuleService moduleService;

    @Autowired
    private ProductServiceImpl service;

    @Test
    @Story("Return only ACTIVE products")
    @Severity(SeverityLevel.CRITICAL)
    void getAllActiveProducts_returnsDtos() {
        Product prod = Allure.step("Create mock active product", () -> mock(Product.class));
        Allure.step("Mock product ID to 1", () -> when(prod.getId()).thenReturn(1L));
        Allure.step("Mock product status to ACTIVE",
                () -> when(prod.getProductStatus()).thenReturn(ProductStatus.ACTIVE));
        Allure.step("Mock product repository to return list of active product",
                () -> when(productRepository.findAllActive(ProductStatus.ACTIVE)).thenReturn(List.of(prod)));
        Allure.step("Mock module service to return empty list of modules for product ID 1",
                () -> when(moduleService.getModulesByProductId(1L)).thenReturn(Collections.emptyList()));

        Allure.step("Fetch all active products and ensure conversion works");
        List<ProductDto> result = service.getAllActiveProducts();
        Allure.addAttachment("Result count", String.valueOf(result.size()));
        assertEquals(1, result.size());
    }

    @Test
    @Story("Create product rejects duplicate name")
    @Severity(SeverityLevel.CRITICAL)
    void createProduct_throwsWhenDuplicateName() {
        Allure.step(
                "Mock product repository to return true when existsActiveByName is called with name Analytics and status ACTIVE",
                () -> when(productRepository.existsActiveByName("Analytics", ProductStatus.ACTIVE)).thenReturn(true));

        ProductDto dto = Allure.step("Create ProductDto with name Analytics and description desc",
                () -> new ProductDto());
        dto.setName("Analytics");
        dto.setDescription("desc");

        IllegalArgumentException ex = Allure.step(
                "Assert that createProduct throws IllegalArgumentException when duplicate name",
                () -> assertThrows(IllegalArgumentException.class, () -> service.createProduct(dto)));
        Allure.addAttachment("Exception message", ex.getMessage());
        Allure.step("Check exception message contains 'already exists'",
                () -> assertTrue(ex.getMessage().contains("already exists")));
    }

    @Test
    @Story("Create product succeeds when name unique")
    @Severity(SeverityLevel.NORMAL)
    void createProduct_savesEntity_withActiveStatus() {
        when(productRepository.existsActiveByName("Billing", ProductStatus.ACTIVE)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(2L);
            p.setProductStatus(ProductStatus.ACTIVE);
            return p;
        });
        when(moduleService.getModulesByProductId(2L)).thenReturn(Collections.emptyList());

        ProductDto dto = new ProductDto();
        dto.setName("Billing");
        dto.setDescription("handles invoices");

        ProductDto created = service.createProduct(dto);
        assertEquals("Billing", created.getName());
        assertEquals(ProductStatus.ACTIVE, created.getProductStatus());
    }

    @Test
    @Story("Update product when ACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void updateProduct_changesName_andSaves() {
        Product existing = Allure.step("Create mock existing product", () -> new Product());
        existing.setId(77L);
        existing.setName("Old");
        existing.setProductStatus(ProductStatus.ACTIVE);

        Allure.step("Mock product repository to return existing product when findById is called with ID 77",
                () -> when(productRepository.findById(77L)).thenReturn(java.util.Optional.of(existing)));
        Allure.step(
                "Mock product repository to return false when existsActiveByName is called with name New and status ACTIVE",
                () -> when(productRepository.existsActiveByName("New", ProductStatus.ACTIVE)).thenReturn(false));
        Allure.step("Mock product repository to save product when save is called",
                () -> when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0)));

        ProductDto dto = Allure.step("Create ProductDto with name New and description desc", () -> new ProductDto());
        Allure.step("Set ProductDto name to New", () -> dto.setName("New"));
        Allure.step("Set ProductDto description to desc", () -> dto.setDescription("desc"));

        ProductDto updated = Allure.step("Update product with ID 77 using ProductDto",
                () -> service.updateProduct(77L, dto));
        Allure.addAttachment("Updated product name", updated.getName());
        Allure.step("Check updated product name is New", () -> assertEquals("New", updated.getName()));
        Allure.step("Check updated product description is desc", () -> assertEquals("desc", updated.getDescription()));
    }

    @Test
    @Story("Update product throws when inactive")
    @Severity(SeverityLevel.CRITICAL)
    void updateProduct_throwsWhenInactive() {
        Product existing = new Product();
        existing.setId(88L);
        existing.setName("Old");
        existing.setProductStatus(ProductStatus.INACTIVE);
        when(productRepository.findById(88L)).thenReturn(java.util.Optional.of(existing));

        ProductDto dto = new ProductDto();
        dto.setName("New");
        dto.setDescription("desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateProduct(88L, dto));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @Story("Update product throws when name duplicates")
    @Severity(SeverityLevel.CRITICAL)
    void updateProduct_throwsWhenDuplicateName() {
        Product existing = new Product();
        existing.setId(89L);
        existing.setName("Old");
        existing.setProductStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(89L)).thenReturn(java.util.Optional.of(existing));
        when(productRepository.existsActiveByName("NewDup", ProductStatus.ACTIVE)).thenReturn(true);

        ProductDto dto = new ProductDto();
        dto.setName("NewDup");
        dto.setDescription("desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateProduct(89L, dto));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Story("Get product by id returns only ACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void getProductById_returnsPresentWhenActive_otherwiseEmpty() {
        Product active = new Product();
        active.setId(3L);
        active.setName("Active");
        active.setProductStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(3L)).thenReturn(java.util.Optional.of(active));
        when(moduleService.getModulesByProductId(3L)).thenReturn(Collections.emptyList());

        assertTrue(service.getProductById(3L).isPresent());

        Product inactive = new Product();
        inactive.setId(4L);
        inactive.setName("Inactive");
        inactive.setProductStatus(ProductStatus.INACTIVE);
        when(productRepository.findById(4L)).thenReturn(java.util.Optional.of(inactive));

        assertTrue(service.getProductById(4L).isEmpty());
    }

    @Test
    @Story("Delete marks product INACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void deleteProduct_marksInactive_andSaves() {
        Product existing = new Product();
        existing.setId(5L);
        existing.setProductStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(5L)).thenReturn(java.util.Optional.of(existing));

        service.deleteProduct(5L);
        verify(productRepository).save(argThat(p -> p.getProductStatus() == ProductStatus.INACTIVE));
    }

    @Test
    @Story("Activate and deactivate product")
    @Severity(SeverityLevel.TRIVIAL)
    void activateAndDeactivate_updatesStatus() {
        Product inactive = new Product();
        inactive.setId(6L);
        inactive.setProductStatus(ProductStatus.INACTIVE);
        when(productRepository.findById(6L)).thenReturn(java.util.Optional.of(inactive));
        service.activateProduct(6L);
        verify(productRepository).save(argThat(p -> p.getProductStatus() == ProductStatus.ACTIVE));

        Product active = new Product();
        active.setId(7L);
        active.setProductStatus(ProductStatus.ACTIVE);
        when(productRepository.findById(7L)).thenReturn(java.util.Optional.of(active));
        service.deactivateProduct(7L);
        verify(productRepository).save(argThat(p -> p.getProductStatus() == ProductStatus.INACTIVE));
    }

    @Test
    @Story("Exists by product name")
    @Severity(SeverityLevel.TRIVIAL)
    void existsByName_returnsTrue() {
        Allure.step(
                "Mock product repository to return true when existsActiveByName is called with name ERP and status ACTIVE",
                () -> when(productRepository.existsActiveByName("ERP", ProductStatus.ACTIVE)).thenReturn(true));
        Allure.step("Check that existsByName returns true for ERP", () -> assertTrue(service.existsByName("ERP")));
    }
}