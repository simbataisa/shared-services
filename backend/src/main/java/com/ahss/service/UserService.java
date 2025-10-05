package com.ahss.service;

import com.ahss.dto.UserDto;
import com.ahss.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    List<UserDto> getAllActiveUsers();
    
    List<UserDto> getUsersByStatus(UserStatus status);
    
    Optional<UserDto> getUserById(Long id);
    
    Optional<UserDto> getUserByUsername(String username);
    
    Optional<UserDto> getUserByEmail(String email);
    
    Optional<UserDto> getUserByUsernameOrEmail(String usernameOrEmail);
    
    UserDto createUser(UserDto userDto);
    
    UserDto updateUser(Long id, UserDto userDto);
    
    void deleteUser(Long id);
    
    void activateUser(Long id);
    
    void deactivateUser(Long id);
    
    void lockUser(Long id, LocalDateTime lockUntil);
    
    void unlockUser(Long id);
    
    void verifyEmail(Long id);
    
    void updateLastLogin(Long id);
    
    void incrementFailedLoginAttempts(Long id);
    
    void resetFailedLoginAttempts(Long id);
    
    void changePassword(Long id, String newPassword);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsernameAndIdNot(String username, Long id);
    
    boolean existsByEmailAndIdNot(String email, Long id);
    
    UserDto assignRoles(Long userId, List<Long> roleIds);
    
    UserDto removeRoles(Long userId, List<Long> roleIds);
    
    UserDto assignUserGroups(Long userId, List<Long> userGroupIds);
    
    UserDto removeUserGroups(Long userId, List<Long> userGroupIds);
    
    List<UserDto> searchUsers(String searchTerm);
    
    List<UserDto> getLockedUsers();
    
    List<UserDto> getUnverifiedUsers();
    
    List<UserDto> getInactiveUsers(LocalDateTime cutoffDate);
    
    List<UserDto> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);
}