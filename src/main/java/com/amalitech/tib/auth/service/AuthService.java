package com.amalitech.tib.auth.service;

import com.amalitech.tib.auth.dto.AuthResponse;
import com.amalitech.tib.auth.dto.LoginRequest;
import com.amalitech.tib.auth.dto.RefreshResponse;
import com.amalitech.tib.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest user, HttpServletResponse response);
    AuthResponse login(LoginRequest user, HttpServletResponse response);
    void logout(String refreshTokenValue,  HttpServletResponse response);
    RefreshResponse refreshAccessToken(HttpServletRequest request,String refreshTokenValue);

}
