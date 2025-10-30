package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.user.dto.AuthResponseDto;
import com.amalitech.tib.user.dto.RegisterRequestDto;
import com.amalitech.tib.user.mapper.UserMapper;
import com.amalitech.tib.user.model.OTPToken;
import com.amalitech.tib.user.model.RefreshToken;
import com.amalitech.tib.user.model.Role;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.OTPTokenRepository;
import com.amalitech.tib.user.repository.RefreshTokenRepository;
import com.amalitech.tib.user.repository.RoleRepository;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.user.service.OTPVerificationService;
import com.amalitech.tib.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplRegisterUserTest {

  @Mock private OTPVerificationService otpVerificationService;
  @Mock private RoleRepository roleRepository;
  @Mock private UserRepository userRepository;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private OTPTokenRepository otpTokenRepository;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private CookieUtils cookieUtils;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserMapper userMapper;
  @InjectMocks private AuthServiceImpl authService;

  @Mock private HttpServletResponse response;

  @Test
  void shouldRegisterUserSuccessfully() {

    RegisterRequestDto request =
        new RegisterRequestDto("John", "Doe", "john@example.com", "Password123", "123456");

    User user = new User();
    user.setEmail(request.email());

    OTPToken token = new OTPToken();
    Role role = new Role();
    role.setName("USER");
    User savedUser = new User();
    savedUser.setId(UUID.randomUUID());

    when(otpVerificationService.verifyOTP(request.otp(), request.email())).thenReturn(token);
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
    when(userMapper.fromRegisterRequest(request)).thenReturn(user);
    when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenProvider.generateAccessToken(savedUser)).thenReturn("access-token");
    when(jwtTokenProvider.generateRefreshToken(savedUser.getId().toString()))
        .thenReturn("refresh-token");
    when(cookieUtils.createHttpOnlyCookie(anyString(), anyString(), anyLong()))
        .thenReturn(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token"));

    AuthResponseDto result = authService.registerUser(request, response);

    verify(otpVerificationService).verifyOTP(request.otp(), request.email());
    verify(userRepository).save(any(User.class));
    verify(otpTokenRepository).delete(token);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
    verify(response).addCookie(any());
    assertEquals("Bearer", result.tokenType());
    assertEquals("access-token", result.accessToken());
  }

  @Test
  void shouldThrowWhenOtpInvalid() {
    RegisterRequestDto request =
        new RegisterRequestDto("John", "Doe", "john@example.com", "Password123", "999999");
    when(otpVerificationService.verifyOTP(any(), any()))
        .thenThrow(new InvalidTokenException("Invalid OTP"));

    assertThrows(InvalidTokenException.class, () -> authService.registerUser(request, response));

    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldThrowWhenDefaultRoleMissing() {
    RegisterRequestDto request =
        new RegisterRequestDto("Jane", "Doe", "jane@example.com", "Password123", "123456");
    when(otpVerificationService.verifyOTP(any(), any())).thenReturn(new OTPToken());
    when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> authService.registerUser(request, response));

    verify(userRepository, never()).save(any());
  }
}
