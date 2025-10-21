package com.amalitech.tib.auth.service;

import com.amalitech.tib.auth.dto.AuthResponseDto;
import com.amalitech.tib.auth.dto.LoginRequestDto;
import com.amalitech.tib.auth.dto.RefreshResponseDto;
import com.amalitech.tib.auth.dto.RegisterRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponseDto registerUser(RegisterRequestDto user, HttpServletResponse response);
    AuthResponseDto login(LoginRequestDto user, HttpServletResponse response);
    void logout(String refreshTokenValue,  HttpServletResponse response);
    RefreshResponseDto refreshAccessToken(HttpServletRequest request, String refreshTokenValue);

}
