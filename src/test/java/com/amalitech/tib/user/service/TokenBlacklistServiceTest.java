package com.amalitech.tib.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.user.model.TokenBlacklist;
import com.amalitech.tib.user.repository.TokenBlacklistRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

  @Mock private TokenBlacklistRepository repository;

  @InjectMocks private TokenBlacklistService tokenBlacklistService;

  private final String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
  private final String reason = "USER_LOGOUT";
  private Instant futureExpiry;
  private Instant pastExpiry;

  @BeforeEach
  void setUp() {
    futureExpiry = Instant.now().plusSeconds(3600);
    pastExpiry = Instant.now().minusSeconds(3600);
  }

  @Test
  @DisplayName("Should blacklist token when token does not exist in blacklist")
  void blacklistToken_WhenTokenNotBlacklisted_ShouldSaveNewEntry() {

    when(repository.existsByToken(validToken)).thenReturn(false);
    when(repository.save(any(TokenBlacklist.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    tokenBlacklistService.blacklistToken(validToken, futureExpiry, reason);

    verify(repository, times(1)).existsByToken(validToken);
    verify(repository, times(1)).save(any(TokenBlacklist.class));
  }

  @Test
  @DisplayName("Should not blacklist token when token already exists in blacklist")
  void blacklistToken_WhenTokenAlreadyBlacklisted_ShouldNotSave() {

    when(repository.existsByToken(validToken)).thenReturn(true);

    tokenBlacklistService.blacklistToken(validToken, futureExpiry, reason);

    verify(repository, times(1)).existsByToken(validToken);
    verify(repository, never()).save(any(TokenBlacklist.class));
  }

  @Test
  @DisplayName("Should return true when checking if blacklisted token exists")
  void isTokenBlacklisted_WhenTokenExists_ShouldReturnTrue() {

    when(repository.existsByToken(validToken)).thenReturn(true);

    boolean result = tokenBlacklistService.isTokenBlacklisted(validToken);

    assertTrue(result, "Should return true for blacklisted token");
    verify(repository, times(1)).existsByToken(validToken);
  }

  @Test
  @DisplayName("Should return false when checking if non-blacklisted token exists")
  void isTokenBlacklisted_WhenTokenNotExists_ShouldReturnFalse() {

    when(repository.existsByToken(validToken)).thenReturn(false);

    boolean result = tokenBlacklistService.isTokenBlacklisted(validToken);

    assertFalse(result, "Should return false for non-blacklisted token");
    verify(repository, times(1)).existsByToken(validToken);
  }

  @Test
  @DisplayName("Should remove expired tokens when they exist in repository")
  void removeExpiredTokens_WhenExpiredTokensExist_ShouldDeleteThem() {

    TokenBlacklist expiredToken1 =
        TokenBlacklist.builder().token("expired1").expiresAt(pastExpiry).build();

    TokenBlacklist expiredToken2 =
        TokenBlacklist.builder().token("expired2").expiresAt(pastExpiry).build();

    TokenBlacklist validToken =
        TokenBlacklist.builder().token("valid").expiresAt(futureExpiry).build();

    when(repository.findAll()).thenReturn(List.of(expiredToken1, expiredToken2, validToken));

    tokenBlacklistService.removeExpiredTokens();

    verify(repository, times(1)).findAll();
    verify(repository, times(1)).delete(expiredToken1);
    verify(repository, times(1)).delete(expiredToken2);
    verify(repository, never()).delete(validToken);
  }

  @Test
  @DisplayName("Should not remove any tokens when no expired tokens exist")
  void removeExpiredTokens_WhenNoExpiredTokens_ShouldNotDeleteAny() {

    TokenBlacklist validToken1 =
        TokenBlacklist.builder().token("valid1").expiresAt(futureExpiry).build();

    TokenBlacklist validToken2 =
        TokenBlacklist.builder().token("valid2").expiresAt(futureExpiry.plusSeconds(1800)).build();

    when(repository.findAll()).thenReturn(List.of(validToken1, validToken2));

    tokenBlacklistService.removeExpiredTokens();

    verify(repository, times(1)).findAll();
    verify(repository, never()).delete(any(TokenBlacklist.class));
  }

  @Test
  @DisplayName("Should handle empty token list when removing expired tokens")
  void removeExpiredTokens_WhenNoTokens_ShouldNotDeleteAny() {

    when(repository.findAll()).thenReturn(List.of());

    tokenBlacklistService.removeExpiredTokens();

    verify(repository, times(1)).findAll();
    verify(repository, never()).delete(any(TokenBlacklist.class));
  }

  @Test
  @DisplayName("Should return valid token when token is not blacklisted and not null/blank")
  void validateRequestToken_WithValidNonBlacklistedToken_ShouldReturnToken() {

    when(repository.existsByToken(validToken)).thenReturn(false);

    String result = tokenBlacklistService.validateRequestToken(validToken);

    assertEquals(validToken, result, "Should return the same valid token");
    verify(repository, times(1)).existsByToken(validToken);
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when token is null")
  void validateRequestToken_WithNullToken_ShouldThrowException() {

    String nullToken = null;

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> tokenBlacklistService.validateRequestToken(nullToken),
            "Should throw InvalidTokenException for null token");

    assertEquals("Token is missing.", exception.getMessage());
    verify(repository, never()).existsByToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when token is empty string")
  void validateRequestToken_WithEmptyToken_ShouldThrowException() {

    String emptyToken = "";

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> tokenBlacklistService.validateRequestToken(emptyToken),
            "Should throw InvalidTokenException for empty token");

    assertEquals("Token is missing.", exception.getMessage());
    verify(repository, never()).existsByToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when token is blank string")
  void validateRequestToken_WithBlankToken_ShouldThrowException() {

    String blankToken = "   ";

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> tokenBlacklistService.validateRequestToken(blankToken),
            "Should throw InvalidTokenException for blank token");

    assertEquals("Token is missing.", exception.getMessage());
    verify(repository, never()).existsByToken(anyString());
  }

  @Test
  @DisplayName("Should throw InvalidTokenException when token is blacklisted")
  void validateRequestToken_WithBlacklistedToken_ShouldThrowException() {

    when(repository.existsByToken(validToken)).thenReturn(true);

    InvalidTokenException exception =
        assertThrows(
            InvalidTokenException.class,
            () -> tokenBlacklistService.validateRequestToken(validToken),
            "Should throw InvalidTokenException for blacklisted token");

    assertEquals("Token is expired — please log in again", exception.getMessage());
    verify(repository, times(1)).existsByToken(validToken);
  }

  @Test
  @DisplayName(
      "✅ Should handle token with whitespace — service checks blacklist using original untrimmed token")
  void validateRequestToken_WithWhitespace_ShouldUseOriginalToken() {

    String tokenWithWhitespace = "  " + validToken + "  ";
    when(repository.existsByToken(tokenWithWhitespace)).thenReturn(false);

    String result = tokenBlacklistService.validateRequestToken(tokenWithWhitespace);

    assertEquals(
        tokenWithWhitespace,
        result,
        "Service should return the original token (including whitespace)");
    verify(repository, times(1)).existsByToken(tokenWithWhitespace);
  }

  @Test
  @DisplayName("Should handle multiple consecutive calls to blacklist token check")
  void isTokenBlacklisted_MultipleCalls_ShouldCheckRepositoryEachTime() {

    when(repository.existsByToken(validToken)).thenReturn(true, false);

    boolean firstCall = tokenBlacklistService.isTokenBlacklisted(validToken);
    boolean secondCall = tokenBlacklistService.isTokenBlacklisted(validToken);

    assertTrue(firstCall, "First call should return true");
    assertFalse(secondCall, "Second call should return false");
    verify(repository, times(2)).existsByToken(validToken);
  }

  @Test
  @DisplayName("Should handle token with special characters in blacklist operations")
  void blacklistToken_WithSpecialCharacters_ShouldHandleCorrectly() {

    String tokenWithSpecialChars = "token!@#$%^&*()_+-=[]{}|;:,.<>?";
    when(repository.existsByToken(tokenWithSpecialChars)).thenReturn(false);
    when(repository.save(any(TokenBlacklist.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    tokenBlacklistService.blacklistToken(tokenWithSpecialChars, futureExpiry, reason);

    verify(repository, times(1)).existsByToken(tokenWithSpecialChars);
    verify(repository, times(1)).save(any(TokenBlacklist.class));
  }
}
