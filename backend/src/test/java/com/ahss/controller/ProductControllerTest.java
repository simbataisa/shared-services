package com.ahss.controller;

import com.ahss.dto.ProductDto;
import com.ahss.entity.ProductStatus;
import com.ahss.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("Catalogue")
@Feature("Product")
@Owner("backend")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductDto product(long id, String name) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("desc");
        dto.setCode("PRD-" + id);
        dto.setVersion("1.0.0");
        dto.setProductStatus(ProductStatus.ACTIVE);
        return dto;
    }

    @Test
    @Story("List all active products returns 200 and payload")
    @Severity(SeverityLevel.NORMAL)
    void get_all_products_returns_200_with_list() throws Exception {
        Allure.step("Stub service to return 2 active products", () ->
                when(productService.getAllActiveProducts())
                        .thenReturn(List.of(product(1L, "Payments"), product(2L, "Refunds"))))
        ;

        var result = Allure.step("GET /api/v1/products", () ->
                mockMvc.perform(get("/api/v1/products"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Products retrieved successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products")))
                        .andExpect(jsonPath("$.data", hasSize(2)))
                        .andExpect(jsonPath("$.data[0].name", is("Payments")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get product by ID returns 200 when found")
    @Severity(SeverityLevel.NORMAL)
    void get_product_by_id_found_returns_200() throws Exception {
        Allure.step("Stub service to return product id=1", () ->
                when(productService.getProductById(1L)).thenReturn(Optional.of(product(1L, "Payments")))
        );

        var result = Allure.step("GET /api/v1/products/1", () ->
                mockMvc.perform(get("/api/v1/products/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.data.id", is(1)))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/1")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Get product by ID returns 404 when missing")
    @Severity(SeverityLevel.MINOR)
    void get_product_by_id_not_found_returns_404() throws Exception {
        Allure.step("Stub service to return empty for id=99", () ->
                when(productService.getProductById(99L)).thenReturn(Optional.empty())
        );

        var result = Allure.step("GET /api/v1/products/99", () ->
                mockMvc.perform(get("/api/v1/products/99"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", is("Product not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create product returns 201 when valid")
    @Severity(SeverityLevel.NORMAL)
    void create_product_success_returns_201() throws Exception {
        ProductDto created = product(5L, "Billing");
        Allure.step("Stub service to create product", () ->
                when(productService.createProduct(any(ProductDto.class))).thenReturn(created)
        );

        ProductDto req = new ProductDto();
        req.setName("Billing");
        req.setDescription("billing product");
        req.setCode("BILL");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("POST /api/v1/products", () ->
                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Product created successfully")))
                        .andExpect(jsonPath("$.data.id", is(5)))
                        .andExpect(jsonPath("$.path", is("/api/v1/products")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Create product returns 400 for duplicate name or invalid")
    @Severity(SeverityLevel.MINOR)
    void create_product_bad_request_returns_400() throws Exception {
        Allure.step("Stub service to throw duplicate name error", () ->
                when(productService.createProduct(any(ProductDto.class))).thenThrow(new IllegalArgumentException("Product name already exists"))
        );

        ProductDto req = new ProductDto();
        req.setName("Billing");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("POST /api/v1/products (bad request)", () ->
                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("already exists")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update product returns 200 when valid")
    @Severity(SeverityLevel.NORMAL)
    void update_product_success_returns_200() throws Exception {
        ProductDto updated = product(5L, "Billing-Updated");
        Allure.step("Stub service to update product id=5", () ->
                when(productService.updateProduct(eq(5L), any(ProductDto.class))).thenReturn(updated)
        );

        ProductDto req = new ProductDto();
        req.setName("Billing-Updated");
        req.setDescription("updated");
        req.setCode("BILL");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("PUT /api/v1/products/5", () ->
                mockMvc.perform(put("/api/v1/products/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Product updated successfully")))
                        .andExpect(jsonPath("$.data.name", is("Billing-Updated")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/5")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Update product returns 404 when ID missing")
    @Severity(SeverityLevel.MINOR)
    void update_product_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for id=99", () ->
                when(productService.updateProduct(eq(99L), any(ProductDto.class))).thenThrow(new IllegalArgumentException("Product not found with id: 99"))
        );

        ProductDto req = new ProductDto();
        req.setName("X");
        String body = objectMapper.writeValueAsString(req);
        Allure.addAttachment("Request Body", MediaType.APPLICATION_JSON_VALUE, body);

        var result = Allure.step("PUT /api/v1/products/99 (not found)", () ->
                mockMvc.perform(put("/api/v1/products/99")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Product not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/99")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete product returns 200 when deletion succeeds")
    @Severity(SeverityLevel.NORMAL)
    void delete_product_success_returns_200() throws Exception {
        Allure.step("Stub service to delete product id=7", () ->
                doNothing().when(productService).deleteProduct(7L)
        );

        var result = Allure.step("DELETE /api/v1/products/7", () ->
                mockMvc.perform(delete("/api/v1/products/7"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Product deleted successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/7")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Delete product returns 404 when ID missing")
    @Severity(SeverityLevel.MINOR)
    void delete_product_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for delete id=77", () ->
                doThrow(new IllegalArgumentException("Product not found with id: 77")).when(productService).deleteProduct(77L)
        );

        var result = Allure.step("DELETE /api/v1/products/77 (not found)", () ->
                mockMvc.perform(delete("/api/v1/products/77"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Product not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/77")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Activate product returns 200 when activation succeeds")
    @Severity(SeverityLevel.NORMAL)
    void activate_product_success_returns_200() throws Exception {
        Allure.step("Stub service to activate product id=5", () ->
                doNothing().when(productService).activateProduct(5L)
        );

        var result = Allure.step("PUT /api/v1/products/5/activate", () ->
                mockMvc.perform(put("/api/v1/products/5/activate"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Product activated successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/5/activate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Deactivate product returns 200 when deactivation succeeds")
    @Severity(SeverityLevel.NORMAL)
    void deactivate_product_success_returns_200() throws Exception {
        Allure.step("Stub service to deactivate product id=5", () ->
                doNothing().when(productService).deactivateProduct(5L)
        );

        var result = Allure.step("PUT /api/v1/products/5/deactivate", () ->
                mockMvc.perform(put("/api/v1/products/5/deactivate"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success", is(true)))
                        .andExpect(jsonPath("$.message", is("Product deactivated successfully")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/5/deactivate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Activate product returns 404 when ID missing")
    @Severity(SeverityLevel.MINOR)
    void activate_product_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for activate id=55", () ->
                doThrow(new IllegalArgumentException("Product not found with id: 55")).when(productService).activateProduct(55L)
        );

        var result = Allure.step("PUT /api/v1/products/55/activate (not found)", () ->
                mockMvc.perform(put("/api/v1/products/55/activate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Product not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/55/activate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }

    @Test
    @Story("Deactivate product returns 404 when ID missing")
    @Severity(SeverityLevel.MINOR)
    void deactivate_product_not_found_returns_404() throws Exception {
        Allure.step("Stub service to throw not found for deactivate id=56", () ->
                doThrow(new IllegalArgumentException("Product not found with id: 56")).when(productService).deactivateProduct(56L)
        );

        var result = Allure.step("PUT /api/v1/products/56/deactivate (not found)", () ->
                mockMvc.perform(put("/api/v1/products/56/deactivate"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.success", is(false)))
                        .andExpect(jsonPath("$.message", containsString("Product not found")))
                        .andExpect(jsonPath("$.path", is("/api/v1/products/56/deactivate")))
                        .andReturn()
        );
        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentAsString());
    }
}