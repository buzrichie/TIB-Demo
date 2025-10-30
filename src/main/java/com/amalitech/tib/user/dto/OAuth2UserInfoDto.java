package com.amalitech.tib.user.dto;

public record OAuth2UserInfoDto(
    String email, String name, String profileImageUrl, String provider) {
  public OAuth2UserInfoDto {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
  }
}
