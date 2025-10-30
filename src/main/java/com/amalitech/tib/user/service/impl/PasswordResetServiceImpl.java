package com.amalitech.tib.user.service.impl;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.exception.ResourceNotFoundException;
import com.amalitech.tib.user.dto.PasswordResetConfirmDto;
import com.amalitech.tib.user.dto.PasswordResetRequestDto;
import com.amalitech.tib.user.model.OTPToken;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.OTPTokenRepository;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.service.EmailService;
import com.amalitech.tib.user.service.OTPVerificationService;
import com.amalitech.tib.user.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service implementation for password reset functionality. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

  private final UserRepository userRepository;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;
  private final OTPVerificationService otpVerificationService;
  private final OTPTokenRepository otpTokenRepository;

  /**
   * Initiates a password reset process for a user by emailing them with a reset token.
   *
   * @param request contains user's email address
   * @throws ResourceNotFoundException if a user is not found
   */
  @Override
  @Transactional
  public void resetPassword(PasswordResetRequestDto request) {

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    String token = otpVerificationService.generateOTP(user.getEmail());

    emailService.sendPasswordResetEmail(user.getEmail(), token);

    log.info("Password reset initiated for user: {}", user.getEmail());
  }

  /**
   * Resets the user's password using valid reset token.
   *
   * @param confirmDto contains reset token and new password
   * @throws InvalidTokenException if the token is invalid or expired
   */
  @Override
  @Transactional
  public void setPassword(PasswordResetConfirmDto confirmDto) {
    OTPToken otpToken =
        otpTokenRepository
            .findByToken(confirmDto.otp())
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP"));

    if (otpToken.isExpired()) {
      throw new InvalidTokenException("OTP has expired");
    }

    User user =
        userRepository
            .findByEmail(otpToken.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setPassword(passwordEncoder.encode(confirmDto.newPassword()));
    userRepository.save(user);

    otpTokenRepository.delete(otpToken);

    log.info("Password reset successful for user: {}", user.getEmail());
  }
}
