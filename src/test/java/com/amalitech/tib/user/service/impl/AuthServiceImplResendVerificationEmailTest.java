package com.amalitech.tib.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
class AuthServiceImplResendVerificationEmailTest {

  @Mock private UserRepository userRepository;
  @Mock private OTPVerificationService otpVerificationService;
  @Mock private EmailService emailService;
  @InjectMocks private AuthServiceImpl authService;

  @Test
  void shouldResendVerificationEmailSuccessfully() {

    String email = "pendinguser@example.com";
    String otp = "654321";

    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(otpVerificationService.generateOTP(email)).thenReturn(otp);

    authService.resendVerificationEmail(email);

    verify(userRepository).existsByEmail(email);
    verify(otpVerificationService).generateOTP(email);
    verify(emailService).sendOtpMail(email, otp);
  }

  @Test
  void shouldThrowExceptionWhenEmailAlreadyExists() {

    String email = "registered@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(true);

    assertThrows(
        EmailAlreadyExistException.class, () -> authService.resendVerificationEmail(email));

    verify(otpVerificationService, never()).generateOTP(anyString());
    verify(emailService, never()).sendOtpMail(anyString(), anyString());
  }

  @Test
  void shouldPropagateExceptionWhenOtpGenerationFails() {

    String email = "new@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(otpVerificationService.generateOTP(email))
        .thenThrow(new RuntimeException("OTP service unavailable"));

    assertThrows(RuntimeException.class, () -> authService.resendVerificationEmail(email));

    verify(emailService, never()).sendOtpMail(anyString(), anyString());
  }
}
