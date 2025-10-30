package com.amalitech.tib.user.service;

import com.amalitech.tib.user.dto.UpdateProfileRequestDto;
import com.amalitech.tib.user.dto.UserProfileResponseDto;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
  UserProfileResponseDto getUserProfile(UUID userId);

  UserProfileResponseDto updateUserProfile(
      UUID userId, UpdateProfileRequestDto requestDto, MultipartFile profileImage);
}
