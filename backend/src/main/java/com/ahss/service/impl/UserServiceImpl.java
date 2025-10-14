package com.ahss.service.impl;

import com.ahss.dto.PermissionDto;
import com.ahss.dto.RoleDto;
import com.ahss.dto.UserDto;
import com.ahss.dto.UserGroupDto;
import com.ahss.entity.Permission;
import com.ahss.entity.Role;
import com.ahss.entity.User;
import com.ahss.entity.UserGroup;
import com.ahss.entity.UserStatus;
import com.ahss.repository.RoleRepository;
import com.ahss.repository.UserGroupRepository;
import com.ahss.repository.UserRepository;
import com.ahss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllActiveUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByStatus(UserStatus status) {
        return userRepository.findByUserStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        Optional<User> userOpt = userRepository.findWithRolesAndUserGroups(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Fetch user groups separately to avoid MultipleBagFetchException
            Optional<User> userWithGroups = userRepository.findWithUserGroups(id);
            if (userWithGroups.isPresent()) {
                user.setUserGroups(userWithGroups.get().getUserGroups());
            }
            return Optional.of(convertToDto(user));
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmailWithRoles(email)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .map(this::convertToDtoWithPassword);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        // Validate unique constraints
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username '" + userDto.getUsername() + "' already exists");
        }
        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDto.getEmail() + "' already exists");
        }

        User user = convertToEntity(userDto);
        
        // Encode password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        // Set default values
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPasswordChangedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Validate unique constraints (excluding current user)
        if (userRepository.existsByUsernameAndIdNot(userDto.getUsername(), id)) {
            throw new IllegalArgumentException("Username '" + userDto.getUsername() + "' already exists");
        }
        
        if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
            throw new IllegalArgumentException("Email '" + userDto.getEmail() + "' already exists");
        }

        // Update fields
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhoneNumber(userDto.getPhoneNumber());
        
        if (userDto.getUserStatus() != null) {
            existingUser.setUserStatus(userDto.getUserStatus());
        }
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        existingUser.setUpdatedBy(userDto.getUpdatedBy() != null ? userDto.getUpdatedBy() : "system");

        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Soft delete by setting status to INACTIVE
        user.setUserStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setUserStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setUserStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void lockUser(Long id, LocalDateTime lockUntil) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setAccountLockedUntil(lockUntil);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setAccountLockedUntil(null);
        user.setFailedLoginAttempts(0);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void verifyEmail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void updateLastLogin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Use native query to avoid enum casting issues
        userRepository.updateLastLoginNative(id, LocalDateTime.now());
    }

    @Override
    public void incrementFailedLoginAttempts(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        attempts++;
        
        // Lock account after 5 failed attempts for 30 minutes
        LocalDateTime lockUntil = null;
        if (attempts >= 5) {
            lockUntil = LocalDateTime.now().plusMinutes(30);
        }
        
        // Use native query to avoid enum casting issues
        userRepository.updateFailedLoginAttemptsNative(id, attempts, lockUntil);
    }

    @Override
    public void resetFailedLoginAttempts(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Use native query to avoid enum casting issues
        userRepository.resetFailedLoginAttemptsNative(id);
    }

    @Override
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsernameAndIdNot(String username, Long id) {
        return userRepository.existsByUsernameAndIdNot(username, id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public UserDto assignRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findWithRoles(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }
        
        user.getRoles().addAll(roles);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public UserDto removeRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findWithRoles(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<Role> rolesToRemove = roleRepository.findAllById(roleIds);
        user.getRoles().removeAll(rolesToRemove);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public UserDto assignUserGroups(Long userId, List<Long> userGroupIds) {
        User user = userRepository.findWithUserGroups(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<UserGroup> userGroups = userGroupRepository.findAllById(userGroupIds);
        if (userGroups.size() != userGroupIds.size()) {
            throw new IllegalArgumentException("One or more user groups not found");
        }
        
        user.getUserGroups().addAll(userGroups);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public UserDto removeUserGroups(Long userId, List<Long> userGroupIds) {
        User user = userRepository.findWithUserGroups(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        List<UserGroup> userGroupsToRemove = userGroupRepository.findAllById(userGroupIds);
        user.getUserGroups().removeAll(userGroupsToRemove);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getLockedUsers() {
        return userRepository.findLockedUsers(LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUnverifiedUsers() {
        return userRepository.findUnverifiedUsers()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getInactiveUsers(LocalDateTime cutoffDate) {
        return userRepository.findInactiveUsers(cutoffDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findUsersCreatedBetween(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper methods for conversion
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // Don't include password in DTO for security
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUserStatus(user.getUserStatus());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setLastLogin(user.getLastLogin());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setAccountLockedUntil(user.getAccountLockedUntil());
        dto.setPasswordChangedAt(user.getPasswordChangedAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());
        
        // Convert roles
        if (user.getRoles() != null) {
            List<RoleDto> roleDtos = user.getRoles().stream()
                    .map(this::convertRoleToDto)
                    .collect(Collectors.toList());
            dto.setRoles(roleDtos);
        }
        
        // Convert user groups
        if (user.getUserGroups() != null) {
            List<UserGroupDto> userGroupDtos = user.getUserGroups().stream()
                    .map(this::convertUserGroupToDto)
                    .collect(Collectors.toList());
            dto.setUserGroups(userGroupDtos);
        }
        
        return dto;
    }
    
    // Special conversion method that includes password for authentication
    private UserDto convertToDtoWithPassword(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword()); // Include password for authentication
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUserStatus(user.getUserStatus());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setLastLogin(user.getLastLogin());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setAccountLockedUntil(user.getAccountLockedUntil());
        dto.setPasswordChangedAt(user.getPasswordChangedAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());
        
        // Convert roles
        if (user.getRoles() != null) {
            List<RoleDto> roleDtos = user.getRoles().stream()
                    .map(this::convertRoleToDto)
                    .collect(Collectors.toList());
            dto.setRoles(roleDtos);
        }
        
        // Convert user groups
        if (user.getUserGroups() != null) {
            List<UserGroupDto> userGroupDtos = user.getUserGroups().stream()
                    .map(this::convertUserGroupToDto)
                    .collect(Collectors.toList());
            dto.setUserGroups(userGroupDtos);
        }
        
        return dto;
    }

    private RoleDto convertRoleToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setRoleStatus(role.getRoleStatus());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        
        // Convert permissions
        if (role.getPermissions() != null) {
            List<PermissionDto> permissionDtos = role.getPermissions().stream()
                    .map(this::convertPermissionToDto)
                    .collect(Collectors.toList());
            dto.setPermissions(permissionDtos);
        }
        
        return dto;
    }

    private PermissionDto convertPermissionToDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setResourceType(permission.getResourceType());
        dto.setAction(permission.getAction());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        
        // Include module information if permission has a module
        if (permission.getModule() != null) {
            dto.setModuleId(permission.getModule().getId());
            dto.setModuleName(permission.getModule().getName());
        }
        
        return dto;
    }

    private UserGroupDto convertUserGroupToDto(UserGroup userGroup) {
        UserGroupDto dto = new UserGroupDto();
        dto.setId(userGroup.getId());
        dto.setName(userGroup.getName());
        dto.setDescription(userGroup.getDescription());
        dto.setCreatedAt(userGroup.getCreatedAt());
        dto.setUpdatedAt(userGroup.getUpdatedAt());
        dto.setCreatedBy(userGroup.getCreatedBy());
        dto.setUpdatedBy(userGroup.getUpdatedBy());
        dto.setDeletedAt(userGroup.getDeletedAt());
        
        // Convert roles
        if (userGroup.getRoles() != null) {
            List<RoleDto> roleDtos = userGroup.getRoles().stream()
                    .map(this::convertRoleToDto)
                    .collect(Collectors.toList());
            dto.setRoles(roleDtos);
        }
        
        return dto;
    }

    private User convertToEntity(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        
        // Set userStatus from DTO if provided, otherwise default to ACTIVE
        if (dto.getUserStatus() != null) {
            user.setUserStatus(dto.getUserStatus());
        } else {
            user.setUserStatus(UserStatus.ACTIVE);
        }
        
        // Set emailVerified from DTO if provided, otherwise default to false
        if (dto.getEmailVerified() != null) {
            user.setEmailVerified(dto.getEmailVerified());
        } else {
            user.setEmailVerified(false);
        }
        
        user.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "system");
        user.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : "system");
        
        return user;
    }
}