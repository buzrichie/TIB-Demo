package com.amalitech.tib.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A utility component for constructing trip invitation URLs. It combines a configured base URL with
 * a unique invitation token to create a full, clickable link.
 */
@Component
public class TripInviteLinkBuilder {

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  /**
   * Builds a full invitation URL from a given token.
   *
   * @param token The unique invitation token.
   * @return The complete URL for the trip invitation, or null if the token is null or blank.
   */
  public String build(String token) {
    if (token == null || token.isBlank()) return null;
    String base = baseUrl != null ? baseUrl.trim() : "http://localhost:8080";
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/api/v1/trips/invite?token=" + token;
  }
}
