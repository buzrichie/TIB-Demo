package com.amalitech.tib.user.service;

import com.amalitech.tib.user.dto.PasswordResetConfirmDto;
import com.amalitech.tib.user.dto.PasswordResetRequestDto;

public interface PasswordResetService {
  void resetPassword(PasswordResetRequestDto request);

  void setPassword(PasswordResetConfirmDto confirmDto);
}
