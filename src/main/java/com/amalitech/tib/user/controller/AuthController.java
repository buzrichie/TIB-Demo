package com.amalitech.tib.user.controller;

import com.amalitech.tib.user.dto.*;
import com.amalitech.tib.user.service.AuthService;
import com.amalitech.tib.user.service.PasswordResetService;
import com.amalitech.tib.util.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Handles authentication-related endpoints such as registration and login. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final ObjectMapper objectMapper;
  private final PasswordResetService passwordResetService;

  @Operation(
      summary = "Register a new user account",
      description =
          "Validates the OTP, creates the account, assigns default USER role, and logs in the user.")
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponseDto>> registerUser(
      @Valid @RequestBody RegisterRequestDto request, HttpServletResponse response) {
    AuthResponseDto authResponse = authService.registerUser(request, response);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(authResponse, "User registered successfully"));
  }

  @Operation(
      summary = "Authenticate an existing user",
      description =
          "Validates user credentials and returns an access token (refresh token is stored server-side).")
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponseDto>> loginUser(
      @Valid @RequestBody LoginRequestDto request, HttpServletResponse headResponse) {
    AuthResponseDto response = authService.login(request, headResponse);
    return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
  }

  @Operation(
      summary = "Request OTP for email verification",
      description =
          "Generates a one-time verification code (OTP) and sends it to the user's email.")
  @PostMapping("/verify-email")
  public ResponseEntity<ApiResponse<String>> requestOtp(@RequestParam("email") String email) {
    authService.requestOtp(email);
    return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully!"));
  }

  @Operation(
      summary = "Resend OTP for email verification",
      description =
          "Sends a new OTP to the user's email if the previous one expired or was not received.")
  @PostMapping("/resend-otp")
  public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam("email") String email) {
    authService.resendVerificationEmail(email);
    return ResponseEntity.ok(ApiResponse.success(null, "New OTP sent successfully!"));
  }

  @Operation(
      summary = "OAuth2 authentication success callback",
      description = "Callback endpoint that processes OAuth2 authentication successes")
  @GetMapping("/oauth2/success")
  public ResponseEntity<ApiResponse<AuthResponseDto>> oauthSuccess(@RequestParam String data)
      throws JsonProcessingException {
    String decodedAuth = URLDecoder.decode(data, StandardCharsets.UTF_8);
    AuthResponseDto response = objectMapper.readValue(decodedAuth, AuthResponseDto.class);
    return ResponseEntity.ok(ApiResponse.success(response, "Google authentication successful"));
  }

  @Operation(
      summary = "OAuth2 authentication failure callback",
      description = "Callback endpoint that processes OAuth2 authentication failures")
  @GetMapping("/oauth2/failure")
  public ResponseEntity<ApiResponse<String>> oauthFailure(@RequestParam String error) {
    String decodedError = URLDecoder.decode(error, StandardCharsets.UTF_8);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Google login failed", List.of(decodedError)));
  }

  @Operation(
      summary = "Logout user",
      description = "Revokes the user's refresh token, effectively logging them out.")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logoutUser(
      @CookieValue(name = "refreshToken", required = false) String refreshTokenValue,
      HttpServletRequest request,
      HttpServletResponse headResponse) {
    authService.logout(refreshTokenValue, request, headResponse);
    return ResponseEntity.ok(ApiResponse.success(null, "User logged out successfully"));
  }

  @Operation(
      summary = "Refresh access token",
      description =
          "Generates a new access token using the server-stored refresh token for the authenticated user.")
  @GetMapping("/refresh-token")
  public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshAccessToken(
      HttpServletRequest request,
      @CookieValue(name = "refreshToken", required = false) String refreshTokenValue) {
    RefreshResponseDto response = authService.refreshAccessToken(request, refreshTokenValue);
    return ResponseEntity.ok(ApiResponse.success(response, "Access token refreshed successfully"));
  }

  @Operation(
      summary = "Request password reset",
      description = "Sends a password reset link to the user's email.")
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<String>> forgotPassword(
      @Valid @RequestBody PasswordResetRequestDto request) {
    passwordResetService.resetPassword(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Email sent with password reset details"));
  }

  @Operation(
      summary = "Set new password",
      description = "Resets user's password using valid reset token.")
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<String>> resetPassword(
      @Valid @RequestBody PasswordResetConfirmDto confirmDto) {
    passwordResetService.setPassword(confirmDto);
    return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
  }
}
