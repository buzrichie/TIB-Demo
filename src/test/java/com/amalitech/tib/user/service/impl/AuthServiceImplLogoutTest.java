package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.user.dto.RefreshResponseDto;
import com.amalitech.tib.user.model.RefreshToken;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.RefreshTokenRepository;
import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.user.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLogoutTest {

  @Mock private TokenBlacklistService tokenBlacklistService;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private AuthServiceImpl authService;

  private HttpServletRequest request;
  private String clientRefreshTokenValue = "client.refresh.token.value";
  private UUID userId;
  private User user;
  private RefreshToken refreshTokenEntity;
  private String oldAccessToken = "old.access.token";
  private String newAccessToken = "new.access.token";
  private long accessTokenExpiration = 3600L;

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class);
    userId = UUID.randomUUID();

    user = new User();
    user.setId(userId);

    refreshTokenEntity = new RefreshToken();
    refreshTokenEntity.setId(UUID.randomUUID());
    refreshTokenEntity.setToken(clientRefreshTokenValue);
    refreshTokenEntity.setIsRevoked(false);
    refreshTokenEntity.setUser(user);
  }

  @Test
  @DisplayName("Should successfully refresh access token and blacklist old one")
  void refreshAccessToken_Success() {

    when(tokenBlacklistService.validateRequestToken(clientRefreshTokenValue))
        .thenReturn(clientRefreshTokenValue);

    when(jwtTokenProvider.getSubject(clientRefreshTokenValue)).thenReturn(userId.toString());

    when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.of(refreshTokenEntity));

    when(jwtTokenProvider.validateToken(clientRefreshTokenValue)).thenReturn(true);

    when(jwtTokenProvider.generateAccessToken(user)).thenReturn(newAccessToken);

    when(jwtTokenProvider.extractJWTAuthTokenFromHeader(request)).thenReturn(oldAccessToken);
    when(jwtTokenProvider.getExpiration(oldAccessToken))
        .thenReturn(Instant.now().plusSeconds(accessTokenExpiration));

    RefreshResponseDto result = authService.refreshAccessToken(request, clientRefreshTokenValue);

    assertNotNull(result, "Result should not be null");
    assertEquals(newAccessToken, result.accessToken(), "New access token should be returned");
    assertEquals("Bearer", result.tokenType(), "Token type should be 'Bearer'");

    verify(tokenBlacklistService, times(1)).validateRequestToken(clientRefreshTokenValue);
    verify(refreshTokenRepository, times(1)).findByUserId(userId);
    verify(jwtTokenProvider, times(1)).validateToken(clientRefreshTokenValue);
    verify(jwtTokenProvider, times(1)).generateAccessToken(user);

    verify(tokenBlacklistService, times(1))
        .blacklistToken(eq(oldAccessToken), any(Instant.class), eq("refresh"));
  }

  @Test
  @DisplayName("Should throw InvalidTokenException if refresh token is missing (null)")
  void refreshAccessToken_MissingToken_Null() {

    assertThrows(
        InvalidTokenException.class,
        () -> authService.refreshAccessToken(request, null),
        "Should throw InvalidTokenException for null token");

    verify(tokenBlacklistService, never()).validateRequestToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException if refresh token is blank")
  void refreshAccessToken_MissingToken_Blank() {

    assertThrows(
        InvalidTokenException.class,
        () -> authService.refreshAccessToken(request, "  "),
        "Should throw InvalidTokenException for blank token");

    verify(tokenBlacklistService, never()).validateRequestToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException if no matching refresh token is found in DB")
  void refreshAccessToken_NoTokenInDb() {

    when(tokenBlacklistService.validateRequestToken(clientRefreshTokenValue))
        .thenReturn(clientRefreshTokenValue);
    when(jwtTokenProvider.getSubject(clientRefreshTokenValue)).thenReturn(userId.toString());

    when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> authService.refreshAccessToken(request, clientRefreshTokenValue),
            "Should throw InvalidTokenException when DB token is missing");

    assertEquals("No refresh token found for this user", exception.getMessage());

    verify(jwtTokenProvider, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException if refresh token is revoked")
  void refreshAccessToken_RevokedToken() {

    refreshTokenEntity.setIsRevoked(true);

    when(tokenBlacklistService.validateRequestToken(clientRefreshTokenValue))
        .thenReturn(clientRefreshTokenValue);
    when(jwtTokenProvider.getSubject(clientRefreshTokenValue)).thenReturn(userId.toString());
    when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.of(refreshTokenEntity));

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> authService.refreshAccessToken(request, clientRefreshTokenValue),
            "Should throw InvalidTokenException when token is revoked");

    assertEquals("Refresh token has been revoked — please log in again", exception.getMessage());

    verify(jwtTokenProvider, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException if refresh token is expired")
  void refreshAccessToken_ExpiredToken() {

    when(tokenBlacklistService.validateRequestToken(clientRefreshTokenValue))
        .thenReturn(clientRefreshTokenValue);
    when(jwtTokenProvider.getSubject(clientRefreshTokenValue)).thenReturn(userId.toString());
    when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.of(refreshTokenEntity));

    when(jwtTokenProvider.validateToken(clientRefreshTokenValue)).thenReturn(false);

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> authService.refreshAccessToken(request, clientRefreshTokenValue),
            "Should throw InvalidTokenException when token is expired");

    assertEquals("Refresh token expired — please log in again", exception.getMessage());

    verify(jwtTokenProvider, never()).generateAccessToken(any());
    verify(tokenBlacklistService, never())
        .blacklistToken(anyString(), any(Instant.class), anyString());
  }
}
