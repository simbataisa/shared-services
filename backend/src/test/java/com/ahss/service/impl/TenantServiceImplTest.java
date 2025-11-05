package com.ahss.service.impl;

import com.ahss.dto.TenantDto;
import com.ahss.entity.Tenant;
import com.ahss.entity.TenantStatus;
import com.ahss.repository.TenantRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Catalogue")
@Feature("Tenant Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TenantServiceImpl.class)
public class TenantServiceImplTest {

    @MockBean
    private TenantRepository tenantRepository;

    @Autowired
    private TenantServiceImpl service;

    @Test
    @Story("Filter tenants by status")
    @Severity(SeverityLevel.NORMAL)
    void getTenantsByStatus_returnsDtos() {
        Tenant t1 = Allure.step("Mock tenant entity with ACTIVE status", () -> mock(Tenant.class));
        Tenant t2 = Allure.step("Mock tenant entity with ACTIVE status", () -> mock(Tenant.class));
        Allure.step("Set status of tenant t1 to ACTIVE", () -> when(t1.getStatus()).thenReturn(TenantStatus.ACTIVE));
        Allure.step("Set status of tenant t2 to ACTIVE", () -> when(t2.getStatus()).thenReturn(TenantStatus.ACTIVE));
        Allure.step("Mock repository to return list of t1, t2 when filtered by ACTIVE status",
                () -> when(tenantRepository.findByStatus(TenantStatus.ACTIVE)).thenReturn(List.of(t1, t2)));

        Allure.step("Fetch tenants by ACTIVE status");
        List<TenantDto> result = Allure.step("Call service to get tenants by ACTIVE status",
                () -> service.getTenantsByStatus(TenantStatus.ACTIVE));
        Allure.addAttachment("Result count", String.valueOf(result.size()));
        Allure.step("Verify result size is 2", () -> assertEquals(2, result.size()));
    }

    @Test
    @Story("List all tenants")
    @Severity(SeverityLevel.TRIVIAL)
    void getAllTenants_returnsDtos() {
        Tenant t1 = new Tenant(); t1.setId(1L); t1.setName("A");
        Tenant t2 = new Tenant(); t2.setId(2L); t2.setName("B");
        when(tenantRepository.findAll()).thenReturn(List.of(t1, t2));
        List<TenantDto> dtos = service.getAllTenants();
        assertEquals(2, dtos.size());
    }

    @Test
    @Story("Filter tenants by type")
    @Severity(SeverityLevel.TRIVIAL)
    void getTenantsByType_returnsDtos() {
        Tenant t1 = new Tenant(); t1.setId(3L);
        Tenant t2 = new Tenant(); t2.setId(4L);
        when(tenantRepository.findByType(com.ahss.entity.TenantType.BUSINESS_IN))
                .thenReturn(List.of(t1, t2));
        List<TenantDto> dtos = service.getTenantsByType(com.ahss.entity.TenantType.BUSINESS_IN);
        assertEquals(2, dtos.size());
    }

    @Test
    @Story("Get tenant by id and code")
    @Severity(SeverityLevel.NORMAL)
    void getTenantById_andCode_returnOptional() {
        Tenant t = new Tenant(); t.setId(10L); t.setCode("TEN-10");
        when(tenantRepository.findById(10L)).thenReturn(java.util.Optional.of(t));
        when(tenantRepository.findByCode("TEN-10")).thenReturn(java.util.Optional.of(t));
        assertTrue(service.getTenantById(10L).isPresent());
        assertTrue(service.getTenantByCode("TEN-10").isPresent());
    }

    @Test
    @Story("Update tenant success path")
    @Severity(SeverityLevel.NORMAL)
    void updateTenant_updatesFields() {
        Tenant existing = new Tenant(); existing.setId(20L);
        when(tenantRepository.findById(20L)).thenReturn(java.util.Optional.of(existing));
        when(tenantRepository.existsByCodeAndIdNot("ACME", 20L)).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantDto dto = new TenantDto();
        dto.setCode("ACME"); dto.setName("ACME");
        dto.setType(com.ahss.entity.TenantType.BUSINESS_IN);
        dto.setOrganizationId(99L);
        dto.setStatus(TenantStatus.ACTIVE);

        TenantDto updated = service.updateTenant(20L, dto);
        assertEquals("ACME", updated.getCode());
        assertEquals(TenantStatus.ACTIVE, updated.getStatus());
    }

    @Test
    @Story("Delete tenant throws when missing")
    @Severity(SeverityLevel.MINOR)
    void deleteTenant_throwsWhenMissing() {
        when(tenantRepository.existsById(77L)).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deleteTenant(77L));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @Story("Activate/Deactivate/Suspend tenant updates status")
    @Severity(SeverityLevel.MINOR)
    void lifecycle_updates_status() {
        Tenant t1 = new Tenant(); t1.setId(31L); t1.setStatus(TenantStatus.INACTIVE);
        when(tenantRepository.findById(31L)).thenReturn(java.util.Optional.of(t1));
        service.activateTenant(31L);
        verify(tenantRepository).save(argThat(t -> t.getStatus() == TenantStatus.ACTIVE));

        Tenant t2 = new Tenant(); t2.setId(32L); t2.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById(32L)).thenReturn(java.util.Optional.of(t2));
        service.deactivateTenant(32L);
        verify(tenantRepository).save(argThat(t -> t.getStatus() == TenantStatus.INACTIVE));

        Tenant t3 = new Tenant(); t3.setId(33L); t3.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById(33L)).thenReturn(java.util.Optional.of(t3));
        service.suspendTenant(33L);
        verify(tenantRepository).save(argThat(t -> t.getStatus() == TenantStatus.SUSPENDED));
    }

    @Test
    @Story("Exists checks delegate")
    @Severity(SeverityLevel.TRIVIAL)
    void exists_delegates() {
        when(tenantRepository.existsByCode("TEN-001")).thenReturn(true);
        when(tenantRepository.existsByCodeAndIdNot("TEN-001", 1L)).thenReturn(false);
        assertTrue(service.existsByCode("TEN-001"));
        assertFalse(service.existsByCodeAndIdNot("TEN-001", 1L));
    }
    @Test
    @Story("Prevent duplicate tenant codes on update")
    @Severity(SeverityLevel.CRITICAL)
    void updateTenant_throwsWhenDuplicateCode() {
        Tenant existing = Allure.step("Mock existing tenant entity", () -> new Tenant());
        existing.setId(5L);
        Allure.step("Set ID of existing tenant to 5",
                () -> when(tenantRepository.findById(5L)).thenReturn(Optional.of(existing)));
        Allure.step("Mock repository to return true when checking existence of code 'TEN-001' for tenant ID 5",
                () -> when(tenantRepository.existsByCodeAndIdNot("TEN-001", 5L)).thenReturn(true));

        TenantDto dto = Allure.step("Create DTO with code 'TEN-001' and name 'Tenant 1'", () -> new TenantDto());
        Allure.step("Set code of DTO to 'TEN-001'", () -> dto.setCode("TEN-001"));
        Allure.step("Set name of DTO to 'Tenant 1'", () -> dto.setName("Tenant 1"));

        Allure.step("Attempt to update tenant with duplicate code");
        RuntimeException ex = Allure.step(
                "Verify service throws RuntimeException when updating tenant with duplicate code",
                () -> assertThrows(RuntimeException.class, () -> service.updateTenant(5L, dto)));
        Allure.step("Add exception message as attachment",
                () -> Allure.addAttachment("Exception message", ex.getMessage()));
        Allure.step("Verify exception message contains 'already exists'",
                () -> assertTrue(ex.getMessage().contains("already exists")));
    }

    @Test
    @Story("Activate tenant")
    @Severity(SeverityLevel.NORMAL)
    void activateTenant_setsStatusActive() {
        Tenant tenant = Allure.step("Mock tenant entity with ID 12 and INACTIVE status", () -> new Tenant());
        Allure.step("Set ID of tenant to 12", () -> tenant.setId(12L));
        Allure.step("Set status of tenant to INACTIVE", () -> tenant.setStatus(TenantStatus.INACTIVE));
        Allure.step("Mock repository to return tenant when finding by ID 12",
                () -> when(tenantRepository.findById(12L)).thenReturn(Optional.of(tenant)));

        Allure.step("Activate tenant 12");
        service.activateTenant(12L);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        Allure.step("Verify repository save is called with tenant having ACTIVE status",
                () -> verify(tenantRepository).save(captor.capture()));
        Tenant saved = captor.getValue();
        Allure.step("Verify saved tenant status is ACTIVE", () -> assertEquals(TenantStatus.ACTIVE, saved.getStatus()));
    }

    @Test
    @Story("Deactivate tenant")
    @Severity(SeverityLevel.NORMAL)
    void deactivateTenant_setsStatusInactive() {
        Tenant tenant = Allure.step("Mock tenant entity with ID 13 and ACTIVE status", () -> new Tenant());
        Allure.step("Set ID of tenant to 13", () -> tenant.setId(13L));
        Allure.step("Set status of tenant to ACTIVE", () -> tenant.setStatus(TenantStatus.ACTIVE));
        Allure.step("Mock repository to return tenant when finding by ID 13",
                () -> when(tenantRepository.findById(13L)).thenReturn(Optional.of(tenant)));

        Allure.step("Deactivate tenant 13");
        service.deactivateTenant(13L);

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        Allure.step("Verify repository save is called with tenant having INACTIVE status",
                () -> verify(tenantRepository).save(captor.capture()));
        Allure.step("Verify saved tenant status is INACTIVE",
                () -> assertEquals(TenantStatus.INACTIVE, captor.getValue().getStatus()));
    }

    @Test
    @Story("Suspend tenant")
    @Severity(SeverityLevel.NORMAL)
    void suspendTenant_setsStatusSuspended() {
        Tenant tenant = Allure.step("Mock tenant entity with ID 14 and ACTIVE status", () -> new Tenant());
        Allure.step("Set ID of tenant to 14", () -> tenant.setId(14L));
        Allure.step("Set status of tenant to ACTIVE", () -> tenant.setStatus(TenantStatus.ACTIVE));
        Allure.step("Mock repository to return tenant when finding by ID 14",
                () -> when(tenantRepository.findById(14L)).thenReturn(Optional.of(tenant)));

        Allure.step("Suspend tenant 14");
        service.suspendTenant(14L);
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        Allure.step("Verify repository save is called with tenant having SUSPENDED status",
                () -> verify(tenantRepository).save(captor.capture()));
        Allure.step("Verify saved tenant status is SUSPENDED",
                () -> assertEquals(TenantStatus.SUSPENDED, captor.getValue().getStatus()));
    }

    @Test
    @Story("Delete tenant")
    @Severity(SeverityLevel.TRIVIAL)
    void deleteTenant_deletesWhenExists() {
        Allure.step("Mock repository to return true when checking existence of tenant ID 99",
                () -> when(tenantRepository.existsById(99L)).thenReturn(true));
        Allure.step("Delete tenant 99");
        service.deleteTenant(99L);
        Allure.step("Verify repository deleteById is called with ID 99",
                () -> verify(tenantRepository).deleteById(99L));
    }

    @Test
    @Story("Search tenants")
    @Severity(SeverityLevel.TRIVIAL)
    void searchTenants_returnsDtos() {
        Tenant t1 = Allure.step("Mock tenant entity with ID 100 and name 'Acme Inc'", () -> new Tenant());
        Allure.step("Set ID of tenant to 100", () -> t1.setId(100L));
        Allure.step("Set name of tenant to 'Acme Inc'", () -> t1.setName("Acme Inc"));
        Tenant t2 = Allure.step("Mock tenant entity with ID 101 and name 'Acme Co'", () -> new Tenant());
        Allure.step("Set ID of tenant to 101", () -> t2.setId(101L));
        Allure.step("Set name of tenant to 'Acme Co'", () -> t2.setName("Acme Co"));
        Allure.step("Mock repository to return list of tenants when searching for 'acme'",
                () -> when(tenantRepository.searchTenants("acme")).thenReturn(java.util.List.of(t1, t2)));
        List<TenantDto> dtos = Allure.step("Call searchTenants with 'acme'", () -> service.searchTenants("acme"));
        Allure.step("Verify returned list contains 2 tenants", () -> assertEquals(2, dtos.size()));
        Allure.step("Verify returned list contains tenant with ID 100 and name 'Acme Inc'", () -> assertTrue(
                dtos.stream().anyMatch(d -> d.getId().equals(100L) && d.getName().equals("Acme Inc"))));
        Allure.step("Verify returned list contains tenant with ID 101 and name 'Acme Co'",
                () -> assertTrue(dtos.stream().anyMatch(d -> d.getId().equals(101L) && d.getName().equals("Acme Co"))));
    }
}