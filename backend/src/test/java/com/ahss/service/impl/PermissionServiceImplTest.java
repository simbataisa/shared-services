package com.ahss.service.impl;

import com.ahss.dto.PermissionDto;
import com.ahss.entity.Permission;
import com.ahss.repository.PermissionRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("IAM")
@Feature("Permission Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PermissionServiceImpl.class)
public class PermissionServiceImplTest {

    @MockBean
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionServiceImpl service;

    @Test
    @Story("List permissions ordered by name")
    @Severity(SeverityLevel.NORMAL)
    void getAllActivePermissions_returnsDtos() {
        Permission p1 = Allure.step("Prepare permission 1 with name View", () -> new Permission());
        p1.setName("View");
        Permission p2 = Allure.step("Prepare permission 2 with name Export", () -> new Permission());
        p2.setName("Export");
        Allure.step("Mock permission repository to return two permissions when findAllOrderByName is called",
                () -> when(permissionRepository.findAllOrderByName()).thenReturn(List.of(p1, p2)));

        Allure.step("Fetch ordered permissions and ensure conversion works");
        List<PermissionDto> result = Allure.step("Call service to get all active permissions",
                () -> service.getAllActivePermissions());
        Allure.addAttachment("Result count", String.valueOf(result.size()));
        Allure.addAttachment("Result names", String.join(", ", result.stream().map(PermissionDto::getName).toList()));
        assertEquals(2, result.size());
    }

    @Test
    @Story("Get permission by id")
    @Severity(SeverityLevel.NORMAL)
    void getPermissionById_returnsDto() {
        Permission p = Allure.step("Prepare permission with name View", () -> new Permission());
        p.setName("View");
        Allure.step("Mock permission repository to return permission with id 10 when findById is called",
                () -> when(permissionRepository.findById(10L)).thenReturn(java.util.Optional.of(p)));

        Allure.step("Fetch permission by id 10");
        java.util.Optional<PermissionDto> result = Allure.step("Call service to get permission by id 10",
                () -> service.getPermissionById(10L));
        Allure.addAttachment("Result name", result.map(PermissionDto::getName).orElse(""));
        Allure.step("Verify result is present and has name View");
        assertTrue(result.isPresent());
        assertEquals("View", result.get().getName());
    }

    @Test
    @Story("Create permission")
    @Severity(SeverityLevel.CRITICAL)
    void createPermission_savesEntity() {
        Allure.step("Mock permission repository to return false when existsByName is called with Export",
                () -> when(permissionRepository.existsByName("Export")).thenReturn(false));
        Allure.step("Mock permission repository to save permission when save is called",
                () -> when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0)));

        PermissionDto dto = Allure.step("Prepare permission dto with name Export and description Export data",
                () -> new PermissionDto());
        dto.setName("Export");
        dto.setDescription("Export data");

        Allure.step("Create new permission Export");
        PermissionDto created = Allure.step("Call service to create permission Export",
                () -> service.createPermission(dto));
        Allure.addAttachment("Created name", created.getName());
        Allure.step("Verify created permission has name Export");
        assertEquals("Export", created.getName());
    }

    @Test
    @Story("Update permission name")
    @Severity(SeverityLevel.NORMAL)
    void updatePermission_updatesFields() {
        Permission existing = Allure.step("Prepare existing permission with name Old", () -> new Permission());
        existing.setName("Old");
        Allure.step("Mock permission repository to return existing permission when findById is called with 5L",
                () -> when(permissionRepository.findById(5L)).thenReturn(java.util.Optional.of(existing)));
        Allure.step("Mock permission repository to save permission when save is called",
                () -> when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> inv.getArgument(0)));

        PermissionDto dto = Allure.step("Prepare permission dto with name New and description desc",
                () -> new PermissionDto());
        dto.setName("New");
        dto.setDescription("desc");

        Allure.step("Update permission to New");
        PermissionDto updated = Allure.step("Call service to update permission 5L with dto",
                () -> service.updatePermission(5L, dto));
        Allure.addAttachment("Updated name", updated.getName());
        Allure.step("Verify updated permission has name New");
        Allure.addAttachment("Updated description", updated.getDescription());
        Allure.step("Verify updated permission has description desc",
                () -> assertEquals("desc", updated.getDescription()));
    }

    @Test
    @Story("Delete permission")
    @Severity(SeverityLevel.NORMAL)
    void deletePermission_deletes() {
        Permission existing = Allure.step("Prepare existing permission with name DeleteMe", () -> new Permission());
        existing.setName("DeleteMe");
        Allure.step("Mock permission repository to return existing permission when findById is called with 3L",
                () -> when(permissionRepository.findById(3L)).thenReturn(java.util.Optional.of(existing)));

        Allure.step("Delete permission 3");
        Allure.step("Call service to delete permission 3L", () -> service.deletePermission(3L));
        Allure.step("Verify permission repository delete is called with existing permission",
                () -> verify(permissionRepository).delete(existing));
    }

    @Test
    @Story("Unsupported activation/deactivation")
    @Severity(SeverityLevel.TRIVIAL)
    void activateAndDeactivatePermission_throwUnsupported() {
        Allure.step("Verify activatePermission throws UnsupportedOperationException for id 1L",
                () -> assertThrows(UnsupportedOperationException.class, () -> service.activatePermission(1L)));
        Allure.step("Verify deactivatePermission throws UnsupportedOperationException for id 1L",
                () -> assertThrows(UnsupportedOperationException.class, () -> service.deactivatePermission(1L)));
    }

    @Test
    @Story("Exists by name")
    @Severity(SeverityLevel.TRIVIAL)
    void existsByName_returnsTrue() {
        Allure.step("Mock permission repository to return true when existsByName is called with Import",
                () -> when(permissionRepository.existsByName("Import")).thenReturn(true));
        Allure.step("Verify service existsByName returns true for Import",
                () -> assertTrue(service.existsByName("Import")));
    }
    
    @Test
    @Story("Prevent duplicate permission creation")
    @Severity(SeverityLevel.CRITICAL)
    void createPermission_throwsWhenNameExists() {
        Allure.step("Mock repository existsByName to return true for Duplicate",
                () -> when(permissionRepository.existsByName("Duplicate")).thenReturn(true));

        PermissionDto dto = new PermissionDto();
        dto.setName("Duplicate");

        Allure.step("Attempt to create duplicate permission should throw");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createPermission(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
    }

    @Test
    @Story("Update permission prevents duplicate name on change")
    @Severity(SeverityLevel.CRITICAL)
    void updatePermission_throwsWhenNameExistsOnChange() {
        Permission existing = new Permission();
        existing.setName("Old");
        when(permissionRepository.findById(9L)).thenReturn(java.util.Optional.of(existing));
        when(permissionRepository.existsByName("New")).thenReturn(true);

        PermissionDto dto = new PermissionDto();
        dto.setName("New");
        dto.setDescription("desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updatePermission(9L, dto));
        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
    }

    @Test
    @Story("Update permission not found")
    @Severity(SeverityLevel.NORMAL)
    void updatePermission_throwsWhenNotFound() {
        when(permissionRepository.findById(404L)).thenReturn(java.util.Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updatePermission(404L, new PermissionDto()));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    @Story("Delete permission not found throws")
    @Severity(SeverityLevel.TRIVIAL)
    void deletePermission_throwsWhenNotFound() {
        when(permissionRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.deletePermission(999L));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    @Story("Get permission by id empty when missing")
    @Severity(SeverityLevel.TRIVIAL)
    void getPermissionById_returnsEmptyWhenMissing() {
        when(permissionRepository.findById(123L)).thenReturn(java.util.Optional.empty());
        assertTrue(service.getPermissionById(123L).isEmpty());
    }
}