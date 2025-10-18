package com.amalitech.tib.auth.dto;

public record RefreshResponse(
        String accessToken,
        String tokenType
) {

}
