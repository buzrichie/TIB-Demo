package com.amalitech.tib.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDto(
    @NotBlank(message = "Token is required") String otp,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message =
                "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
        String newPassword) {}
