package com.amalitech.tib.auth.dto;

public record RefreshResponseDto(
        String accessToken,
        String tokenType
) {

}
