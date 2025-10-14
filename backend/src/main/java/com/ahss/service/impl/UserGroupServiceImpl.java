package com.ahss.service.impl;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.request.UpdateUserGroupRequest;
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
        int memberCount = saved.getUsers() != null ? saved.getUsers().size() : 0;
        return new UserGroupResponse(saved.getId(), saved.getName(), saved.getDescription(), memberCount);
    }

    @Override
    public Page<UserGroupResponse> list(Pageable pageable) {
        return repo.findAllWithUsers(pageable).map(ug -> {
            int memberCount = ug.getUsers() != null ? ug.getUsers().size() : 0;
            return new UserGroupResponse(ug.getId(), ug.getName(), ug.getDescription(), memberCount);
        });
    }

    @Override
    public UserGroupResponse update(Long id, UpdateUserGroupRequest request) {
        UserGroup userGroup = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User group not found with id: " + id));
        
        userGroup.setName(request.getName());
        userGroup.setDescription(request.getDescription());
        
        UserGroup saved = repo.save(userGroup);
        int memberCount = saved.getUsers() != null ? saved.getUsers().size() : 0;
        return new UserGroupResponse(saved.getId(), saved.getName(), saved.getDescription(), memberCount);
    }

    @Override
    public UserGroupResponse getById(Long id) {
        UserGroup userGroup = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User group not found with id: " + id));
        
        int memberCount = userGroup.getUsers() != null ? userGroup.getUsers().size() : 0;
        
        return new UserGroupResponse(userGroup.getId(), userGroup.getName(), userGroup.getDescription(), memberCount);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("User group not found with id: " + id);
        }
        repo.deleteById(id);
    }
}