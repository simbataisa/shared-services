package com.ahss.repository;

import com.ahss.entity.Product;
import com.ahss.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.productStatus = :status")
    List<Product> findAllActive(@Param("status") ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.name = :name AND p.productStatus = :status")
    Optional<Product> findActiveByName(@Param("name") String name, @Param("status") ProductStatus status);
    
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name AND p.productStatus = :status")
    boolean existsActiveByName(@Param("name") String name, @Param("status") ProductStatus status);
}