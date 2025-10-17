package com.amalitech.tib.authentication.auth.dto;

import com.amalitech.tib.authentication.user.dto.UserDto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserDto user
) {

}
