package com.amalitech.tib.authentication.dto;

public record LoginRequest(
        String email,
        String password
) { }
