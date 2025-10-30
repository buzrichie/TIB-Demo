package com.amalitech.tib.user.service.impl;

import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.user.dto.UpdateProfileRequestDto;
import com.amalitech.tib.user.dto.UserProfileResponseDto;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.service.UserProfileService;
import com.amalitech.tib.util.S3FileStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Service class for managing user profiles. */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserRepository userRepository;
  private final S3FileStorage s3FileStorage;
  private static final String PROFILE_IMAGE_PATH = "user-profiles/";

  @Transactional(readOnly = true)
  public UserProfileResponseDto getUserProfile(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    ;
    return UserProfileResponseDto.fromUser(user);
  }

  @Transactional
  public UserProfileResponseDto updateUserProfile(
      UUID userId, UpdateProfileRequestDto requestDto, MultipartFile profileImage) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    ;

    if (profileImage != null && !profileImage.isEmpty()) {
      String s3Path = PROFILE_IMAGE_PATH + userId;
      String newImageUrl = s3FileStorage.uploadFile(profileImage, s3Path);

      user.setProfileImageUrl(newImageUrl);
    }

    user.setFirstName(requestDto.firstName());
    user.setLastName(requestDto.lastName());
    user.setUsername(requestDto.username());

    User updatedUser = userRepository.save(user);
    return UserProfileResponseDto.fromUser(updatedUser);
  }
}
