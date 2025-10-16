package com.amalitech.tib.authentication.service;

import com.amalitech.tib.authentication.dto.AuthResponse;
import com.amalitech.tib.authentication.dto.LoginRequest;
import com.amalitech.tib.authentication.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest user);
    AuthResponse login(LoginRequest user);
    void logout(HttpServletRequest request);
    AuthResponse refreshAccessToken(HttpServletRequest request);

}
