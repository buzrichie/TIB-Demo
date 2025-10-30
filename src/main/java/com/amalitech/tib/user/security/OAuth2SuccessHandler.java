package com.amalitech.tib.user.security;

import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.user.dto.AuthResponseDto;
import com.amalitech.tib.user.dto.OAuth2UserInfoDto;
import com.amalitech.tib.user.dto.UserDto;
import com.amalitech.tib.user.enums.UserStatus;
import com.amalitech.tib.user.mapper.UserMapper;
import com.amalitech.tib.user.model.RefreshToken;
import com.amalitech.tib.user.model.Role;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.RefreshTokenRepository;
import com.amalitech.tib.user.repository.RoleRepository;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.util.CookieUtils;
import com.amalitech.tib.util.NameUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class handles the successful authentication of OAuth2 users. It updates an existing user or
 * creates a new user if the user does not exist. It generates a new access token and refresh token
 * for the user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final CookieUtils cookieUtils;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  @Transactional
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    try {
      OAuth2UserInfoDto userInfo = extractUserInfo(authentication);

      User user = processOAuth2User(userInfo);

      AuthResponseDto authResponse = createAuthResponse(user, response);

      String jsonAuthResponse = objectMapper.writeValueAsString(authResponse);
      String encodedAuthResponse = URLEncoder.encode(jsonAuthResponse, StandardCharsets.UTF_8);

      log.info("OAuth2 authentication successful: {}", userInfo.email());
      response.sendRedirect("/api/v1/auth/oauth2/success?data=" + encodedAuthResponse);

    } catch (Exception e) {
      log.warn("OAuth2 authentication failed: {}", e.getMessage());
      String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
      response.sendRedirect("/api/v1/auth/oauth2/failure?error=" + encodedError);
    }
  }

  private OAuth2UserInfoDto extractUserInfo(Authentication authentication) {
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

    return new OAuth2UserInfoDto(
        oauthUser.getAttribute("email"),
        oauthUser.getAttribute("name"),
        oauthUser.getAttribute("picture"),
        oauthToken.getAuthorizedClientRegistrationId());
  }

  public User processOAuth2User(OAuth2UserInfoDto userInfo) {
    String[] names = NameUtils.splitFullName(userInfo.name());
    String firstName = names[0];
    String lastName = names[1];

    return userRepository
        .findByEmail(userInfo.email())
        .map(
            existingUser -> {
              if (!existingUser.getProvider().equals(userInfo.provider())) {
                existingUser.setProvider(userInfo.provider());
              }
              if (existingUser.getUsername() == null && userInfo.name() != null) {
                existingUser.setFirstName(firstName);
                existingUser.setLastName(lastName);
              }
              if (existingUser.getProfileImageUrl() == null && userInfo.profileImageUrl() != null) {
                existingUser.setProfileImageUrl(userInfo.profileImageUrl());
              }

              existingUser.setLastActive(Instant.now());
              return userRepository.save(existingUser);
            })
        .orElseGet(
            () -> {
              Role defaultRole =
                  roleRepository
                      .findByName("USER")
                      .orElseThrow(
                          () -> new ResourceNotFoundException("Default role 'USER' not found"));

              Set<Role> roles = new HashSet<>();
              roles.add(defaultRole);

              User newUser =
                  User.builder()
                      .email(userInfo.email())
                      .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                      .firstName(firstName)
                      .lastName(lastName)
                      .profileImageUrl(userInfo.profileImageUrl())
                      .provider(userInfo.provider())
                      .status(UserStatus.ACTIVE)
                      .lastActive(Instant.now())
                      .defaultRole(defaultRole)
                      .roles(roles)
                      .build();
              return userRepository.save(newUser);
            });
  }

  private AuthResponseDto createAuthResponse(User user, HttpServletResponse response) {
    String accessToken = jwtTokenProvider.generateAccessToken(user);
    String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId().toString());

    saveRefreshToken(user, refreshTokenValue);

    response.addCookie(
        cookieUtils.createHttpOnlyCookie("refreshToken", refreshTokenValue, 604799998L));

    UserDto userDto = userMapper.toDto(user);
    return new AuthResponseDto(accessToken, "Bearer", userDto);
  }

  private void saveRefreshToken(User user, String refreshTokenValue) {
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
  }
}
