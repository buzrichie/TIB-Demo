package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amalitech.tib.exception.EmailAlreadyExistException;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.service.EmailService;
import com.amalitech.tib.user.service.OTPVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private OTPVerificationService otpVerificationService;
  @Mock private EmailService emailService;
  @InjectMocks private AuthServiceImpl authService;

  @Test
  void registerUser() {}

  @Test
  void login() {}

  @Test
  void logout() {}

  @Test
  void refreshAccessToken() {}

  @Test
  void shouldGenerateAndSendOtpSuccessfully() {

    String email = "newuser@example.com";
    String otp = "123456";

    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(otpVerificationService.generateOTP(email)).thenReturn(otp);

    authService.requestOtp(email);

    verify(userRepository).existsByEmail(email);
    verify(otpVerificationService).generateOTP(email);
    verify(emailService).sendOtpMail(email, otp);
  }

  @Test
  void shouldThrowExceptionWhenEmailAlreadyExists() {

    String email = "existing@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(true);

    assertThrows(EmailAlreadyExistException.class, () -> authService.requestOtp(email));

    verify(otpVerificationService, never()).generateOTP(anyString());
    verify(emailService, never()).sendOtpMail(anyString(), anyString());
  }

  @Test
  void shouldPropagateExceptionWhenOtpGenerationFails() {

    String email = "user@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(otpVerificationService.generateOTP(email))
        .thenThrow(new RuntimeException("OTP Service unavailable"));

    assertThrows(RuntimeException.class, () -> authService.requestOtp(email));

    verify(emailService, never()).sendOtpMail(anyString(), anyString());
  }

  @Test
  void resendVerificationEmail() {}
}
