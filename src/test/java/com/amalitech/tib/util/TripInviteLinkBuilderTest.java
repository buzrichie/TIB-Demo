package com.amalitech.tib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TripInviteLinkBuilderTest {

  private TripInviteLinkBuilder tripInviteLinkBuilder;

  @BeforeEach
  void setUp() {
    tripInviteLinkBuilder = new TripInviteLinkBuilder();
    ReflectionTestUtils.setField(tripInviteLinkBuilder, "baseUrl", "http://localhost:8080");
  }

  @Test
  void testBuildWithValidToken() {
    String token = "valid-token";
    String expectedUrl = "http://localhost:8080/api/v1/trips/invite?token=valid-token";
    String actualUrl = tripInviteLinkBuilder.build(token);
    assertEquals(expectedUrl, actualUrl);
  }

  @Test
  void testBuildWithNullToken() {
    String actualUrl = tripInviteLinkBuilder.build(null);
    assertNull(actualUrl);
  }

  @Test
  void testBuildWithBlankToken() {
    String actualUrl = tripInviteLinkBuilder.build("   ");
    assertNull(actualUrl);
  }

  @Test
  void testBuildWithBaseUrlWithTrailingSlash() {
    ReflectionTestUtils.setField(tripInviteLinkBuilder, "baseUrl", "http://localhost:8080/");
    String token = "valid-token";
    String expectedUrl = "http://localhost:8080/api/v1/trips/invite?token=valid-token";
    String actualUrl = tripInviteLinkBuilder.build(token);
    assertEquals(expectedUrl, actualUrl);
  }

  @Test
  void testBuildWithNullBaseUrl() {
    ReflectionTestUtils.setField(tripInviteLinkBuilder, "baseUrl", null);
    String token = "valid-token";
    String expectedUrl = "http://localhost:8080/api/v1/trips/invite?token=valid-token";
    String actualUrl = tripInviteLinkBuilder.build(token);
    assertEquals(expectedUrl, actualUrl);
  }
}
