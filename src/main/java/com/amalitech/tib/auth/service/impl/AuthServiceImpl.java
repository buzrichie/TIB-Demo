package com.amalitech.tib.auth.service.impl;

import com.amalitech.tib.auth.dto.*;
import com.amalitech.tib.auth.model.RefreshToken;
import com.amalitech.tib.auth.repository.RefreshTokenRepository;
import com.amalitech.tib.auth.service.AuthService;
import com.amalitech.tib.auth.service.TokenBlacklistService;
import com.amalitech.tib.config.CookieUtils;
import com.amalitech.tib.shared.exception.*;
import com.amalitech.tib.auth.mapper.UserMapper;
import com.amalitech.tib.auth.model.Role;
import com.amalitech.tib.auth.repository.RoleRepository;
import com.amalitech.tib.auth.security.CustomUserDetailsService;
import com.amalitech.tib.auth.security.UserDetailsImpl;
import com.amalitech.tib.auth.security.provider.JwtTokenProvider;
import com.amalitech.tib.auth.repository.UserRepository;
import com.amalitech.tib.auth.enums.UserStatus;
import com.amalitech.tib.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final CookieUtils cookieUtils;

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request, HttpServletResponse response) {
        checkForExistingData(request);

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role 'USER' not found"));

        User user = userMapper.fromRegisterRequest(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setLastActive(Instant.now());
        user.setDefaultRole(defaultRole);
        user.getRoles().add(defaultRole);

        User savedUser = userRepository.save(user);

        String accessToken = generateAccessTokenForUser(savedUser);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(savedUser.getId().toString());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(savedUser);
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setIsRevoked(false);
        refreshTokenRepository.save(refreshToken);

        response.addCookie(cookieUtils.createHttpOnlyCookie("refreshToken",refreshToken.getToken(),604799998L));

        UserDto userDto = userMapper.toDto(savedUser);
        return new AuthResponse(accessToken, "Bearer", userDto);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user =  userDetails.getUser();

        String accessToken = generateAccessTokenForUser(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    RefreshToken newToken = new RefreshToken();
                    newToken.setUser(user);
                    return newToken;
                });

        refreshToken.setToken(refreshTokenValue);
        refreshToken.setIsRevoked(false);
        refreshTokenRepository.save(refreshToken);

        user.setLastActive(Instant.now());
        userRepository.save(user);

        response.addCookie(cookieUtils.createHttpOnlyCookie("refreshToken",refreshToken.getToken(),604799998L));
        UserDto userDto = userMapper.toDto(user);

        return new AuthResponse(accessToken, "Bearer", userDto);
    }

    @Override
    @Transactional
    public void logout(String requestToken, HttpServletResponse response) {
        String token = validateAccessToken(requestToken);

        refreshTokenRepository.findByUserId(UUID.fromString(jwtTokenProvider.getSubject(token)))
                .ifPresent(refreshToken -> {
                    if (!refreshToken.getIsRevoked()) {
                        refreshToken.setIsRevoked(true);
                        refreshTokenRepository.save(refreshToken);
                    }
                });

        Instant tokenExpiry = jwtTokenProvider.getExpiration(token);
        tokenBlacklistService.blacklistToken(token, tokenExpiry, "logout");
        response.addCookie(cookieUtils.expireCookie());
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public RefreshResponse refreshAccessToken(HttpServletRequest request,String refreshTokenValue, HttpServletResponse response) {

        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidTokenException("Refresh Token is missing from cookie.");
        }

        String validRefreshToken = validateAccessToken(refreshTokenValue);

        String userId = jwtTokenProvider.getSubject(validRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new InvalidTokenException("No refresh token found for this user"));

        if (refreshToken.getIsRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked — please log in again");
        }

        if (!jwtTokenProvider.validateToken(refreshToken.getToken())) {
            throw new InvalidTokenException("Refresh token expired — please log in again");
        }

        User user = refreshToken.getUser();
        String newAccessToken = generateAccessTokenForUser(user);

        return new RefreshResponse(newAccessToken, "Bearer");
    }

    private String generateAccessTokenForUser(User user) {
        List<String> roles = user.getEffectiveRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .toList();

        List<String> permissions = user.getEffectiveRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Enum::name)
                .toList();

        return jwtTokenProvider.generateAccessToken(
                user.getId().toString(),
                user.getEmail(),
                roles,
                permissions
        );
    }
    private String validateAccessToken(String token) {

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new InvalidTokenException("Access token is expired — please log in again");
        }

        String userId = jwtTokenProvider.getSubject(token);
        if (userId == null) {
            throw new BadException("User session not found or already logged out");
        }

        return token;
    }

    private String validateAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RefreshTokenRequiredException("Token is required");
        }

        String token = header.substring(7);

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new InvalidTokenException("Access token is expired — please log in again");
        }

        String userId = jwtTokenProvider.getSubject(token);
        if (userId == null) {
            throw new BadException("User session not found or already logged out");
        }

        return token;
    }

    private void checkForExistingData(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistException("Email already exists");
        }
    }
}
