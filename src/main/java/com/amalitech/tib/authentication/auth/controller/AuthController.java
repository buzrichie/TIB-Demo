package com.amalitech.tib.authentication.controller;

import com.amalitech.tib.authentication.dto.AuthResponse;
import com.amalitech.tib.authentication.dto.LoginRequest;
import com.amalitech.tib.authentication.dto.RegisterRequest;
import com.amalitech.tib.authentication.service.AuthService;
import com.amalitech.tib.shared.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles authentication-related endpoints such as registration and login.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user profile with provided details and assigns the default USER role."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @Operation(
            summary = "Authenticate an existing user",
            description = "Validates user credentials and returns an access token (refresh token is stored server-side)."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
    }

    @Operation(
            summary = "Logout user",
            description = "Revokes the user's refresh token, effectively logging them out."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "User logged out successfully"));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using the server-stored refresh token for the authenticated user."
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshAccessToken(HttpServletRequest request) {
        AuthResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Access token refreshed successfully"));
    }


}
