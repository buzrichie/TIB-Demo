package com.amalitech.tib.auth.dto;

public record AuthResponseDto(
        String accessToken,
        String tokenType,
        UserDto user
) {

}
