package com.amalitech.tib.user.service;

import com.amalitech.tib.user.dto.AuthResponseDto;
import com.amalitech.tib.user.dto.LoginRequestDto;
import com.amalitech.tib.user.dto.RefreshResponseDto;
import com.amalitech.tib.user.dto.RegisterRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
  AuthResponseDto registerUser(RegisterRequestDto user, HttpServletResponse response);

  AuthResponseDto login(LoginRequestDto user, HttpServletResponse response);

  void logout(String refreshTokenValue, HttpServletRequest request, HttpServletResponse response);

  RefreshResponseDto refreshAccessToken(HttpServletRequest request, String refreshTokenValue);

  void requestOtp(String token);

  void resendVerificationEmail(String email);
}
