package com.amalitech.tib.util;

import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Utility class for security-related operations. */
@Component
@Slf4j
public class SecurityUtils {

  private static UserRepository userRepository;

  @Autowired
  public SecurityUtils(UserRepository userRepository) {
    SecurityUtils.userRepository = userRepository;
  }

  /**
   * Gets the current authenticated user's ID.
   *
   * @return The user's UUID
   * @throws IllegalStateException if user is not authenticated
   */
  public static UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new IllegalStateException("User is not authenticated");
    }

    String principal = authentication.getName();
    return parseOrFetchUserId(principal);
  }

  /**
   * Gets the current authenticated user's ID, or null if not authenticated. This is useful for
   * endpoints that support both authenticated and unauthenticated access.
   *
   * @return The user's UUID if authenticated, null otherwise
   */
  public static UUID getCurrentUserIdOrNull() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null
          || !authentication.isAuthenticated()
          || "anonymousUser".equals(authentication.getPrincipal())) {
        return null;
      }

      String principal = authentication.getName();
      return parseOrFetchUserId(principal);
    } catch (Exception e) {
      log.warn("Failed to get current user ID: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Parses the principal string as UUID if possible, otherwise fetches user ID by email.
   *
   * @param principal The principal string (either UUID or email)
   * @return The user's UUID
   */
  private static UUID parseOrFetchUserId(String principal) {
    try {
      return UUID.fromString(principal);
    } catch (IllegalArgumentException e) {
      log.debug("Principal is not a UUID, fetching user by email: {}", principal);
      return fetchUserIdByEmail(principal);
    }
  }

  /**
   * Fetches user ID from database using email.
   *
   * @param email The user's email
   * @return The user's UUID
   * @throws ResourceNotFoundException if user not found
   */
  private static UUID fetchUserIdByEmail(String email) {
    if (userRepository == null) {
      throw new IllegalStateException("UserRepository not initialized");
    }

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));
    return user.getId();
  }
}
