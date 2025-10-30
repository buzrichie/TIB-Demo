package com.amalitech.tib.user.service.impl;

import com.amalitech.tib.exception.*;
import com.amalitech.tib.user.dto.*;
import com.amalitech.tib.user.enums.UserStatus;
import com.amalitech.tib.user.mapper.UserMapper;
import com.amalitech.tib.user.model.OTPToken;
import com.amalitech.tib.user.model.RefreshToken;
import com.amalitech.tib.user.model.Role;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.OTPTokenRepository;
import com.amalitech.tib.user.repository.RefreshTokenRepository;
import com.amalitech.tib.user.repository.RoleRepository;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.security.UserDetailsImpl;
import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.user.service.AuthService;
import com.amalitech.tib.user.service.EmailService;
import com.amalitech.tib.user.service.OTPVerificationService;
import com.amalitech.tib.user.service.TokenBlacklistService;
import com.amalitech.tib.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthenticationManager authenticationManager;
  private final CookieUtils cookieUtils;
  private final TokenBlacklistService tokenBlacklistService;
  private final EmailService emailService;
  private final OTPTokenRepository OTPTokenRepository;
  private final OTPVerificationService otpVerificationService;

  @Override
  @Transactional
  public AuthResponseDto registerUser(RegisterRequestDto request, HttpServletResponse response) {
    OTPToken token = otpVerificationService.verifyOTP(request.otp(), request.email());
    Role defaultRole =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Default role 'USER' not found"));

    User user = userMapper.fromRegisterRequest(request);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setStatus(UserStatus.ACTIVE);
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setLastActive(Instant.now());
    user.setDefaultRole(defaultRole);
    user.getRoles().add(defaultRole);
    user.setProvider("local");

    User savedUser = userRepository.save(user);

    OTPTokenRepository.delete(token);

    String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
    String refreshTokenValue = jwtTokenProvider.generateRefreshToken(savedUser.getId().toString());

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(savedUser);
    refreshToken.setToken(refreshTokenValue);
    refreshToken.setIsRevoked(false);
    refreshTokenRepository.save(refreshToken);

    response.addCookie(
        cookieUtils.createHttpOnlyCookie("refreshToken", refreshToken.getToken(), 604799998L));

    UserDto userDto = userMapper.toDto(savedUser);
    return new AuthResponseDto(accessToken, "Bearer", userDto);
  }

  @Override
  @Transactional
  public AuthResponseDto login(LoginRequestDto request, HttpServletResponse response) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    User user = userDetails.getUser();

    String accessToken = jwtTokenProvider.generateAccessToken(user);
    String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId().toString());

    RefreshToken refreshToken =
        refreshTokenRepository
            .findByUserId(user.getId())
            .orElseGet(
                () -> {
                  RefreshToken newToken = new RefreshToken();
                  newToken.setUser(user);
                  return newToken;
                });

    refreshToken.setToken(refreshTokenValue);
    refreshToken.setIsRevoked(false);
    refreshTokenRepository.save(refreshToken);

    user.setLastActive(Instant.now());
    userRepository.save(user);

    response.addCookie(
        cookieUtils.createHttpOnlyCookie("refreshToken", refreshToken.getToken(), 604799998L));
    UserDto userDto = userMapper.toDto(user);

    return new AuthResponseDto(accessToken, "Bearer", userDto);
  }

  @Override
  @Transactional
  public void logout(
      String requestToken, HttpServletRequest request, HttpServletResponse response) {
    String accessToken = jwtTokenProvider.extractJWTAuthTokenFromHeader(request);
    if (requestToken != null && !requestToken.isBlank()) {
      String token = tokenBlacklistService.validateRequestToken(requestToken);

      refreshTokenRepository
          .findByUserId(UUID.fromString(jwtTokenProvider.getSubject(token)))
          .ifPresent(
              refreshToken -> {
                if (!refreshToken.getIsRevoked()) {
                  refreshToken.setIsRevoked(true);
                  refreshTokenRepository.save(refreshToken);
                }
              });

      Instant tokenExpiry = jwtTokenProvider.getExpiration(token);
      tokenBlacklistService.blacklistToken(token, tokenExpiry, "logout");
    }

    String validAccessToken = tokenBlacklistService.validateRequestToken(accessToken);
    Instant accessTokenExpiry = jwtTokenProvider.getExpiration(validAccessToken);
    tokenBlacklistService.blacklistToken(validAccessToken, accessTokenExpiry, "logout");
    response.addCookie(cookieUtils.expireCookie());
    SecurityContextHolder.clearContext();
  }

  @Override
  @Transactional
  public RefreshResponseDto refreshAccessToken(
      HttpServletRequest request, String refreshTokenValue) {

    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      throw new InvalidTokenException("Refresh Token is missing from cookie.");
    }

    String validRefreshToken = tokenBlacklistService.validateRequestToken(refreshTokenValue);

    String userId = jwtTokenProvider.getSubject(validRefreshToken);
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByUserId(UUID.fromString(userId))
            .orElseThrow(() -> new InvalidTokenException("No refresh token found for this user"));

    if (refreshToken.getIsRevoked()) {
      throw new InvalidTokenException("Refresh token has been revoked — please log in again");
    }

    if (!jwtTokenProvider.validateToken(refreshToken.getToken())) {
      throw new InvalidTokenException("Refresh token expired — please log in again");
    }

    User user = refreshToken.getUser();
    String newAccessToken = jwtTokenProvider.generateAccessToken(user);

    String oldAccessToken = jwtTokenProvider.extractJWTAuthTokenFromHeader(request);
    tokenBlacklistService.blacklistToken(
        oldAccessToken, jwtTokenProvider.getExpiration(oldAccessToken), "refresh");

    return new RefreshResponseDto(newAccessToken, "Bearer");
  }

  @Override
  @Transactional
  public void requestOtp(String email) {
    checkForExistingData(email);

    String token = otpVerificationService.generateOTP(email);

    emailService.sendOtpMail(email, token);
  }

  @Override
  @Transactional
  public void resendVerificationEmail(String email) {
    checkForExistingData(email);

    String token = otpVerificationService.generateOTP(email);

    emailService.sendOtpMail(email, token);
  }

  private void checkForExistingData(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistException("Email already exists");
    }
  }
}
