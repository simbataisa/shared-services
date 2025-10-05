package com.ahss.controller;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.response.ApiResponse;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.service.UserGroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-groups")
public class UserGroupController {
    private final UserGroupService service;

    public UserGroupController(UserGroupService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ApiResponse<UserGroupResponse>> create(@RequestBody CreateUserGroupRequest request) {
        UserGroupResponse resp = service.create(request);
        return ResponseEntity.status(201).body(ApiResponse.ok(resp, "Permission group created successfully", "/api/v1/user-groups"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserGroupResponse>>> list(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Page<UserGroupResponse> resp = service.list(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(resp, "Operation successful", "/api/v1/user-groups"));
    }
}