package com.ahss.service;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.request.UpdateUserGroupRequest;
import com.ahss.dto.response.UserGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserGroupService {
    UserGroupResponse create(CreateUserGroupRequest request);
    Page<UserGroupResponse> list(Pageable pageable);
    UserGroupResponse update(Long id, UpdateUserGroupRequest request);
    UserGroupResponse getById(Long id);
    void delete(Long id);
}