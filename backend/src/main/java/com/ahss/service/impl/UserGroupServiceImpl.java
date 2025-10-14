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
        int roleCount = saved.getRoles() != null ? saved.getRoles().size() : 0;
        return new UserGroupResponse(saved.getId(), saved.getName(), saved.getDescription(), memberCount, roleCount, saved.getUserGroupStatus());
    }

    @Override
    public Page<UserGroupResponse> list(Pageable pageable) {
        return repo.findAllWithUsers(pageable).map(ug -> {
            int memberCount = ug.getUsers() != null ? ug.getUsers().size() : 0;
            int roleCount = ug.getRoles() != null ? ug.getRoles().size() : 0;
            return new UserGroupResponse(ug.getId(), ug.getName(), ug.getDescription(), memberCount, roleCount, ug.getUserGroupStatus());
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
        int roleCount = saved.getRoles() != null ? saved.getRoles().size() : 0;
        return new UserGroupResponse(saved.getId(), saved.getName(), saved.getDescription(), memberCount, roleCount, saved.getUserGroupStatus());
    }

    @Override
    public UserGroupResponse getById(Long id) {
        UserGroup userGroup = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User group not found with id: " + id));
        
        // Fetch users and roles separately to avoid MultipleBagFetchException
        UserGroup userGroupWithUsers = repo.findAllWithUsers(org.springframework.data.domain.PageRequest.of(0, 1))
                .getContent().stream()
                .filter(ug -> ug.getId().equals(id))
                .findFirst()
                .orElse(userGroup);
        
        UserGroup userGroupWithRoles = repo.findByIdWithRoles(id).orElse(userGroup);
        
        int memberCount = userGroupWithUsers.getUsers() != null ? userGroupWithUsers.getUsers().size() : 0;
        int roleCount = userGroupWithRoles.getRoles() != null ? userGroupWithRoles.getRoles().size() : 0;
        
        System.out.println("DEBUG: UserGroup ID: " + userGroup.getId() + 
                          ", memberCount: " + memberCount + 
                          ", roleCount: " + roleCount + 
                          ", roles: " + (userGroupWithRoles.getRoles() != null ? userGroupWithRoles.getRoles().size() : "null"));
        
        return new UserGroupResponse(userGroup.getId(), userGroup.getName(), userGroup.getDescription(), memberCount, roleCount, userGroup.getUserGroupStatus());
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("User group not found with id: " + id);
        }
        repo.deleteById(id);
    }
}