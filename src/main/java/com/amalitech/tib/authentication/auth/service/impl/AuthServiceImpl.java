package com.amalitech.tib.authentication.auth.service.impl;

import com.amalitech.tib.authentication.auth.dto.AuthResponse;
import com.amalitech.tib.authentication.auth.dto.LoginRequest;
import com.amalitech.tib.authentication.auth.dto.RegisterRequest;
import com.amalitech.tib.authentication.auth.model.RefreshToken;
import com.amalitech.tib.authentication.auth.repository.RefreshTokenRepository;
import com.amalitech.tib.authentication.auth.service.AuthService;
import com.amalitech.tib.authentication.auth.service.TokenBlacklistService;
import com.amalitech.tib.shared.exception.EmailAlreadyExistException;
import com.amalitech.tib.shared.exception.InvalidTokenException;
import com.amalitech.tib.shared.exception.ResourceNotFoundException;
import com.amalitech.tib.authentication.user.mapper.UserMapper;
import com.amalitech.tib.authentication.user.model.Role;
import com.amalitech.tib.authentication.user.repository.RoleRepository;
import com.amalitech.tib.authentication.security.CustomUserDetailsService;
import com.amalitech.tib.authentication.security.UserDetailsImpl;
import com.amalitech.tib.authentication.security.provider.JwtTokenProvider;
import com.amalitech.tib.authentication.user.repository.UserRepository;
import com.amalitech.tib.authentication.user.dto.UserDto;
import com.amalitech.tib.authentication.user.enums.UserStatus;
import com.amalitech.tib.authentication.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
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

        UserDto userDto = userMapper.toDto(savedUser);
        return new AuthResponse(accessToken, "Bearer", userDto);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
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

        UserDto userDto = userMapper.toDto(user);

        return new AuthResponse(accessToken, "Bearer", userDto);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        String token = validateAccessToken(request);

        refreshTokenRepository.findByUserId(UUID.fromString(jwtTokenProvider.getSubject(token)))
                .ifPresent(refreshToken -> {
                    if (!refreshToken.getIsRevoked()) {
                        refreshToken.setIsRevoked(true);
                        refreshTokenRepository.save(refreshToken);
                    }
                });

        Instant tokenExpiry = jwtTokenProvider.getExpiration(token);
        tokenBlacklistService.blacklistToken(token, tokenExpiry, "logout");

        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(HttpServletRequest request) {
        String oldAccessToken = validateAccessToken(request);

        String userId = jwtTokenProvider.getSubject(oldAccessToken);
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

        Instant expiry = jwtTokenProvider.getExpiration(oldAccessToken);
        tokenBlacklistService.blacklistToken(oldAccessToken, expiry, "refreshed");

        UserDto userDto = userMapper.toDto(user);
        return new AuthResponse(newAccessToken, "Bearer", userDto);
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

    private String validateAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String token = header.substring(7);

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new InvalidTokenException("Access token is blacklisted — please log in again");
        }

        String userId = jwtTokenProvider.getSubject(token);
        if (userId == null) {
            throw new InvalidTokenException("Invalid access token");
        }

        return token;
    }

    private void checkForExistingData(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistException("Email already exists");
        }
    }
}
