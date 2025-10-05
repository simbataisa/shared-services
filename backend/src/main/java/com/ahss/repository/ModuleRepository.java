package com.ahss.repository;

import com.ahss.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    @Query("SELECT m FROM Module m WHERE m.product.id = :productId AND m.moduleStatus = com.ahss.entity.ModuleStatus.ACTIVE")
    List<Module> findActiveByProductId(@Param("productId") Long productId);
    
    @Query("SELECT m FROM Module m WHERE m.product.id = :productId AND m.name = :name AND m.moduleStatus = com.ahss.entity.ModuleStatus.ACTIVE")
    Optional<Module> findActiveByProductIdAndName(@Param("productId") Long productId, @Param("name") String name);
    
    @Query("SELECT COUNT(m) > 0 FROM Module m WHERE m.product.id = :productId AND m.name = :name AND m.moduleStatus = com.ahss.entity.ModuleStatus.ACTIVE")
    boolean existsActiveByProductIdAndName(@Param("productId") Long productId, @Param("name") String name);
}