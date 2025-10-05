package com.ahss.service.impl;

import com.ahss.dto.ProductDto;
import com.ahss.entity.Product;
import com.ahss.repository.ProductRepository;
import com.ahss.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllActiveProducts() {
        return productRepository.findAllActive()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> product.getProductStatus() == com.ahss.entity.ProductStatus.ACTIVE)
                .map(this::convertToDto);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.existsActiveByName(productDto.getName())) {
            throw new IllegalArgumentException("Product with name '" + productDto.getName() + "' already exists");
        }
        
        Product product = convertToEntity(productDto);
        product.setProductStatus(com.ahss.entity.ProductStatus.ACTIVE);
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        if (existingProduct.getProductStatus() != com.ahss.entity.ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot update inactive product");
        }
        
        // Check if name is being changed and if new name already exists
        if (!existingProduct.getName().equals(productDto.getName()) && 
            productRepository.existsActiveByName(productDto.getName())) {
            throw new IllegalArgumentException("Product with name '" + productDto.getName() + "' already exists");
        }
        
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        product.setProductStatus(com.ahss.entity.ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        product.setProductStatus(com.ahss.entity.ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        product.setProductStatus(com.ahss.entity.ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return productRepository.existsActiveByName(name);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setIsActive(product.getProductStatus() == com.ahss.entity.ProductStatus.ACTIVE);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    private Product convertToEntity(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        return product;
    }
}