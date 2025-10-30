package com.amalitech.tib.user.service;

import com.amalitech.tib.user.model.OTPToken;

public interface OTPVerificationService {

  OTPToken verifyOTP(String otp, String email);

  String generateOTP(String email);
}
