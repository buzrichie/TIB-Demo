package com.amalitech.tib.trip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for accepting or rejecting trip invitations. Email is required to track the invitation
 * response even before user authentication.
 */
public record TripInviteRequestDTO(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
        String email) {}
