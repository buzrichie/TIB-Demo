package com.amalitech.tib.auth.controller;

import com.amalitech.tib.auth.dto.AuthResponse;
import com.amalitech.tib.auth.dto.LoginRequest;
import com.amalitech.tib.auth.dto.RefreshResponse;
import com.amalitech.tib.auth.dto.RegisterRequest;
import com.amalitech.tib.auth.service.AuthService;
import com.amalitech.tib.shared.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest request, HttpServletResponse headResponse) {
        AuthResponse response = authService.registerUser(request, headResponse);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @Operation(
            summary = "Authenticate an existing user",
            description = "Validates user credentials and returns an access token (refresh token is stored server-side)."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest request,HttpServletResponse headResponse) {
        AuthResponse response = authService.login(request, headResponse);
        return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
    }

    @Operation(
            summary = "Logout user",
            description = "Revokes the user's refresh token, effectively logging them out."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(@CookieValue(name = "refreshToken", required = false) String refreshTokenValue, HttpServletResponse headResponse) {
        authService.logout(refreshTokenValue, headResponse);
        return ResponseEntity.ok(ApiResponse.success(null, "User logged out successfully"));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using the server-stored refresh token for the authenticated user."
    )
    @GetMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshResponse>> refreshAccessToken(HttpServletRequest request,@CookieValue(name = "refreshToken", required = false) String refreshTokenValue
                                                                           ) {
        RefreshResponse response = authService.refreshAccessToken(request, refreshTokenValue);
        return ResponseEntity.ok(ApiResponse.success(response, "Access token refreshed successfully"));
    }


}
