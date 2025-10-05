package com.ahss.repository;

import com.ahss.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    @Query("SELECT r FROM Role r WHERE r.module.id = :moduleId AND r.roleStatus = com.ahss.entity.RoleStatus.ACTIVE")
    List<Role> findByModuleId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT r FROM Role r WHERE r.module.id = :moduleId AND r.name = :name AND r.roleStatus = com.ahss.entity.RoleStatus.ACTIVE")
    Optional<Role> findByModuleIdAndName(@Param("moduleId") Long moduleId, @Param("name") String name);
    
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id AND r.roleStatus = com.ahss.entity.RoleStatus.ACTIVE")
    Optional<Role> findWithPermissions(@Param("id") Long id);
    
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.module.id = :moduleId AND r.name = :name AND r.roleStatus = com.ahss.entity.RoleStatus.ACTIVE")
    boolean existsByModuleIdAndName(@Param("moduleId") Long moduleId, @Param("name") String name);
}