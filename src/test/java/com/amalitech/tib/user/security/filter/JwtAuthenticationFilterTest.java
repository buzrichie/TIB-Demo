package com.amalitech.tib.user.security.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.user.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private TokenBlacklistService tokenBlacklistService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldSkipAuthenticationForExcludedPaths() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtTokenProvider, never()).validateToken(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldSkipWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/v1/users");
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtTokenProvider, never()).validateToken(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldSkipWhenTokenIsInvalid() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/v1/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
    when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldSkipWhenTokenIsBlacklisted() throws ServletException, IOException {
    String token = "blacklisted-token";

    when(request.getRequestURI()).thenReturn("/api/v1/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldAuthenticateValidToken() throws ServletException, IOException {
    String token = "valid-token";

    when(request.getRequestURI()).thenReturn("/api/v1/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token)).thenReturn(true);
    when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);
    when(jwtTokenProvider.getSubject(token)).thenReturn("1234");
    when(jwtTokenProvider.getEmail(token)).thenReturn("user@example.com");
    when(jwtTokenProvider.getRoles(token)).thenReturn(List.of("USER"));
    when(jwtTokenProvider.getPermissions(token)).thenReturn(List.of("READ_PRIVILEGE"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);
    assertEquals("user@example.com", auth.getPrincipal());
    assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    assertTrue(
        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("READ_PRIVILEGE")));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldHandleExpiredJwtException() throws ServletException, IOException {
    String token = "expired-token";

    when(request.getRequestURI()).thenReturn("/api/v1/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtTokenProvider.validateToken(token))
        .thenThrow(new ExpiredJwtException(null, null, "expired"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}
