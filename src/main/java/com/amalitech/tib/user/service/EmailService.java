package com.amalitech.tib.user.service;

public interface EmailService {
  void sendPasswordResetEmail(String to, String resetToken);

  void sendOtpMail(String to, String token);
}
