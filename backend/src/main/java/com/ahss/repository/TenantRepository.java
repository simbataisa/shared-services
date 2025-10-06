package com.ahss.repository;

import com.ahss.entity.Tenant;
import com.ahss.entity.TenantStatus;
import com.ahss.entity.TenantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    @Query("SELECT t FROM Tenant t WHERE t.code = :code")
    Optional<Tenant> findByCode(@Param("code") String code);
    
    @Query("SELECT t FROM Tenant t WHERE t.status = :status")
    List<Tenant> findByStatus(@Param("status") TenantStatus status);
    
    @Query("SELECT t FROM Tenant t WHERE t.status = com.ahss.entity.TenantStatus.ACTIVE")
    List<Tenant> findAllActive();
    
    @Query("SELECT t FROM Tenant t WHERE t.type = :type")
    List<Tenant> findByType(@Param("type") TenantType type);
    
    @Query("SELECT COUNT(t) > 0 FROM Tenant t WHERE t.code = :code")
    boolean existsByCode(@Param("code") String code);
    
    @Query("SELECT COUNT(t) > 0 FROM Tenant t WHERE t.code = :code AND t.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);
    
    @Query("SELECT t FROM Tenant t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tenant> searchTenants(@Param("searchTerm") String searchTerm);
}