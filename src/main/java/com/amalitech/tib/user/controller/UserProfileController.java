package com.amalitech.tib.user.controller;

import com.amalitech.tib.user.dto.UpdateProfileRequestDto;
import com.amalitech.tib.user.dto.UserProfileResponseDto;
import com.amalitech.tib.user.service.UserProfileService;
import com.amalitech.tib.util.ApiResponse;
import com.amalitech.tib.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserProfileService userProfileService;

  @Operation(
      summary = "Get the profile of the authenticated user",
      description = "Gets the profile information of the currently authenticated user")
  @GetMapping
  public ResponseEntity<ApiResponse<UserProfileResponseDto>> getUserProfile() {
    UUID userId = SecurityUtils.getCurrentUserId();
    UserProfileResponseDto profileDto = userProfileService.getUserProfile(userId);
    return ResponseEntity.ok(ApiResponse.success(profileDto, "Profile retrieved successfully"));
  }

  @Operation(
      summary = "Update the profile of the authenticated user",
      description = "Updates the profile information of the currently authenticated user")
  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateUserProfile(
      @Valid @ModelAttribute UpdateProfileRequestDto requestDto,
      @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
    UUID userId = SecurityUtils.getCurrentUserId();
    UserProfileResponseDto updatedProfile =
        userProfileService.updateUserProfile(userId, requestDto, profileImage);
    return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
  }
}
