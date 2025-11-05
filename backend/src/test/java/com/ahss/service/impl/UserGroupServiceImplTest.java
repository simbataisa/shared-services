package com.ahss.service.impl;

import com.ahss.dto.request.CreateUserGroupRequest;
import com.ahss.dto.request.UpdateUserGroupRequest;
import com.ahss.dto.response.UserGroupResponse;
import com.ahss.entity.Role;
import com.ahss.entity.User;
import com.ahss.entity.UserGroup;
import com.ahss.repository.UserGroupRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("IAM")
@Feature("User Group Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserGroupServiceImpl.class)
public class UserGroupServiceImplTest {

    @MockBean
    private UserGroupRepository repo;

    @Autowired
    private UserGroupServiceImpl service;

    @Test
    @Story("Create user group and compute counts")
    @Severity(SeverityLevel.CRITICAL)
    void create_returnsResponseWithCounts() {
        UserGroup saved = Allure.step("Create user group entity", () -> new UserGroup("Team A", "Core team"));
        saved.setId(10L);
        saved.setUsers(List.of());
        saved.setRoles(List.of());
        Allure.step("Mock user group repository to return saved entity",
                () -> when(repo.save(any(UserGroup.class))).thenReturn(saved));

        CreateUserGroupRequest req = Allure.step("Create user group request", () -> new CreateUserGroupRequest());
        req.setName("Team A");
        req.setDescription("Core team");

        Allure.step("Create user group and check member/role counts");
        UserGroupResponse resp = Allure.step("Create user group and return response", () -> service.create(req));
        Allure.addAttachment("UserGroup id", String.valueOf(resp.getUserGroupId()));
        Allure.step("Verify user group ID is 10", () -> assertEquals(10L, resp.getUserGroupId()));
        Allure.step("Verify member count is 0", () -> assertEquals(0, resp.getMemberCount()));
        Allure.step("Verify role count is 0", () -> assertEquals(0, resp.getRoleCount()));
    }

    @Test
    @Story("List user groups with computed counts")
    @Severity(SeverityLevel.NORMAL)
    void list_returnsPageWithCounts() {
        UserGroup ug = Allure.step("Create user group entity", () -> new UserGroup("Team B", "Ops"));
        ug.setId(11L);
        ug.setUsers(List.of(new User(), new User()));
        ug.setRoles(List.of(new Role()));

        Page<UserGroup> page = new PageImpl<>(List.of(ug));
        Allure.step("Mock user group repository to return page with user group",
                () -> when(repo.findAllWithUsers(any())).thenReturn(page));

        Allure.step("List user groups and confirm counts are mapped");
        Page<UserGroupResponse> respPage = Allure.step("List user groups and return response page",
                () -> service.list(PageRequest.of(0, 10)));
        UserGroupResponse first = respPage.getContent().get(0);
        Allure.step("Verify member count is 2", () -> assertEquals(2, first.getMemberCount()));
        Allure.step("Verify role count is 1", () -> assertEquals(1, first.getRoleCount()));
    }

    @Test
    @Story("Get by id computes counts via separate fetches")
    @Severity(SeverityLevel.NORMAL)
    void getById_computesCountsFromSeparateFetches() {
        UserGroup base = Allure.step("Create user group entity", () -> new UserGroup("Team C", "desc"));
        base.setId(21L);
        Allure.step("Mock user group repository to return base entity",
                () -> when(repo.findById(21L)).thenReturn(java.util.Optional.of(base)));

        UserGroup withUsers = Allure.step("Create user group entity with users", () -> new UserGroup("Team C", "desc"));
        withUsers.setId(21L);
        Allure.step("Set users field on user group entity with 3 users",
                () -> ReflectionTestUtils.setField(withUsers, "users",
                        java.util.List.of(new User(), new User(), new User())));
        Page<UserGroup> page = new PageImpl<>(java.util.List.of(withUsers));
        Allure.step("Mock user group repository to return page with user group having users",
                () -> when(repo.findAllWithUsers(any())).thenReturn(page));

        UserGroup withRoles = Allure.step("Create user group entity with roles", () -> new UserGroup("Team C", "desc"));
        withRoles.setId(21L);
        Allure.step("Set roles field on user group entity with 2 roles",
                () -> ReflectionTestUtils.setField(withRoles, "roles", java.util.List.of(new Role(), new Role())));
        Allure.step("Mock user group repository to return user group with roles",
                () -> when(repo.findByIdWithRoles(21L)).thenReturn(java.util.Optional.of(withRoles)));

        Allure.step("Fetch user group by id 21 with separate users/roles fetches");
        UserGroupResponse resp = Allure.step("Fetch user group by id 21 and return response",
                () -> service.getById(21L));
        Allure.step("Verify member count is 3", () -> assertEquals(3, resp.getMemberCount()));
        Allure.step("Verify role count is 2", () -> assertEquals(2, resp.getRoleCount()));
    }

    @Test
    @Story("Update user group updates fields and computes counts")
    @Severity(SeverityLevel.NORMAL)
    void update_updatesFields_andCountsWithNullCollections() {
        Long id = 30L;
        UserGroup existing = Allure.step("Create existing user group", () -> new UserGroup("Old", "Old desc"));
        existing.setId(id);
        Allure.step("Mock repo.findById to return existing", () -> when(repo.findById(id)).thenReturn(java.util.Optional.of(existing)));

        UserGroup saved = Allure.step("Create saved user group with null collections", () -> new UserGroup("New", "New desc"));
        saved.setId(id);
        // leave users/roles as null -> counts zero
        Allure.step("Mock repo.save to return saved", () -> when(repo.save(any(UserGroup.class))).thenReturn(saved));

        UpdateUserGroupRequest req = new UpdateUserGroupRequest();
        req.setName("New");
        req.setDescription("New desc");

        UserGroupResponse resp = Allure.step("Call update and get response", () -> service.update(id, req));
        Allure.step("Verify id matches", () -> assertEquals(id, resp.getUserGroupId()));
        Allure.step("Verify member count is 0", () -> assertEquals(0, resp.getMemberCount()));
        Allure.step("Verify role count is 0", () -> assertEquals(0, resp.getRoleCount()));
        Allure.step("Verify repo.save received updated name/description",
                () -> verify(repo).save(argThat(ug -> "New".equals(ug.getName()) && "New desc".equals(ug.getDescription()))));
    }

    @Test
    @Story("Update user group throws when not found")
    @Severity(SeverityLevel.CRITICAL)
    void update_throwsWhenNotFound() {
        Long id = 99L;
        Allure.step("Mock repo.findById to return empty", () -> when(repo.findById(id)).thenReturn(java.util.Optional.empty()));
        UpdateUserGroupRequest req = new UpdateUserGroupRequest();
        req.setName("X");
        req.setDescription("Y");
        IllegalArgumentException ex = Allure.step("Call update expecting exception",
                () -> assertThrows(IllegalArgumentException.class, () -> service.update(id, req)));
        Allure.step("Verify message mentions not found with id",
                () -> assertTrue(ex.getMessage().contains("not found with id")));
    }

    @Test
    @Story("Update user group computes counts with non-null collections")
    @Severity(SeverityLevel.NORMAL)
    void update_countsWithNonNullCollections() {
        Long id = 31L;
        UserGroup existing = new UserGroup("Old", "Old desc");
        existing.setId(id);
        when(repo.findById(id)).thenReturn(java.util.Optional.of(existing));

        UserGroup saved = new UserGroup("New", "New desc");
        saved.setId(id);
        ReflectionTestUtils.setField(saved, "users", java.util.List.of(new User(), new User()));
        ReflectionTestUtils.setField(saved, "roles", java.util.List.of(new Role()));
        when(repo.save(any(UserGroup.class))).thenReturn(saved);

        UpdateUserGroupRequest req = new UpdateUserGroupRequest();
        req.setName("New");
        req.setDescription("New desc");

        UserGroupResponse resp = service.update(id, req);
        assertEquals(2, resp.getMemberCount());
        assertEquals(1, resp.getRoleCount());
    }

    @Test
    @Story("Delete user group throws when not exists")
    @Severity(SeverityLevel.CRITICAL)
    void delete_throwsWhenNotExists() {
        Long id = 77L;
        Allure.step("Mock repo.existsById to return false", () -> when(repo.existsById(id)).thenReturn(false));
        IllegalArgumentException ex = Allure.step("Call delete expecting exception",
                () -> assertThrows(IllegalArgumentException.class, () -> service.delete(id)));
        Allure.step("Verify message mentions not found with id",
                () -> assertTrue(ex.getMessage().contains("not found with id")));
    }

    @Test
    @Story("Delete user group when exists")
    @Severity(SeverityLevel.NORMAL)
    void delete_deletesWhenExists() {
        Long id = 78L;
        Allure.step("Mock repo.existsById to return true", () -> when(repo.existsById(id)).thenReturn(true));
        Allure.step("Call delete without exception", () -> service.delete(id));
        Allure.step("Verify repo.deleteById invoked", () -> verify(repo).deleteById(id));
    }

    @Test
    @Story("Get by id throws when not found")
    @Severity(SeverityLevel.CRITICAL)
    void getById_throwsWhenNotFound() {
        Long id = 404L;
        Allure.step("Mock repo.findById to return empty", () -> when(repo.findById(id)).thenReturn(java.util.Optional.empty()));
        IllegalArgumentException ex = Allure.step("Call getById expecting exception",
                () -> assertThrows(IllegalArgumentException.class, () -> service.getById(id)));
        Allure.step("Verify message mentions not found with id",
                () -> assertTrue(ex.getMessage().contains("not found with id")));
    }

    @Test
    @Story("Get by id falls back to base entity when separate fetches missing")
    @Severity(SeverityLevel.NORMAL)
    void getById_fallbacksToBase_whenSeparateFetchesMissing() {
        Long id = 40L;
        UserGroup base = new UserGroup("Base", "Desc");
        base.setId(id);
        // leave users/roles null to produce zero counts
        when(repo.findById(id)).thenReturn(java.util.Optional.of(base));

        // Page contains a different id, so filter finds no match and orElse(base) is used
        UserGroup other = new UserGroup("Other", "Other");
        other.setId(41L);
        other.setUsers(java.util.List.of(new User()));
        Page<UserGroup> page = new PageImpl<>(java.util.List.of(other));
        when(repo.findAllWithUsers(any())).thenReturn(page);

        // Roles fetch returns empty -> roleCount computed from base (null -> 0)
        when(repo.findByIdWithRoles(id)).thenReturn(java.util.Optional.empty());

        UserGroupResponse resp = service.getById(id);
        assertEquals(id, resp.getUserGroupId());
        assertEquals(0, resp.getMemberCount());
        assertEquals(0, resp.getRoleCount());
    }
}