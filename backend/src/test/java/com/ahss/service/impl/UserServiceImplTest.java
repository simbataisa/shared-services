package com.ahss.service.impl;

import com.ahss.dto.RoleDto;
import com.ahss.dto.UserDto;
import com.ahss.dto.UserGroupDto;
import com.ahss.entity.Module;
import com.ahss.entity.Permission;
import com.ahss.entity.Role;
import com.ahss.entity.User;
import com.ahss.entity.UserGroup;
import com.ahss.entity.UserStatus;
import com.ahss.repository.RoleRepository;
import com.ahss.repository.UserGroupRepository;
import com.ahss.repository.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("IAM")
@Feature("User Management")
@Owner("backend")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserServiceImpl.class)
public class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private UserGroupRepository userGroupRepository;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private UserServiceImpl service;

    @Test
    @Story("Lock user after repeated failed logins")
    @Severity(SeverityLevel.CRITICAL)
    void incrementFailedLoginAttempts_locksUserOnFifthAttempt() {
        User user = Allure.step("Create user with 4 failed attempts", () -> new User());
        user.setId(1L);
        user.setFailedLoginAttempts(4);
        Allure.step("Mock user repository to return user with 4 failed attempts",
                () -> when(userRepository.findById(1L)).thenReturn(Optional.of(user)));

        Allure.step("Call incrementFailedLoginAttempts for user with 4 failed attempts");
        service.incrementFailedLoginAttempts(1L);

        ArgumentCaptor<Integer> attemptsCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<LocalDateTime> lockCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        Allure.step(
                "Verify user repository updateFailedLoginAttemptsNative is called with 5 attempts and lockUntil set",
                () -> verify(userRepository).updateFailedLoginAttemptsNative(eq(1L), attemptsCaptor.capture(),
                        lockCaptor.capture()));

        Integer attempts = attemptsCaptor.getValue();
        LocalDateTime lockUntil = lockCaptor.getValue();

        Allure.addAttachment("Updated attempts", attempts.toString());
        Allure.addAttachment("Lock until non-null", String.valueOf(lockUntil != null));

        assertEquals(5, attempts, "Attempts should increment to 5");
        assertNotNull(lockUntil, "LockUntil should be set on fifth failed attempt");
    }

    @Test
    @Story("Do not lock user below lock threshold")
    @Severity(SeverityLevel.NORMAL)
    void incrementFailedLoginAttempts_noLockBelowThreshold() {
        User user = Allure.step("Create user with 2 failed attempts", () -> new User());
        user.setId(2L);
        user.setFailedLoginAttempts(2);
        Allure.step("Mock user repository to return user with 2 failed attempts",
                () -> when(userRepository.findById(2L)).thenReturn(Optional.of(user)));

        Allure.step("Call incrementFailedLoginAttempts for user with 2 failed attempts");
        service.incrementFailedLoginAttempts(2L);

        ArgumentCaptor<Integer> attemptsCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<LocalDateTime> lockCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        Allure.step(
                "Verify user repository updateFailedLoginAttemptsNative is called with 3 attempts and lockUntil null",
                () -> verify(userRepository).updateFailedLoginAttemptsNative(eq(2L), attemptsCaptor.capture(),
                        lockCaptor.capture()));

        Integer attempts = attemptsCaptor.getValue();
        LocalDateTime lockUntil = lockCaptor.getValue();

        Allure.addAttachment("Updated attempts", attempts.toString());
        Allure.addAttachment("Lock until should be null", String.valueOf(lockUntil));

        assertEquals(3, attempts, "Attempts should increment to 3");
        assertNull(lockUntil, "LockUntil should remain null below threshold");
    }

    @Test
    @Story("List locked users")
    @Severity(SeverityLevel.NORMAL)
    void getLockedUsers_returnsDtos() {
        User u1 = Allure.step("Create locked user", () -> new User());
        u1.setUserStatus(UserStatus.LOCKED);
        User u2 = Allure.step("Create locked user", () -> new User());
        u2.setUserStatus(UserStatus.LOCKED);
        Allure.step("Mock user repository to return locked users",
                () -> when(userRepository.findLockedUsers(any())).thenReturn(List.of(u1, u2)));

        Allure.step("Fetch locked users");
        List<UserDto> result = service.getLockedUsers();
        Allure.addAttachment("Locked users count", String.valueOf(result.size()));
        assertEquals(2, result.size());
    }

    @Test
    @Story("List users by status")
    @Severity(SeverityLevel.TRIVIAL)
    void getUsersByStatus_returnsDtos() {
        User u1 = Allure.step("Create active user", () -> new User());
        u1.setUserStatus(UserStatus.ACTIVE);
        Allure.step("Mock user repository to return active user",
                () -> when(userRepository.findByUserStatus(UserStatus.ACTIVE)).thenReturn(List.of(u1)));
        List<UserDto> result = Allure.step("Fetch active users", () -> service.getUsersByStatus(UserStatus.ACTIVE));
        Allure.addAttachment("Active users count", String.valueOf(result.size()));
        assertEquals(1, result.size());
    }

    @Test
    @Story("Get user by ID returns empty when not found")
    @Severity(SeverityLevel.TRIVIAL)
    void getUserById_returnsEmpty_whenNotFound() {
        Allure.step("Mock user repository to return empty for user 99",
                () -> when(userRepository.findWithRolesAndUserGroups(99L)).thenReturn(Optional.empty()));
        Optional<UserDto> result = Allure.step("Fetch user by ID 99", () -> service.getUserById(99L));
        Allure.step("Verify result is empty", () -> assertTrue(result.isEmpty()));
    }

    @Test
    @Story("Get user by username or email returns DTO with password")
    @Severity(SeverityLevel.NORMAL)
    void getUserByUsernameOrEmail_returnsDtoWithPassword_present() {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        Allure.step("Mock user repository to return user by usernameOrEmail",
                () -> when(userRepository.findByUsernameOrEmail("john", "john"))
                        .thenReturn(Optional.of(user)));
        Optional<UserDto> dtoOpt = Allure.step("Fetch by usernameOrEmail 'john'",
                () -> service.getUserByUsernameOrEmail("john"));
        Allure.step("Verify present and contains password", () -> {
            assertTrue(dtoOpt.isPresent());
            assertEquals("secret", dtoOpt.get().getPassword());
        });
    }

    @Test
    @Story("Get user by username or email returns empty when not found")
    @Severity(SeverityLevel.TRIVIAL)
    void getUserByUsernameOrEmail_returnsEmpty_whenNotFound() {
        Allure.step("Mock user repository to return empty for lookup 'missing'",
                () -> when(userRepository.findByUsernameOrEmail("missing", "missing"))
                        .thenReturn(Optional.empty()));
        Optional<UserDto> dtoOpt = Allure.step("Fetch by usernameOrEmail 'missing'",
                () -> service.getUserByUsernameOrEmail("missing"));
        Allure.step("Verify empty", () -> assertTrue(dtoOpt.isEmpty()));
    }

    @Test
    @Story("Get all active users maps roles, groups, permissions including module branch")
    @Severity(SeverityLevel.NORMAL)
    void getAllActiveUsers_mapsNestedRolesGroupsPermissions() {
        Permission permWithModule = Allure.step("Create permission with module", () -> new Permission());
        Allure.step("Set permission ID to 1", () -> permWithModule.setId(1L));
        Allure.step("Set permission name to READ_USERS", () -> permWithModule.setName("READ_USERS"));
        Module module = Allure.step("Create module", () -> new Module());
        Allure.step("Set module ID to 10", () -> module.setId(10L));
        Allure.step("Set module name to USER_MGMT", () -> module.setName("USER_MGMT"));
        Allure.step("Set permission module to created module", () -> permWithModule.setModule(module));

        Permission permNoModule = Allure.step("Create permission without module", () -> new Permission());
        Allure.step("Set permission ID to 2", () -> permNoModule.setId(2L));
        Allure.step("Set permission name to WRITE_USERS", () -> permNoModule.setName("WRITE_USERS"));

        Role role = Allure.step("Create role", () -> new Role());
        Allure.step("Set role ID to 3", () -> role.setId(3L));
        Allure.step("Set role name to Admin", () -> role.setName("Admin"));
        Allure.step("Add permissions to role",
                () -> role.setPermissions(java.util.Arrays.asList(permWithModule, permNoModule)));

        UserGroup group = Allure.step("Create user group", () -> new UserGroup());
        Allure.step("Set group ID to 4", () -> group.setId(4L));
        Allure.step("Set group name to Core", () -> group.setName("Core"));
        Allure.step("Add role to group", () -> group.setRoles(Collections.singletonList(role)));

        User user = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 5", () -> user.setId(5L));
        Allure.step("Set username to alice", () -> user.setUsername("alice"));
        Allure.step("Set email to alice@example.com", () -> user.setEmail("alice@example.com"));
        Allure.step("Add role to user", () -> user.setRoles(Collections.singletonList(role)));
        Allure.step("Add group to user", () -> user.setUserGroups(Collections.singletonList(group)));

        Allure.step("Mock repository to return one active user with nested structures",
                () -> when(userRepository.findAllActive()).thenReturn(Collections.singletonList(user)));

        java.util.List<UserDto> dtos = Allure.step("Fetch all active users",
                () -> service.getAllActiveUsers());
        Allure.step("Verify mappings for roles, groups, and permission module fields", () -> {
            assertEquals(1, dtos.size());
            UserDto dto = dtos.get(0);
            assertEquals(1, dto.getRoles().size());
            RoleDto rDto = dto.getRoles().get(0);
            assertEquals(2, rDto.getPermissions().size());
            assertEquals("USER_MGMT", rDto.getPermissions().get(0).getModuleName());
            assertNull(rDto.getPermissions().get(1).getModuleName());

            assertEquals(1, dto.getUserGroups().size());
            UserGroupDto gDto = dto.getUserGroups().get(0);
            assertEquals(1, gDto.getRoles().size());
            assertEquals("Admin", gDto.getRoles().get(0).getName());
        });
    }

    @Test
    @Story("Get user by username returns present and empty")
    @Severity(SeverityLevel.NORMAL)
    void getUserByUsername_present_and_empty() {
        User user = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 6", () -> user.setId(6L));
        Allure.step("Set username to bob", () -> user.setUsername("bob"));
        Allure.step("Set email to bob@example.com", () -> user.setEmail("bob@example.com"));
        Allure.step("Mock repository to return user for username 'bob'",
                () -> when(userRepository.findByUsernameWithRoles("bob")).thenReturn(Optional.of(user)));
        Optional<UserDto> present = Allure.step("Fetch user by username 'bob'",
                () -> service.getUserByUsername("bob"));
        Allure.step("Verify user is present", () -> assertTrue(present.isPresent()));
        Allure.step("Verify username is 'bob'", () -> assertEquals("bob", present.get().getUsername()));

        Allure.step("Mock repository to return empty for username 'charlie'",
                () -> when(userRepository.findByUsernameWithRoles("charlie")).thenReturn(Optional.empty()));
        Optional<UserDto> empty = Allure.step("Fetch user by username 'charlie'",
                () -> service.getUserByUsername("charlie"));
        Allure.step("Verify user is empty", () -> assertFalse(empty.isPresent()));
    }

    @Test
    @Story("Get user by email returns present and empty")
    @Severity(SeverityLevel.NORMAL)
    void getUserByEmail_present_and_empty() {
        User user = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 7", () -> user.setId(7L));
        Allure.step("Set username to dave", () -> user.setUsername("dave"));
        Allure.step("Set email to dave@example.com", () -> user.setEmail("dave@example.com"));
        Allure.step("Mock repository to return user for email 'dave@example.com'",
                () -> when(userRepository.findByEmailWithRoles("dave@example.com")).thenReturn(Optional.of(user)));
        Optional<UserDto> present = Allure.step("Fetch user by email 'dave@example.com'",
                () -> service.getUserByEmail("dave@example.com"));
        Allure.step("Verify user is present", () -> assertTrue(present.isPresent()));
        Allure.step("Verify email is 'dave@example.com'",
                () -> assertEquals("dave@example.com", present.get().getEmail()));

        Allure.step("Mock repository to return empty for email 'nope@example.com'",
                () -> when(userRepository.findByEmailWithRoles("nope@example.com")).thenReturn(Optional.empty()));
        Optional<UserDto> empty = Allure.step("Fetch user by email 'nope@example.com'",
                () -> service.getUserByEmail("nope@example.com"));
        Allure.step("Verify user is empty", () -> assertFalse(empty.isPresent()));
    }

    @Test
    @Story("Search users returns list and supports empty result")
    @Severity(SeverityLevel.NORMAL)
    void searchUsers_returnsList_and_empty() {
        User u1 = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 8", () -> u1.setId(8L));
        Allure.step("Set username to x", () -> u1.setUsername("x"));
        User u2 = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 9", () -> u2.setId(9L));
        Allure.step("Set username to y", () -> u2.setUsername("y"));
        Allure.step("Mock repository to return users for search 'xy'",
                () -> when(userRepository.searchUsers("xy")).thenReturn(Arrays.asList(u1, u2)));
        List<UserDto> two = Allure.step("Search users with 'xy'", () -> service.searchUsers("xy"));
        Allure.step("Verify 2 users are found", () -> assertEquals(2, two.size()));

        Allure.step("Mock repository to return empty for search 'none'",
                () -> when(userRepository.searchUsers("none")).thenReturn(Collections.emptyList()));
        List<UserDto> none = Allure.step("Search users with 'none'", () -> service.searchUsers("none"));
        Allure.step("Verify no users are found", () -> assertTrue(none.isEmpty()));
    }

    @Test
    @Story("Get locked/unverified/inactive users and created-between queries")
    @Severity(SeverityLevel.NORMAL)
    void getLockedUnverifiedInactiveCreatedBetween_queriesReturnMappedDtos() {
        User u = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 11", () -> u.setId(11L));
        Allure.step("Set username to lock", () -> u.setUsername("lock"));
        Allure.step("Mock repository to return locked user for date '2023-01-01'",
                () -> when(userRepository.findLockedUsers(org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                        .thenReturn(Collections.singletonList(u)));
        List<UserDto> locked = Allure.step("Get locked users", () -> service.getLockedUsers());
        Allure.step("Verify 1 locked user is found", () -> assertEquals(1, locked.size()));

        User uv = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 12", () -> uv.setId(12L));
        Allure.step("Set username to uv", () -> uv.setUsername("uv"));
        Allure.step("Mock repository to return unverified user",
                () -> when(userRepository.findUnverifiedUsers()).thenReturn(Collections.singletonList(uv)));
        List<UserDto> unverified = Allure.step("Get unverified users", () -> service.getUnverifiedUsers());
        Allure.step("Verify 1 unverified user is found", () -> assertEquals(1, unverified.size()));

        User in = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 13", () -> in.setId(13L));
        Allure.step("Set username to in", () -> in.setUsername("in"));
        Allure.step("Mock repository to return inactive user for date '2023-01-01'",
                () -> when(userRepository.findInactiveUsers(org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                        .thenReturn(Collections.singletonList(in)));
        List<UserDto> inactive = Allure.step("Get inactive users", () -> service
                .getInactiveUsers(LocalDateTime.now().minusDays(30)));
        Allure.step("Verify 1 inactive user is found", () -> assertEquals(1, inactive.size()));

        User cb = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 14", () -> cb.setId(14L));
        Allure.step("Set username to cb", () -> cb.setUsername("cb"));
        Allure.step("Mock repository to return created-between user for date '2023-01-01'",
                () -> when(userRepository.findUsersCreatedBetween(org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                        .thenReturn(Collections.singletonList(cb)));
        List<UserDto> between = Allure.step("Get users created between '2023-01-01' and now",
                () -> service.getUsersCreatedBetween(LocalDateTime.now().minusDays(7), LocalDateTime.now()));
        Allure.step("Verify 1 created-between user is found", () -> assertEquals(1, between.size()));
    }

    @Test
    @Story("Get users created-between returns list and supports empty result")
    @Severity(SeverityLevel.NORMAL)
    void getUsersCreatedBetween_returnsList_and_empty() {
        Allure.step("Mock repository to return empty for created-between query",
                () -> when(userRepository.findUsersCreatedBetween(org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                        org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                        .thenReturn(Collections.emptyList()));
        List<UserDto> between = Allure.step("Get users created between '2023-01-01' and now",
                () -> service.getUsersCreatedBetween(LocalDateTime.now().minusDays(7), LocalDateTime.now()));
        Allure.step("Verify no created-between users are found", () -> assertTrue(between.isEmpty()));
    }

    @Test
    @Story("Exists-by excluding id paths return true and false")
    @Severity(SeverityLevel.NORMAL)
    void existsByUsernameAndEmailAndIdNot_true_and_false() {
        Allure.step("Mock repository to return true for exists-by username 'sam' and id not 100",
                () -> when(userRepository.existsByUsernameAndIdNot("sam", 100L)).thenReturn(true));
        Allure.step("Mock repository to return false for exists-by email 'sam@example.com' and id not 100",
                () -> when(userRepository.existsByEmailAndIdNot("sam@example.com", 100L)).thenReturn(false));
        Allure.step("Verify exists-by username 'sam' and id not 100 returns true",
                () -> assertTrue(service.existsByUsernameAndIdNot("sam", 100L)));
        Allure.step("Verify exists-by email 'sam@example.com' and id not 100 returns false",
                () -> assertFalse(service.existsByEmailAndIdNot("sam@example.com", 100L)));
    }

    @Test
    @Story("Get by usernameOrEmail maps roles/groups and includes password")
    @Severity(SeverityLevel.NORMAL)
    void getUserByUsernameOrEmail_mapsRolesGroupsAndPassword() {
        Permission p = Allure.step("Create permission", () -> new Permission());
        Allure.step("Set permission ID to 21", () -> p.setId(21L));
        Allure.step("Set permission name to READ", () -> p.setName("READ"));
        Role role = Allure.step("Create role", () -> new Role());
        Allure.step("Set role ID to 22", () -> role.setId(22L));
        Allure.step("Set role name to Viewer", () -> role.setName("Viewer"));
        Allure.step("Add permission to role", () -> role.setPermissions(Collections.singletonList(p)));
        UserGroup ug = Allure.step("Create user group", () -> new UserGroup());
        Allure.step("Set user group ID to 23", () -> ug.setId(23L));
        Allure.step("Set user group name to UG", () -> ug.setName("UG"));
        Allure.step("Add role to user group", () -> ug.setRoles(Collections.singletonList(role)));
        User user = Allure.step("Create user", () -> new User());
        Allure.step("Set user ID to 24", () -> user.setId(24L));
        Allure.step("Set username to eve", () -> user.setUsername("eve"));
        Allure.step("Set email to eve@example.com", () -> user.setEmail("eve@example.com"));
        Allure.step("Set password to pw", () -> user.setPassword("pw"));
        Allure.step("Add role to user", () -> user.setRoles(Collections.singletonList(role)));
        Allure.step("Add user group to user", () -> user.setUserGroups(Collections.singletonList(ug)));
        when(userRepository.findByUsernameOrEmail("eve", "eve")).thenReturn(Optional.of(user));
        Optional<UserDto> dtoOpt = Allure.step("Get user by username or email 'eve'",
                () -> service.getUserByUsernameOrEmail("eve"));
        Allure.step("Verify user is present", () -> assertTrue(dtoOpt.isPresent()));
        UserDto dto = dtoOpt.get();
        Allure.step("Verify password is 'pw'", () -> assertEquals("pw", dto.getPassword()));
        Allure.step("Verify 1 role is mapped", () -> assertEquals(1, dto.getRoles().size()));
        Allure.step("Verify 1 user group is mapped", () -> assertEquals(1, dto.getUserGroups().size()));
    }

    @Test
    @Story("createUser sets defaults when DTO fields are null")
    @Severity(SeverityLevel.NORMAL)
    void createUser_setsDefaults_whenDtoFieldsNull() {
        UserDto dto = Allure.step("Create user DTO", () -> new UserDto());
        Allure.step("Set username to 'newbie'", () -> dto.setUsername("newbie"));
        Allure.step("Set email to 'newbie@example.com'", () -> dto.setEmail("newbie@example.com"));
        Allure.step("Mock repository to return false for exists-by username 'newbie'",
                () -> when(userRepository.existsByUsername("newbie")).thenReturn(false));
        Allure.step("Mock repository to return false for exists-by email 'newbie@example.com'",
                () -> when(userRepository.existsByEmail("newbie@example.com")).thenReturn(false));

        org.mockito.stubbing.Answer<User> answer = inv -> (User) inv.getArguments()[0];
        Allure.step("Mock repository to save user and return it",
                () -> when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(answer));

        Allure.step("Create user from DTO", () -> service.createUser(dto));
        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor
                .forClass(User.class);
        Allure.step("Verify user repository save is called with captured user",
                () -> org.mockito.Mockito.verify(userRepository).save(captor.capture()));
        User saved = captor.getValue();
        Allure.step("Verify user status is ACTIVE", () -> assertEquals(UserStatus.ACTIVE, saved.getUserStatus()));
        Allure.step("Verify emailVerified is false", () -> assertFalse(saved.getEmailVerified()));
        Allure.step("Verify createdBy is 'system'", () -> assertEquals("system", saved.getCreatedBy()));
        Allure.step("Verify updatedBy is 'system'", () -> assertEquals("system", saved.getUpdatedBy()));
    }

    @Test
    @Story("createUser respects provided status, emailVerified, createdBy/updatedBy")
    @Severity(SeverityLevel.NORMAL)
    void createUser_respectsProvidedFlags() {
        UserDto dto = Allure.step("Create user DTO", () -> new UserDto());
        Allure.step("Set username to 'pro'", () -> dto.setUsername("pro"));
        Allure.step("Set email to 'pro@example.com'", () -> dto.setEmail("pro@example.com"));
        Allure.step("Set user status to SUSPENDED", () -> dto.setUserStatus(UserStatus.SUSPENDED));
        Allure.step("Set emailVerified to true", () -> dto.setEmailVerified(true));
        Allure.step("Set createdBy to 'admin'", () -> dto.setCreatedBy("admin"));
        Allure.step("Set updatedBy to 'owner'", () -> dto.setUpdatedBy("owner"));
        Allure.step("Mock repository to return false for exists-by username 'pro'",
                () -> when(userRepository.existsByUsername("pro")).thenReturn(false));
        Allure.step("Mock repository to return false for exists-by email 'pro@example.com'",
                () -> when(userRepository.existsByEmail("pro@example.com")).thenReturn(false));

        org.mockito.stubbing.Answer<User> answer = inv -> (User) inv.getArguments()[0];
        Allure.step("Mock repository to save user and return it",
                () -> when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(answer));

        Allure.step("Create user from DTO", () -> service.createUser(dto));
        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor
                .forClass(User.class);
        Allure.step("Verify user repository save is called with captured user",
                () -> org.mockito.Mockito.verify(userRepository).save(captor.capture()));
        User saved = captor.getValue();
        Allure.step("Verify user status is SUSPENDED", () -> assertEquals(UserStatus.SUSPENDED, saved.getUserStatus()));
        Allure.step("Verify emailVerified is true", () -> assertTrue(saved.getEmailVerified()));
        Allure.step("Verify createdBy is 'admin'", () -> assertEquals("admin", saved.getCreatedBy()));
        Allure.step("Verify updatedBy is 'owner'", () -> assertEquals("owner", saved.getUpdatedBy()));
    }

    @Test
    @Story("Create user rejects duplicate username and email")
    @Severity(SeverityLevel.CRITICAL)
    void createUser_throwsOnDuplicateUsernameAndEmail() {
        UserDto dto = Allure.step("Create user DTO", () -> new UserDto());
        Allure.step("Set username to 'dup'", () -> dto.setUsername("dup"));
        Allure.step("Set email to 'dup@example.com'", () -> dto.setEmail("dup@example.com"));
        Allure.step("Mock user repository existsByUsername and existsByEmail to true",
                () -> {
                    when(userRepository.existsByUsername("dup")).thenReturn(true);
                    when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);
                });
        IllegalArgumentException ex1 = Allure.step("Create user with duplicate username",
                () -> assertThrows(IllegalArgumentException.class, () -> service.createUser(dto)));
        Allure.step("Verify duplicate username message", () -> assertTrue(ex1.getMessage().contains("already exists")));

        // Now only email duplicate
        Allure.step("Mock user repository existsByUsername to false",
                () -> when(userRepository.existsByUsername("dup")).thenReturn(false));
        Allure.step("Mock user repository existsByEmail to true",
                () -> when(userRepository.existsByEmail("dup@example.com")).thenReturn(true));
        IllegalArgumentException ex2 = Allure.step("Create user with duplicate email",
                () -> assertThrows(IllegalArgumentException.class, () -> service.createUser(dto)));
        Allure.step("Verify duplicate email message", () -> assertTrue(ex2.getMessage().contains("already exists")));
    }

    @Test
    @Story("Create user encodes password when provided and defaults status")
    @Severity(SeverityLevel.NORMAL)
    void createUser_encodesPassword_andDefaultsStatus() {
        UserDto dto = Allure.step("Create user DTO", () -> new UserDto());
        Allure.step("Set username to 'alice'", () -> dto.setUsername("alice"));
        Allure.step("Set email to 'alice@example.com'", () -> dto.setEmail("alice@example.com"));
        Allure.step("Set password to 'plain'", () -> dto.setPassword("plain"));
        Allure.step("Mock user repository existsByUsername to false",
                () -> when(userRepository.existsByUsername("alice")).thenReturn(false));
        Allure.step("Mock user repository existsByEmail to false",
                () -> when(userRepository.existsByEmail("alice@example.com")).thenReturn(false));
        Allure.step("Mock password encoder to return encoded",
                () -> when(passwordEncoder.encode("plain")).thenReturn("encoded"));
        Allure.step("Mock save to echo argument",
                () -> when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0)));

        UserDto saved = Allure.step("Create user alice with password",
                () -> service.createUser(dto));
        Allure.step("Verify encoded password and default ACTIVE status",
                () -> {
                    verify(userRepository).save(argThat(u -> "encoded".equals(u.getPassword())));
                    assertEquals(UserStatus.ACTIVE, saved.getUserStatus());
                });

        // Blank password path: ensure encoder not called
        UserDto dto2 = Allure.step("Create user DTO", () -> new UserDto());
        Allure.step("Set username to 'bob'", () -> dto2.setUsername("bob"));
        Allure.step("Set email to 'bob@example.com'", () -> dto2.setEmail("bob@example.com"));
        Allure.step("Set password to ''", () -> dto2.setPassword(""));
        Allure.step("Mock user repository existsByUsername to false",
                () -> when(userRepository.existsByUsername("bob")).thenReturn(false));
        Allure.step("Mock user repository existsByEmail to false",
                () -> when(userRepository.existsByEmail("bob@example.com")).thenReturn(false));
        Allure.step("Create user bob with blank password",
                () -> service.createUser(dto2));
        Allure.step("Verify encoder not called for blank password",
                () -> verify(passwordEncoder, never()).encode(""));
    }

    @Test
    @Story("Update user rejects duplicate username/email and preserves status when null")
    @Severity(SeverityLevel.CRITICAL)
    void updateUser_duplicates_throw_and_statusPreservedWhenNull() {
        User existing = Allure.step("Create existing user", () -> new User());
        Allure.step("Set user ID to 50L", () -> existing.setId(50L));
        Allure.step("Set user status to ACTIVE", () -> existing.setUserStatus(UserStatus.ACTIVE));
        Allure.step("Mock user repository findById to return existing user",
                () -> when(userRepository.findById(50L)).thenReturn(Optional.of(existing)));

        UserDto dto = Allure.step("Create user DTO", () -> new UserDto());
        Allure.step("Set username to 'newu'", () -> dto.setUsername("newu"));
        Allure.step("Set email to 'new@example.com'", () -> dto.setEmail("new@example.com"));
        // duplicate username
        Allure.step("Mock user repository existsByUsernameAndIdNot to true",
                () -> when(userRepository.existsByUsernameAndIdNot("newu", 50L)).thenReturn(true));
        IllegalArgumentException ex1 = Allure.step("Update user with duplicate username",
                () -> assertThrows(IllegalArgumentException.class, () -> service.updateUser(50L, dto)));
        Allure.step("Verify duplicate username message", () -> assertTrue(ex1.getMessage().contains("already exists")));

        // duplicate email
        Allure.step("Mock user repository existsByUsernameAndIdNot to false",
                () -> when(userRepository.existsByUsernameAndIdNot("newu", 50L)).thenReturn(false));
        Allure.step("Mock user repository existsByEmailAndIdNot to true",
                () -> when(userRepository.existsByEmailAndIdNot("new@example.com", 50L)).thenReturn(true));
        IllegalArgumentException ex2 = Allure.step("Update user with duplicate email",
                () -> assertThrows(IllegalArgumentException.class, () -> service.updateUser(50L, dto)));
        Allure.step("Verify duplicate email message", () -> assertTrue(ex2.getMessage().contains("already exists")));

        // status preserved when null
        Allure.step("Mock user repository existsByEmailAndIdNot to false",
                () -> when(userRepository.existsByEmailAndIdNot("new@example.com", 50L)).thenReturn(false));
        Allure.step("Set user status to null", () -> dto.setUserStatus(null));
        Allure.step("Mock save to echo argument",
                () -> when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0)));
        UserDto updated = Allure.step("Update user with null status",
                () -> service.updateUser(50L, dto));
        Allure.step("Verify status preserved as ACTIVE",
                () -> assertEquals(UserStatus.ACTIVE, updated.getUserStatus()));
    }

    @Test
    @Story("Delete/activate/deactivate throw when user not found")
    @Severity(SeverityLevel.NORMAL)
    void delete_activate_deactivate_throw_whenNotFound() {
        Allure.step("Mock user repository findById to return empty for 70L",
                () -> when(userRepository.findById(70L)).thenReturn(Optional.empty()));
        Allure.step("Mock user repository findById to return empty for 71L",
                () -> when(userRepository.findById(71L)).thenReturn(Optional.empty()));
        Allure.step("Mock user repository findById to return empty for 72L",
                () -> when(userRepository.findById(72L)).thenReturn(Optional.empty()));
        Allure.step("Verify deleteUser throws IllegalArgumentException for 70L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.deleteUser(70L)));
        Allure.step("Verify activateUser throws IllegalArgumentException for 71L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.activateUser(71L)));
        Allure.step("Verify deactivateUser throws IllegalArgumentException for 72L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.deactivateUser(72L)));
    }

    @Test
    @Story("Lock/unlock/verify/update last login fail when not found")
    @Severity(SeverityLevel.TRIVIAL)
    void lock_unlock_verify_updateLastLogin_throw_whenNotFound() {
        Allure.step("Mock user repository findById to return empty for 73L",
                () -> when(userRepository.findById(73L)).thenReturn(Optional.empty()));
        Allure.step("Mock user repository findById to return empty for 74L",
                () -> when(userRepository.findById(74L)).thenReturn(Optional.empty()));
        Allure.step("Mock user repository findById to return empty for 75L",
                () -> when(userRepository.findById(75L)).thenReturn(Optional.empty()));
        Allure.step("Mock user repository findById to return empty for 76L",
                () -> when(userRepository.findById(76L)).thenReturn(Optional.empty()));
        Allure.step("Verify lockUser throws IllegalArgumentException for 73L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.lockUser(73L, LocalDateTime.now())));
        Allure.step("Verify unlockUser throws IllegalArgumentException for 74L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.unlockUser(74L)));
        Allure.step("Verify verifyEmail throws IllegalArgumentException for 75L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.verifyEmail(75L)));
        Allure.step("Verify updateLastLogin throws IllegalArgumentException for 76L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.updateLastLogin(76L)));
    }

    @Test
    @Story("Failed login attempts and reset throw when not found")
    @Severity(SeverityLevel.TRIVIAL)
    void failedAttempts_reset_throw_whenNotFound() {
        Allure.step("Mock user repository findById to return empty for 77L",
                () -> when(userRepository.findById(77L)).thenReturn(Optional.empty()));
        Allure.step("Mock user repository findById to return empty for 78L",
                () -> when(userRepository.findById(78L)).thenReturn(Optional.empty()));
        Allure.step("Verify incrementFailedLoginAttempts throws IllegalArgumentException for 77L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.incrementFailedLoginAttempts(77L)));
        Allure.step("Verify resetFailedLoginAttempts throws IllegalArgumentException for 78L",
                () -> assertThrows(IllegalArgumentException.class, () -> service.resetFailedLoginAttempts(78L)));
    }

    @Test
    @Story("Assign/remove roles validate user and role existence")
    @Severity(SeverityLevel.CRITICAL)
    void assignRoles_throwsWhenUserNotFound_orRolesMissing() {
        Allure.step("Mock user repository findWithRoles to return empty for 80L",
                () -> when(userRepository.findWithRoles(80L)).thenReturn(Optional.empty()));
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> service.assignRoles(80L, List.of(1L)));
        Allure.step("Verify exception message contains 'User not found'",
                () -> assertTrue(ex1.getMessage().contains("User not found")));

        User user = Allure.step("Create user with ID 81L", () -> new User());
        user.setId(81L);
        user.setRoles(new java.util.ArrayList<>());
        Allure.step("Mock user repository findWithRoles to return user with ID 81L",
                () -> when(userRepository.findWithRoles(81L)).thenReturn(Optional.of(user)));
        Role r1 = Allure.step("Create role with ID 1L", () -> new Role());
        r1.setId(1L);
        Allure.step("Mock role repository findAllById to return role with ID 1L",
                () -> when(roleRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(r1)));
        IllegalArgumentException ex2 = Allure.step(
                "Verify assignRoles throws IllegalArgumentException for 81L with roles 1L, 2L",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.assignRoles(81L, List.of(1L, 2L))));
        Allure.step("Verify exception message contains 'roles not found'",
                () -> assertTrue(ex2.getMessage().contains("roles not found")));
    }

    @Test
    @Story("Assign/remove user groups validate user and group existence")
    @Severity(SeverityLevel.CRITICAL)
    void assignUserGroups_throwsWhenUserNotFound_orGroupsMissing() {
        Allure.step("Mock user repository findWithUserGroups to return empty for 82L",
                () -> when(userRepository.findWithUserGroups(82L)).thenReturn(Optional.empty()));
        IllegalArgumentException ex1 = Allure.step(
                "Verify assignUserGroups throws IllegalArgumentException for 82L with group 10L",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.assignUserGroups(82L, List.of(10L))));
        Allure.step("Verify exception message contains 'User not found'",
                () -> assertTrue(ex1.getMessage().contains("User not found")));

        User user = Allure.step("Create user with ID 83L", () -> new User());
        user.setId(83L);
        user.setUserGroups(new java.util.ArrayList<>());
        Allure.step("Mock user repository findWithUserGroups to return user with ID 83L",
                () -> when(userRepository.findWithUserGroups(83L)).thenReturn(Optional.of(user)));
        UserGroup g1 = Allure.step("Create user group with ID 10L", () -> new UserGroup());
        g1.setId(10L);
        Allure.step("Mock user group repository findAllById to return user group with ID 10L",
                () -> when(userGroupRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(g1)));
        IllegalArgumentException ex2 = Allure.step(
                "Verify assignUserGroups throws IllegalArgumentException for 83L with groups 10L, 11L",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.assignUserGroups(83L, List.of(10L, 11L))));
        Allure.step("Verify exception message contains 'user groups not found'",
                () -> assertTrue(ex2.getMessage().contains("user groups not found")));
    }

    @Test
    @Story("Change password encodes and saves")
    @Severity(SeverityLevel.CRITICAL)
    void changePassword_encodesAndSaves() {
        User user = Allure.step("Create user with old password", () -> new User());
        user.setId(10L);
        user.setPassword("old");
        Allure.step("Mock user repository to return user with old password",
                () -> when(userRepository.findById(10L)).thenReturn(Optional.of(user)));
        Allure.step("Mock password encoder to return encoded password",
                () -> when(passwordEncoder.encode("newpass")).thenReturn("encoded"));

        Allure.step("Change password for user 10", () -> service.changePassword(10L, "newpass"));
        Allure.step("Verify user repository save is called with encoded password",
                () -> verify(userRepository).save(argThat(u -> "encoded".equals(u.getPassword()))));
    }

    @Test
    @Story("Soft delete sets status INACTIVE")
    @Severity(SeverityLevel.NORMAL)
    void deleteUser_setsInactive() {
        User user = Allure.step("Create active user", () -> new User());
        user.setId(11L);
        user.setUserStatus(UserStatus.ACTIVE);
        Allure.step("Mock user repository to return active user",
                () -> when(userRepository.findById(11L)).thenReturn(Optional.of(user)));

        Allure.step("Soft delete user 11", () -> service.deleteUser(11L));
        Allure.step("Verify user repository save is called with inactive status",
                () -> verify(userRepository).save(argThat(u -> u.getUserStatus() == UserStatus.INACTIVE)));
    }

    @Test
    @Story("getById merges user groups from separate fetch")
    @Severity(SeverityLevel.NORMAL)
    void getUserById_mergesGroupsFromSeparateFetch() {
        User base = Allure.step("Create user with role", () -> new User());
        base.setId(22L);
        Role role = Allure.step("Create role ADMIN", () -> new Role());
        role.setName("ADMIN");
        base.setRoles(List.of(role));

        User withGroups = Allure.step("Create user with group", () -> new User());
        withGroups.setId(22L);
        UserGroup group = Allure.step("Create group Ops", () -> new UserGroup());
        group.setName("Ops");
        withGroups.setUserGroups(List.of(group));

        Allure.step("Mock user repository to return user with role",
                () -> when(userRepository.findWithRolesAndUserGroups(22L)).thenReturn(Optional.of(base)));
        Allure.step("Mock user repository to return user with group",
                () -> when(userRepository.findWithUserGroups(22L)).thenReturn(Optional.of(withGroups)));

        Optional<UserDto> result = Allure.step("Fetch user by ID 22", () -> service.getUserById(22L));
        Allure.step("Verify user DTO is present", () -> assertTrue(result.isPresent()));
        Allure.step("Verify user DTO has 1 role", () -> assertEquals(1, result.get().getRoles().size()));
        Allure.step("Verify user DTO has 1 group", () -> assertEquals(1, result.get().getUserGroups().size()));
        Allure.step("Verify user DTO group name is Ops",
                () -> assertEquals("Ops", result.get().getUserGroups().get(0).getName()));
    }

    @Test
    @Story("Activate user sets ACTIVE status")
    @Severity(SeverityLevel.NORMAL)
    void activateUser_setsActive() {
        User user = Allure.step("Create inactive user", () -> new User());
        user.setId(31L);
        user.setUserStatus(UserStatus.INACTIVE);
        Allure.step("Mock user repository to return inactive user",
                () -> when(userRepository.findById(31L)).thenReturn(Optional.of(user)));

        Allure.step("Activate user 31", () -> service.activateUser(31L));
        Allure.step("Verify user repository save is called with active status",
                () -> verify(userRepository).save(argThat(u -> u.getUserStatus() == UserStatus.ACTIVE)));
    }

    @Test
    @Story("Deactivate user sets INACTIVE status")
    @Severity(SeverityLevel.NORMAL)
    void deactivateUser_setsInactive() {
        User user = Allure.step("Create active user", () -> new User());
        user.setId(32L);
        user.setUserStatus(UserStatus.ACTIVE);
        Allure.step("Mock user repository to return active user",
                () -> when(userRepository.findById(32L)).thenReturn(Optional.of(user)));

        Allure.step("Deactivate user 32", () -> service.deactivateUser(32L));
        Allure.step("Verify user repository save is called with inactive status",
                () -> verify(userRepository).save(argThat(u -> u.getUserStatus() == UserStatus.INACTIVE)));
    }

    @Test
    @Story("Lock user sets accountLockedUntil")
    @Severity(SeverityLevel.NORMAL)
    void lockUser_setsLockUntil() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(33L);
        Allure.step("Mock user repository to return user",
                () -> when(userRepository.findById(33L)).thenReturn(Optional.of(user)));
        LocalDateTime until = LocalDateTime.now().plusHours(1);

        Allure.step("Lock user 33 until " + until, () -> service.lockUser(33L, until));
        Allure.step("Verify user repository save is called with lock until " + until,
                () -> verify(userRepository).save(argThat(u -> until.equals(u.getAccountLockedUntil()))));
    }

    @Test
    @Story("Unlock user clears lock and resets attempts")
    @Severity(SeverityLevel.NORMAL)
    void unlockUser_clearsLockAndResetsAttempts() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(34L);
        user.setFailedLoginAttempts(3);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(10));
        Allure.step("Mock user repository to return user",
                () -> when(userRepository.findById(34L)).thenReturn(Optional.of(user)));

        Allure.step("Unlock user 34", () -> service.unlockUser(34L));
        Allure.step("Verify user repository save is called with null lock until and 0 attempts",
                () -> verify(userRepository)
                        .save(argThat(u -> u.getAccountLockedUntil() == null && u.getFailedLoginAttempts() == 0)));
    }

    @Test
    @Story("Verify email sets flag true")
    @Severity(SeverityLevel.TRIVIAL)
    void verifyEmail_setsFlag() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(35L);
        user.setEmailVerified(false);
        Allure.step("Mock user repository to return user",
                () -> when(userRepository.findById(35L)).thenReturn(Optional.of(user)));

        Allure.step("Verify email for user 35", () -> service.verifyEmail(35L));
        Allure.step("Verify user repository save is called with email verified true",
                () -> verify(userRepository).save(argThat(u -> Boolean.TRUE.equals(u.getEmailVerified()))));
    }

    @Test
    @Story("Update last login uses native query")
    @Severity(SeverityLevel.TRIVIAL)
    void updateLastLogin_usesNative() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(36L);
        Allure.step("Mock user repository to return user",
                () -> when(userRepository.findById(36L)).thenReturn(Optional.of(user)));

        Allure.step("Update last login for user 36", () -> service.updateLastLogin(36L));
        Allure.step("Verify user repository update last login native is called with user 36 and any timestamp",
                () -> verify(userRepository).updateLastLoginNative(eq(36L), any(LocalDateTime.class)));
    }

    @Test
    @Story("Reset failed login attempts uses native query")
    @Severity(SeverityLevel.TRIVIAL)
    void resetFailedLoginAttempts_usesNative() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(37L);
        Allure.step("Mock user repository to return user",
                () -> when(userRepository.findById(37L)).thenReturn(Optional.of(user)));

        Allure.step("Reset failed login attempts for user 37", () -> service.resetFailedLoginAttempts(37L));
        Allure.step("Verify user repository reset failed login attempts native is called with user 37",
                () -> verify(userRepository).resetFailedLoginAttemptsNative(eq(37L)));
    }

    @Test
    @Story("Exists by username/email")
    @Severity(SeverityLevel.TRIVIAL)
    void existsByUsernameAndEmail_checksRepo() {
        Allure.step("Mock user repository to return true for username 'john'",
                () -> when(userRepository.existsByUsername("john")).thenReturn(true));
        Allure.step("Mock user repository to return false for email 'john@example.com'",
                () -> when(userRepository.existsByEmail("john@example.com")).thenReturn(false));

        Allure.step("Check user repository exists by username 'john'",
                () -> assertTrue(service.existsByUsername("john")));
        Allure.step("Check user repository exists by email 'john@example.com'",
                () -> assertFalse(service.existsByEmail("john@example.com")));
    }

    @Test
    @Story("Assign and remove roles")
    @Severity(SeverityLevel.NORMAL)
    void assignAndRemoveRoles_updatesUser() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(40L);
        user.setRoles(new java.util.ArrayList<>());
        Allure.step("Mock user repository to return user with roles",
                () -> when(userRepository.findWithRoles(40L)).thenReturn(Optional.of(user)));

        Role r1 = Allure.step("Create role ADMIN", () -> new Role());
        r1.setId(1L);
        r1.setName("ADMIN");
        Role r2 = Allure.step("Create role USER", () -> new Role());
        r2.setId(2L);
        r2.setName("USER");
        Allure.step("Mock role repository to return roles",
                () -> when(roleRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(r1, r2)));

        Allure.step("Mock user repository to return user",
                () -> when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0)));

        UserDto afterAssign = Allure.step("Assign roles 1, 2 to user 40",
                () -> service.assignRoles(40L, List.of(1L, 2L)));
        assertEquals(2, afterAssign.getRoles().size());

        // Now remove one role
        user.setRoles(new java.util.ArrayList<>(List.of(r1, r2)));
        Allure.step("Mock user repository to return user with roles",
                () -> when(userRepository.findWithRoles(40L)).thenReturn(Optional.of(user)));
        Allure.step("Mock role repository to return role 2 for removal",
                () -> when(roleRepository.findAllById(List.of(2L))).thenReturn(List.of(r2)));
        UserDto afterRemove = Allure.step("Remove role 2 from user 40",
                () -> service.removeRoles(40L, List.of(2L)));
        assertEquals(1, afterRemove.getRoles().size());
    }

    @Test
    @Story("Assign and remove user groups")
    @Severity(SeverityLevel.NORMAL)
    void assignAndRemoveUserGroups_updatesUser() {
        User user = Allure.step("Create user", () -> new User());
        user.setId(41L);
        user.setUserGroups(new java.util.ArrayList<>());
        Allure.step("Mock user repository to return user with groups",
                () -> when(userRepository.findWithUserGroups(41L)).thenReturn(Optional.of(user)));

        UserGroup g1 = Allure.step("Create user group Ops", () -> new UserGroup());
        g1.setId(11L);
        g1.setName("Ops");
        UserGroup g2 = Allure.step("Create user group HR", () -> new UserGroup());
        g2.setId(12L);
        g2.setName("HR");
        Allure.step("Mock user group repository to return groups",
                () -> when(userGroupRepository.findAllById(List.of(11L, 12L))).thenReturn(List.of(g1, g2)));

        Allure.step("Mock user repository to return user",
                () -> when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0)));

        UserDto afterAssign = Allure.step("Assign user groups 11, 12 to user 41",
                () -> service.assignUserGroups(41L, List.of(11L, 12L)));
        assertEquals(2, afterAssign.getUserGroups().size());

        // Now remove one group
        user.setUserGroups(new java.util.ArrayList<>(List.of(g1, g2)));
        Allure.step("Mock user repository to return user with groups",
                () -> when(userRepository.findWithUserGroups(41L)).thenReturn(Optional.of(user)));
        Allure.step("Mock user group repository to return group 12 for removal",
                () -> when(userGroupRepository.findAllById(List.of(12L))).thenReturn(List.of(g2)));
        UserDto afterRemove = Allure.step("Remove user group 12 from user 41",
                () -> service.removeUserGroups(41L, List.of(12L)));
        assertEquals(1, afterRemove.getUserGroups().size());
    }
}