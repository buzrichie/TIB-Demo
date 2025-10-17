package com.amalitech.tib.authentication.auth.service;

import com.amalitech.tib.authentication.auth.dto.AuthResponse;
import com.amalitech.tib.authentication.auth.dto.LoginRequest;
import com.amalitech.tib.authentication.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest user);
    AuthResponse login(LoginRequest user);
    void logout(HttpServletRequest request);
    AuthResponse refreshAccessToken(HttpServletRequest request);

}
