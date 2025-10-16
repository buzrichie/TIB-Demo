package com.amalitech.tib.authentication.dto;

import com.amalitech.tib.user.dto.UserDto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserDto user
) {

}
