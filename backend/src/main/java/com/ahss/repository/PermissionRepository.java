package com.ahss.repository;

import com.ahss.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    @Query("SELECT p FROM Permission p ORDER BY p.name")
    List<Permission> findAllOrderByName();
    
    @Query("SELECT p FROM Permission p WHERE p.name = :name")
    Optional<Permission> findByName(@Param("name") String name);
    
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.name = :name")
    boolean existsByName(@Param("name") String name);
    
    @Query("SELECT p FROM Permission p WHERE p.id IN :ids")
    List<Permission> findByIds(@Param("ids") List<Long> ids);
}