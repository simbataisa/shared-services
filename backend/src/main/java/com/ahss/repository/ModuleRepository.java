package com.ahss.repository;

import com.ahss.entity.Module;
import com.ahss.entity.ModuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    @Query("SELECT m FROM Module m WHERE m.product.id = :productId AND m.moduleStatus = :status")
    List<Module> findActiveByProductId(@Param("productId") Long productId, @Param("status") ModuleStatus status);
    
    @Query("SELECT m FROM Module m WHERE m.product.id = :productId AND m.name = :name AND m.moduleStatus = :status")
    Optional<Module> findActiveByProductIdAndName(@Param("productId") Long productId, @Param("name") String name, @Param("status") ModuleStatus status);
    
    @Query("SELECT COUNT(m) > 0 FROM Module m WHERE m.product.id = :productId AND m.name = :name AND m.moduleStatus = :status")
    boolean existsActiveByProductIdAndName(@Param("productId") Long productId, @Param("name") String name, @Param("status") ModuleStatus status);
}