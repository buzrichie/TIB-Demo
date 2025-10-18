package com.amalitech.tib.auth.dto;

public record LoginRequest(
        String email,
        String password
) { }
