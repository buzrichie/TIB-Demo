package com.amalitech.tib.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequestDto(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @NotBlank(message = "Username is required") String username) {}
