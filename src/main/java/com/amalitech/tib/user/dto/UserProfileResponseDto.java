package com.amalitech.tib.user.dto;

import com.amalitech.tib.user.model.User;
import java.util.UUID;

public record UserProfileResponseDto(
    UUID id,
    String firstName,
    String lastName,
    String username,
    String email,
    String profileImageUrl) {
  public static UserProfileResponseDto fromUser(User user) {
    if (user == null) {
      return null;
    }

    return new UserProfileResponseDto(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getUsername(),
        user.getEmail(),
        user.getProfileImageUrl());
  }
}
