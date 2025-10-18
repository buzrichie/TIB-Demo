package com.amalitech.tib.auth.service;

import com.amalitech.tib.auth.dto.AuthResponse;
import com.amalitech.tib.auth.dto.LoginRequest;
import com.amalitech.tib.auth.dto.RefreshResponse;
import com.amalitech.tib.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest user);
    AuthResponse login(LoginRequest user);
    void logout(HttpServletRequest request);
    RefreshResponse refreshAccessToken(HttpServletRequest request);

}
