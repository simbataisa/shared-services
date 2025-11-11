package com.ahss.util;

import com.ahss.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {

  private SecurityUtil() {
    // Utility class, prevent instantiation
  }

  /**
   * Get the current authenticated user's ID from the security context.
   *
   * @return Optional containing the user ID if authenticated, empty otherwise
   */
  public static Optional<Long> getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserPrincipal) {
      return Optional.ofNullable(((UserPrincipal) principal).getUserId());
    }

    return Optional.empty();
  }

  /**
   * Get the current authenticated user's username from the security context.
   *
   * @return Optional containing the username if authenticated, empty otherwise
   */
  public static Optional<String> getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserPrincipal) {
      return Optional.ofNullable(((UserPrincipal) principal).getUsername());
    } else if (principal instanceof String) {
      return Optional.of((String) principal);
    }

    return Optional.empty();
  }

  /**
   * Get the current authenticated user's ID, or return a default value if not authenticated.
   *
   * @param defaultValue the default value to return if user is not authenticated
   * @return the user ID or default value
   */
  public static Long getCurrentUserIdOrDefault(Long defaultValue) {
    return getCurrentUserId().orElse(defaultValue);
  }

  /**
   * Check if there is a currently authenticated user.
   *
   * @return true if user is authenticated, false otherwise
   */
  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal());
  }
}
