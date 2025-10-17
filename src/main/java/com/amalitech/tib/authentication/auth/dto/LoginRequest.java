package com.amalitech.tib.authentication.auth.dto;

public record LoginRequest(
        String email,
        String password
) { }
