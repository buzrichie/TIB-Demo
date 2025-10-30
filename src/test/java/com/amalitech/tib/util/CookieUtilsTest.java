package com.amalitech.tib.util;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {

  private CookieUtils cookieUtils;

  @BeforeEach
  void setUp() {
    cookieUtils = new CookieUtils();
  }

  @Test
  @DisplayName("Should create HTTP-only cookie with all attributes correctly set")
  void createHttpOnlyCookie_WithValidParameters_ReturnsProperlyConfiguredCookie() {

    String cookieName = "authToken";
    String cookieValue = "encrypted-token-value";
    Long maxAgeSeconds = 3600000L;
    int expectedMaxAge = 3600;

    Cookie result = cookieUtils.createHttpOnlyCookie(cookieName, cookieValue, maxAgeSeconds);

    assertNotNull(result, "Created cookie should not be null");
    assertEquals(cookieName, result.getName(), "Cookie name should match input");
    assertEquals(cookieValue, result.getValue(), "Cookie value should match input");
    assertTrue(result.isHttpOnly(), "Cookie should be HTTP-only");
    assertTrue(result.getSecure(), "Cookie should be secure (HTTPS only)");
    assertEquals("/", result.getPath(), "Cookie path should be root");
    assertEquals(
        expectedMaxAge,
        result.getMaxAge(),
        "Cookie max age should be correctly converted from milliseconds to seconds");
  }

  @Test
  @DisplayName("Should handle zero max age by creating session cookie")
  void createHttpOnlyCookie_WithZeroMaxAge_ReturnsSessionCookie() {

    String cookieName = "sessionToken";
    String cookieValue = "session-value";
    Long maxAgeSeconds = 0L;
    int expectedMaxAge = 0;

    Cookie result = cookieUtils.createHttpOnlyCookie(cookieName, cookieValue, maxAgeSeconds);

    assertNotNull(result, "Cookie should be created even with zero max age");
    assertEquals(
        expectedMaxAge, result.getMaxAge(), "Cookie max age should be zero for session cookie");
    assertEquals(cookieName, result.getName(), "Cookie name should be preserved");
    assertEquals(cookieValue, result.getValue(), "Cookie value should be preserved");
  }

  @Test
  @DisplayName("Should handle null cookie value by creating cookie with null value")
  void createHttpOnlyCookie_WithNullValue_ReturnsCookieWithNullValue() {

    String cookieName = "userPref";
    String cookieValue = null;
    Long maxAgeSeconds = 86400000L;

    Cookie result = cookieUtils.createHttpOnlyCookie(cookieName, cookieValue, maxAgeSeconds);

    assertNotNull(result, "Cookie should be created even with null value");
    assertNull(result.getValue(), "Cookie value should be null when input value is null");
    assertEquals(cookieName, result.getName(), "Cookie name should be correctly set");
    assertTrue(result.isHttpOnly(), "Security attributes should still be set");
  }

  @Test
  @DisplayName("Should handle empty string cookie value correctly")
  void createHttpOnlyCookie_WithEmptyStringValue_ReturnsCookieWithEmptyValue() {

    String cookieName = "emptyCookie";
    String cookieValue = "";
    Long maxAgeSeconds = 1800000L;

    Cookie result = cookieUtils.createHttpOnlyCookie(cookieName, cookieValue, maxAgeSeconds);

    assertNotNull(result, "Cookie should be created with empty string value");
    assertEquals("", result.getValue(), "Cookie value should be empty string");
    assertEquals(cookieName, result.getName(), "Cookie name should be correctly set");
  }

  @Test
  @DisplayName("Should correctly convert max age from milliseconds to seconds with rounding")
  void createHttpOnlyCookie_WithNonDivisibleMaxAge_ReturnsRoundedMaxAge() {

    String cookieName = "testCookie";
    String cookieValue = "test-value";
    Long maxAgeMillis = 1500L;
    int expectedMaxAge = 1;

    Cookie result = cookieUtils.createHttpOnlyCookie(cookieName, cookieValue, maxAgeMillis);

    assertEquals(
        expectedMaxAge,
        result.getMaxAge(),
        "Max age should be rounded down when converting from milliseconds to seconds");
  }

  @Test
  @DisplayName("Should handle negative max age by creating expired cookie")
  void createHttpOnlyCookie_WithNegativeMaxAge_ReturnsExpiredCookie() {

    String cookieName = "expiredCookie";
    String cookieValue = "expired-value";
    Long maxAgeMillis = -1000L;
    int expectedMaxAge = -1;

    Cookie result = cookieUtils.createHttpOnlyCookie(cookieName, cookieValue, maxAgeMillis);

    assertEquals(
        expectedMaxAge,
        result.getMaxAge(),
        "Negative max age should be converted to negative seconds");
  }

  @Test
  @DisplayName("Should create expiration cookie with all attributes properly set")
  void expireCookie_WhenCalled_ReturnsProperlyConfiguredExpirationCookie() {

    Cookie result = cookieUtils.expireCookie();

    assertNotNull(result, "Expiration cookie should not be null");
    assertEquals(
        "refreshToken", result.getName(), "Expiration cookie should have name 'refreshToken'");
    assertNull(result.getValue(), "Expiration cookie value should be null");
    assertEquals(
        0, result.getMaxAge(), "Expiration cookie max age should be 0 (immediate expiration)");
    assertTrue(result.isHttpOnly(), "Expiration cookie should be HTTP-only");
    assertTrue(result.getSecure(), "Expiration cookie should be secure (HTTPS only)");
    assertEquals("/", result.getPath(), "Expiration cookie path should be root");
  }

  @Test
  @DisplayName("Should verify expiration cookie immediately expires when set in browser")
  void expireCookie_WithZeroMaxAge_EnsuresImmediateBrowserExpiration() {

    Cookie result = cookieUtils.expireCookie();

    assertEquals(
        0,
        result.getMaxAge(),
        "Max age of 0 should instruct browser to immediately expire the cookie");
    assertNull(result.getValue(), "Null value ensures no sensitive data remains in the cookie");
  }

  @Test
  @DisplayName("Should maintain security attributes in expiration cookie")
  void expireCookie_SecurityAttributes_AreProperlySet() {

    Cookie result = cookieUtils.expireCookie();

    assertTrue(result.isHttpOnly(), "HTTP-only flag should prevent client-side script access");
    assertTrue(result.getSecure(), "Secure flag should ensure cookie is only sent over HTTPS");
    assertEquals(
        "/", result.getPath(), "Root path ensures cookie is accessible across entire application");
  }

  @Test
  @DisplayName("Should create multiple independent cookie instances")
  void createHttpOnlyCookie_MultipleCalls_ReturnIndependentInstances() {

    String name1 = "cookie1";
    String value1 = "value1";
    String name2 = "cookie2";
    String value2 = "value2";

    Cookie cookie1 = cookieUtils.createHttpOnlyCookie(name1, value1, 1000L);
    Cookie cookie2 = cookieUtils.createHttpOnlyCookie(name2, value2, 2000L);

    assertNotSame(cookie1, cookie2, "Each cookie should be a separate instance");
    assertEquals(name1, cookie1.getName());
    assertEquals(name2, cookie2.getName());
    assertNotEquals(cookie1.getName(), cookie2.getName(), "Cookies should have different names");
  }
}
