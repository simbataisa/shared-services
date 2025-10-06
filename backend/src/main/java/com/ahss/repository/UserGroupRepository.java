package com.ahss.repository;

import com.ahss.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    @Query("SELECT ug FROM UserGroup ug LEFT JOIN FETCH ug.users")
    Page<UserGroup> findAllWithUsers(Pageable pageable);
}