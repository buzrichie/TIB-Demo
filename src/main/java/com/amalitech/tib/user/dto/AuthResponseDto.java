package com.amalitech.tib.user.dto;

public record AuthResponseDto(String accessToken, String tokenType, UserDto user) {}
