package com.ahss.controller;

import com.ahss.dto.ProductDto;
import com.ahss.dto.response.ApiResponse;
import com.ahss.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Use fully qualified Swagger annotations to avoid import issues

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Products", description = "Manage products and their lifecycle")
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
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all products", description = "Retrieve all active products")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        List<ProductDto> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(ApiResponse.ok(products, "Products retrieved successfully", "/api/v1/products"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        Optional<ProductDto> product = productService.getProductById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(product.get(), "Product retrieved successfully", "/api/v1/products/" + id));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, "Product not found", "/api/v1/products/" + id));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody ProductDto productDto) {
        try {
            ProductDto createdProduct = productService.createProduct(productDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdProduct, "Product created successfully", "/api/v1/products"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/products"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody ProductDto productDto) {
        try {
            ProductDto updatedProduct = productService.updateProduct(id, productDto);
            return ResponseEntity.ok(ApiResponse.ok(updatedProduct, "Product updated successfully", "/api/v1/products/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/products/" + id));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Product deleted successfully", "/api/v1/products/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/products/" + id));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateProduct(@PathVariable Long id) {
        try {
            productService.activateProduct(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Product activated successfully", "/api/v1/products/" + id + "/activate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/products/" + id + "/activate"));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable Long id) {
        try {
            productService.deactivateProduct(id);
            return ResponseEntity.ok(ApiResponse.ok(null, "Product deactivated successfully", "/api/v1/products/" + id + "/deactivate"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notOk(null, e.getMessage(), "/api/v1/products/" + id + "/deactivate"));
        }
    }
}