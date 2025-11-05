package com.ahss.service.impl;

import com.ahss.dto.RoleDto;
import com.ahss.entity.Role;
import com.ahss.entity.RoleStatus;
import com.ahss.entity.Permission;
import com.ahss.repository.PermissionRepository;
import com.ahss.repository.RoleRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("IAM")
@Feature("Role Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RoleServiceImpl.class)
public class RoleServiceImplTest {

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleServiceImpl service;

    @Test
    @Story("Return only ACTIVE roles")
    @Severity(SeverityLevel.CRITICAL)
    void getAllActiveRoles_filtersInactive() {
        Role active = Allure.step("Create mock active role", () -> mock(Role.class));
        Allure.step("Mock active role with status ACTIVE",
                () -> when(active.getRoleStatus()).thenReturn(RoleStatus.ACTIVE));
        Allure.step("Mock active role with empty permissions",
                () -> when(active.getPermissions()).thenReturn(Collections.emptyList()));

        Role inactive = Allure.step("Create mock inactive role", () -> mock(Role.class));
        Allure.step("Mock inactive role with status INACTIVE",
                () -> when(inactive.getRoleStatus()).thenReturn(RoleStatus.INACTIVE));
        Allure.step("Mock inactive role with empty permissions",
                () -> when(inactive.getPermissions()).thenReturn(Collections.emptyList()));

        Allure.step("Mock role repository to return both active and inactive roles",
                () -> when(roleRepository.findAll()).thenReturn(Arrays.asList(active, inactive)));

        Allure.step("Fetch all active roles and ensure filter works");
        List<RoleDto> result = Allure.step("Call service to get all active roles", () -> service.getAllActiveRoles());

        Allure.addAttachment("Result count", String.valueOf(result.size()));
        assertEquals(1, result.size());
    }

    @Test
    @Story("Prevent updates to INACTIVE roles")
    @Severity(SeverityLevel.CRITICAL)
    void updateRole_throwsWhenInactive() {
        Role existing = Allure.step("Create mock inactive role", () -> mock(Role.class));
        when(existing.getRoleStatus()).thenReturn(RoleStatus.INACTIVE);
        when(roleRepository.findById(99L)).thenReturn(Optional.of(existing));

        Allure.step("Attempt to update an inactive role");
        IllegalArgumentException ex = Allure.step("Assert that IllegalArgumentException is thrown",
                () -> assertThrows(IllegalArgumentException.class, () -> service.updateRole(99L, new RoleDto())));
        Allure.addAttachment("Exception message", ex.getMessage());
        assertTrue(ex.getMessage().contains("Cannot update inactive role"));
    }

    @Test
    @Story("Update role when ACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void updateRole_updatesName_whenActive() {
        Role existing = Allure.step("Create mock active role", () -> new Role());
        existing.setName("Viewer");
        existing.setRoleStatus(RoleStatus.ACTIVE);
        existing.setPermissions(Collections.emptyList());
        existing.setId(7L);
        Allure.step("Mock role repository to return active role when findById is called with ID 7",
                () -> when(roleRepository.findById(7L)).thenReturn(Optional.of(existing)));
        Allure.step("Ensure duplicate name check returns false",
                () -> when(roleRepository.existsByName("Editor")).thenReturn(false));
        Allure.step("Mock role repository to save role when called with any Role object",
                () -> when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0)));

        RoleDto dto = Allure.step("Create RoleDto with name Editor", () -> new RoleDto());
        Allure.step("Set name in RoleDto to Editor", () -> dto.setName("Editor"));

        Allure.step("Update active role name");
        RoleDto updated = Allure.step("Call service to update role name", () -> service.updateRole(7L, dto));
        Allure.addAttachment("Updated role name", updated.getName());
        assertEquals("Editor", updated.getName());
    }

    @Test
    @Story("Get role by id returns only ACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void getRoleById_returnsDto_whenActive_otherwiseEmpty() {
        Role active = new Role();
        active.setId(11L);
        active.setRoleStatus(RoleStatus.ACTIVE);
        active.setPermissions(Collections.emptyList());
        when(roleRepository.findWithPermissions(11L)).thenReturn(Optional.of(active));
        assertTrue(service.getRoleById(11L).isPresent());

        Role inactive = new Role();
        inactive.setId(12L);
        inactive.setRoleStatus(RoleStatus.INACTIVE);
        inactive.setPermissions(Collections.emptyList());
        when(roleRepository.findWithPermissions(12L)).thenReturn(Optional.of(inactive));
        assertTrue(service.getRoleById(12L).isEmpty());
    }

    @Test
    @Story("Create role rejects duplicate name")
    @Severity(SeverityLevel.CRITICAL)
    void createRole_throwsWhenDuplicateName() {
        RoleDto dto = new RoleDto();
        dto.setName("Admin");
        when(roleRepository.existsByName("Admin")).thenReturn(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createRole(dto));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Story("Create role succeeds and defaults ACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void createRole_succeeds_defaultsActive() {
        RoleDto dto = new RoleDto();
        dto.setName("Observer");
        dto.setDescription("Can view");
        when(roleRepository.existsByName("Observer")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(21L);
            return r;
        });
        RoleDto created = service.createRole(dto);
        assertEquals("Observer", created.getName());
        assertEquals(RoleStatus.ACTIVE, created.getRoleStatus());
    }

    @Test
    @Story("Delete/Activate/Deactivate role updates status")
    @Severity(SeverityLevel.MINOR)
    void lifecycle_updates_status() {
        Role r1 = new Role(); r1.setId(30L); r1.setRoleStatus(RoleStatus.ACTIVE);
        when(roleRepository.findById(30L)).thenReturn(Optional.of(r1));
        service.deleteRole(30L);
        
        Role r2 = new Role(); r2.setId(31L); r2.setRoleStatus(RoleStatus.INACTIVE);
        when(roleRepository.findById(31L)).thenReturn(Optional.of(r2));
        service.activateRole(31L);
        
        Role r3 = new Role(); r3.setId(32L); r3.setRoleStatus(RoleStatus.ACTIVE);
        when(roleRepository.findById(32L)).thenReturn(Optional.of(r3));
        service.deactivateRole(32L);
        
        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(roleRepository);
        inOrder.verify(roleRepository).save(argThat(r -> r.getRoleStatus() == RoleStatus.INACTIVE));
        inOrder.verify(roleRepository).save(argThat(r -> r.getRoleStatus() == RoleStatus.ACTIVE));
        inOrder.verify(roleRepository).save(argThat(r -> r.getRoleStatus() == RoleStatus.INACTIVE));
    }

    @Test
    @Story("Exists by role name delegates")
    @Severity(SeverityLevel.TRIVIAL)
    void existsByName_delegatesToRepository() {
        when(roleRepository.existsByName("Reader")).thenReturn(true);
        assertTrue(service.existsByName("Reader"));
    }

    @Test
    @Story("Assign permissions filters duplicates and persists")
    @Severity(SeverityLevel.NORMAL)
    void assignPermissions_addsNew_only() {
        Role role = new Role();
        role.setId(40L);
        role.setRoleStatus(RoleStatus.ACTIVE);
        Permission p1 = new Permission(); p1.setId(1L);
        role.setPermissions(new java.util.ArrayList<>(java.util.List.of(p1)));
        when(roleRepository.findWithPermissions(40L)).thenReturn(Optional.of(role));

        Permission p2 = new Permission(); p2.setId(2L);
        when(permissionRepository.findAllById(java.util.List.of(1L, 2L)))
                .thenReturn(java.util.List.of(p1, p2));

        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        RoleDto dto = service.assignPermissions(40L, java.util.List.of(1L, 2L));
        assertEquals(2, dto.getPermissions().size());
    }

    @Test
    @Story("Assign permissions validates role active and permissions exist")
    @Severity(SeverityLevel.CRITICAL)
    void assignPermissions_throwsOnInactive_orMissingPermissions() {
        Role inactive = new Role();
        inactive.setRoleStatus(RoleStatus.INACTIVE);
        inactive.setPermissions(new java.util.ArrayList<>());
        when(roleRepository.findWithPermissions(50L)).thenReturn(Optional.of(inactive));
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> service.assignPermissions(50L, java.util.List.of(1L)));
        assertTrue(ex1.getMessage().contains("inactive"));

        Role active = new Role();
        active.setRoleStatus(RoleStatus.ACTIVE);
        active.setPermissions(new java.util.ArrayList<>());
        when(roleRepository.findWithPermissions(51L)).thenReturn(Optional.of(active));
        when(permissionRepository.findAllById(java.util.List.of(1L, 2L))).thenReturn(java.util.List.of(new Permission()));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> service.assignPermissions(51L, java.util.List.of(1L, 2L)));
        assertTrue(ex2.getMessage().contains("not found"));
    }

    @Test
    @Story("Remove permissions removes and persists")
    @Severity(SeverityLevel.NORMAL)
    void removePermissions_removesSpecified() {
        Role role = new Role();
        role.setRoleStatus(RoleStatus.ACTIVE);
        Permission p1 = new Permission(); p1.setId(1L);
        Permission p2 = new Permission(); p2.setId(2L);
        role.setPermissions(new java.util.ArrayList<>(java.util.List.of(p1, p2)));
        when(roleRepository.findWithPermissions(60L)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(java.util.List.of(1L))).thenReturn(java.util.List.of(p1));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        RoleDto dto = service.removePermissions(60L, java.util.List.of(1L));
        assertEquals(1, dto.getPermissions().size());
    }
}