package com.amalitech.tib.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserDto user
) {

}
