package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amalitech.tib.user.dto.AuthResponseDto;
import com.amalitech.tib.user.dto.LoginRequestDto;
import com.amalitech.tib.user.dto.UserDto;
import com.amalitech.tib.user.mapper.UserMapper;
import com.amalitech.tib.user.model.RefreshToken;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.RefreshTokenRepository;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.security.UserDetailsImpl;
import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLoginTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private UserRepository userRepository;

  @Mock private CookieUtils cookieUtils;

  @Mock private UserMapper userMapper;

  @InjectMocks private AuthServiceImpl authService;

  private LoginRequestDto loginRequest;
  private User user;
  private UserDetailsImpl userDetails;
  private Authentication authentication;
  private HttpServletResponse response;
  private final String mockAccessToken = "mock.access.token";
  private final String mockRefreshTokenValue = "mock.refresh.token.value";
  private UserDto userDto;

  @BeforeEach
  void setUp() {

    loginRequest = new LoginRequestDto("test@example.com", "password123");

    user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(loginRequest.email());
    user.setLastActive(null);

    userDetails = new UserDetailsImpl(user);

    authentication = mock(Authentication.class);

    response = mock(HttpServletResponse.class);

    userDto =
        new UserDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            null,
            Instant.now(),
            null,
            null,
            Set.of(),
            null,
            null);
  }

  @Test
  @DisplayName("Should successfully log in and create a new refresh token")
  void login_Success_NewRefreshToken() {

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);

    when(jwtTokenProvider.generateAccessToken(user)).thenReturn(mockAccessToken);
    when(jwtTokenProvider.generateRefreshToken(user.getId().toString()))
        .thenReturn(mockRefreshTokenValue);

    when(refreshTokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

    Cookie mockCookie = new Cookie("refreshToken", mockRefreshTokenValue);
    when(cookieUtils.createHttpOnlyCookie(eq("refreshToken"), eq(mockRefreshTokenValue), anyLong()))
        .thenReturn(mockCookie);

    when(userMapper.toDto(any(User.class))).thenReturn(userDto);

    ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    AuthResponseDto result = authService.login(loginRequest, response);

    assertNotNull(result, "Result should not be null");
    assertEquals(mockAccessToken, result.accessToken(), "Access token should match");
    assertEquals("Bearer", result.tokenType(), "Token type should be 'Bearer'");
    assertEquals(userDto, result.user(), "User DTO should match the expected DTO");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

    verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
    RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();
    assertEquals(
        mockRefreshTokenValue,
        savedRefreshToken.getToken(),
        "Saved RefreshToken token value must match generated one");
    assertEquals(
        user.getId(),
        savedRefreshToken.getUser().getId(),
        "Saved RefreshToken must be linked to the correct User");
    assertEquals(false, savedRefreshToken.getIsRevoked(), "Saved RefreshToken must not be revoked");

    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertNotNull(savedUser.getLastActive(), "User's lastActive field must be updated");

    verify(cookieUtils)
        .createHttpOnlyCookie(eq("refreshToken"), eq(mockRefreshTokenValue), anyLong());
    verify(response).addCookie(mockCookie);
  }

  @Test
  @DisplayName("Should successfully log in and update an existing refresh token")
  void login_Success_ExistingRefreshToken() {

    RefreshToken existingRefreshToken = new RefreshToken();
    existingRefreshToken.setToken("old.refresh.token");
    existingRefreshToken.setUser(user);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);

    when(jwtTokenProvider.generateAccessToken(user)).thenReturn(mockAccessToken);
    when(jwtTokenProvider.generateRefreshToken(user.getId().toString()))
        .thenReturn(mockRefreshTokenValue);

    when(refreshTokenRepository.findByUserId(user.getId()))
        .thenReturn(Optional.of(existingRefreshToken));

    Cookie mockCookie = new Cookie("refreshToken", mockRefreshTokenValue);
    when(cookieUtils.createHttpOnlyCookie(eq("refreshToken"), eq(mockRefreshTokenValue), anyLong()))
        .thenReturn(mockCookie);

    when(userMapper.toDto(any(User.class))).thenReturn(userDto);

    ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

    AuthResponseDto result = authService.login(loginRequest, response);

    assertNotNull(result, "Result should not be null");
    assertEquals(mockAccessToken, result.accessToken(), "Access token should match");

    verify(refreshTokenRepository, times(1)).save(refreshTokenCaptor.capture());
    RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

    assertEquals(
        existingRefreshToken,
        savedRefreshToken,
        "The existing RefreshToken object should have been modified and saved");

    assertEquals(
        mockRefreshTokenValue,
        savedRefreshToken.getToken(),
        "Existing RefreshToken token value must be updated");
    assertEquals(
        false, savedRefreshToken.getIsRevoked(), "Existing RefreshToken must be unrevoked");

    verify(userRepository, times(1)).save(any(User.class));
    verify(response, times(1)).addCookie(mockCookie);
  }

  @Test
  @DisplayName("Should propagate exception on authentication failure")
  void login_Failure_Authentication() {

    final String expectedErrorMessage = "Bad credentials";

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(
            new org.springframework.security.authentication.BadCredentialsException(
                expectedErrorMessage));

    org.junit.jupiter.api.Assertions.assertThrows(
        org.springframework.security.authentication.BadCredentialsException.class,
        () -> authService.login(loginRequest, response),
        "Expected BadCredentialsException to be thrown");

    verify(jwtTokenProvider, times(0)).generateAccessToken(any());
    verify(userRepository, times(0)).save(any());
  }
}
