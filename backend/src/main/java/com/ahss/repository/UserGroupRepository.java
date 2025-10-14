package com.ahss.repository;

import com.ahss.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    @Query("SELECT ug FROM UserGroup ug LEFT JOIN FETCH ug.users")
    Page<UserGroup> findAllWithUsers(Pageable pageable);
    
    @Query("SELECT ug FROM UserGroup ug LEFT JOIN FETCH ug.roles WHERE ug.id = :id")
    Optional<UserGroup> findByIdWithRoles(@Param("id") Long id);
    
    @Query("SELECT ug FROM UserGroup ug LEFT JOIN FETCH ug.users LEFT JOIN FETCH ug.roles WHERE ug.id = :id")
    Optional<UserGroup> findByIdWithUsersAndRoles(@Param("id") Long id);
}