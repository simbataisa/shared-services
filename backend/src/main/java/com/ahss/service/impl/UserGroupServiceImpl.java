package com.ahss.service.impl;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.entity.UserGroup;
import com.ahss.repository.UserGroupRepository;
import com.ahss.service.UserGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserGroupServiceImpl implements UserGroupService {
    private final UserGroupRepository repo;

    public UserGroupServiceImpl(UserGroupRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserGroupResponse create(CreateUserGroupRequest request) {
        UserGroup saved = repo.save(new UserGroup(request.getName(), request.getDescription()));
        return new UserGroupResponse(saved.getId(), saved.getName(), saved.getDescription(), 0);
    }

    @Override
    public Page<UserGroupResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(ug -> new UserGroupResponse(ug.getId(), ug.getName(), ug.getDescription(), 0));
    }
}