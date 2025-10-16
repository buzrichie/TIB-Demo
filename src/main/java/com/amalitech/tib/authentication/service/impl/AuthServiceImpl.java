package com.amalitech.tib.authentication.service.impl;

import com.amalitech.tib.authentication.dto.AuthResponse;
import com.amalitech.tib.authentication.dto.LoginRequest;
import com.amalitech.tib.authentication.dto.RegisterRequest;
import com.amalitech.tib.authentication.model.RefreshToken;
import com.amalitech.tib.authentication.repository.RefreshTokenRepository;
import com.amalitech.tib.authentication.service.AuthService;
import com.amalitech.tib.authentication.service.TokenBlacklistService;
import com.amalitech.tib.exception.EmailAlreadyExistException;
import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.mapper.UserMapper;
import com.amalitech.tib.role.model.Role;
import com.amalitech.tib.role.repository.RoleRepository;
import com.amalitech.tib.security.provider.JwtTokenProvider;
import com.amalitech.tib.user.UserRepository;
import com.amalitech.tib.user.dto.UserDto;
import com.amalitech.tib.user.enums.UserStatus;
import com.amalitech.tib.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

        String accessToken = jwtTokenProvider.generateAccessToken(String.valueOf(savedUser.getId()));
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(String.valueOf(savedUser.getId()));

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
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidTokenException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(String.valueOf(user.getId()));
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(String.valueOf(user.getId()));

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
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String token = header.substring(7);

        String userId = jwtTokenProvider.getSubject(token);
        if (userId == null) {
            throw new InvalidTokenException("Invalid token: missing subject");
        }

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            return;
        }

        refreshTokenRepository.findByUserId(UUID.fromString(userId)).ifPresent(refreshToken -> {
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
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }

        String oldAccessToken = header.substring(7);

        if (tokenBlacklistService.isTokenBlacklisted(oldAccessToken)) {
            throw new InvalidTokenException("Access token is blacklisted — please log in again");
        }

        String userId = jwtTokenProvider.getSubject(oldAccessToken);
        if (userId == null) {
            throw new InvalidTokenException("Invalid access token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new InvalidTokenException("No refresh token found for this user"));

        if (refreshToken.getIsRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked — please log in again");
        }

        if (!jwtTokenProvider.validateToken(refreshToken.getToken())) {
            throw new InvalidTokenException("Refresh token expired — please log in again");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        User user = refreshToken.getUser();
        UserDto userDto = userMapper.toDto(user);

        Instant expiry = jwtTokenProvider.getExpiration(oldAccessToken);
        tokenBlacklistService.blacklistToken(oldAccessToken, expiry, "refreshed");

        return new AuthResponse(newAccessToken, "Bearer", userDto);
    }

    private void checkForExistingData(RegisterRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistException("Email already exists");
        }
    }
}
