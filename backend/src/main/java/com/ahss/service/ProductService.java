package com.ahss.service;

import com.ahss.dto.ProductDto;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    List<ProductDto> getAllActiveProducts();
    
    Optional<ProductDto> getProductById(Long id);
    
    ProductDto createProduct(ProductDto productDto);
    
    ProductDto updateProduct(Long id, ProductDto productDto);
    
    void deleteProduct(Long id);
    
    void activateProduct(Long id);
    
    void deactivateProduct(Long id);
    
    boolean existsByName(String name);
}