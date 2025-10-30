package com.amalitech.tib.user.service.impl;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.user.model.OTPToken;
import com.amalitech.tib.user.repository.OTPTokenRepository;
import com.amalitech.tib.user.service.OTPVerificationService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OTPVerificationServiceImpl implements OTPVerificationService {

  private final OTPTokenRepository OTPTokenRepository;
  private final RateLimiterImpl rateLimiter;

  @Override
  public OTPToken verifyOTP(String otp, String email) {
    if (otp == null || otp.trim().isEmpty()) {
      throw new InvalidTokenException("Invalid OTP");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new InvalidTokenException("Invalid email");
    }
    OTPToken token =
        OTPTokenRepository.findByToken(otp)
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP"));

    if (token.isExpired()) {
      throw new InvalidTokenException("OTP has expired");
    }

    if (!token.getEmail().equalsIgnoreCase(email)) {
      throw new InvalidTokenException("OTP does not match this email");
    }
    return token;
  }

  @Override
  @Transactional
  public String generateOTP(String email) {
    if (email == null || email.isBlank()) {
      throw new InvalidTokenException("Invalid email");
    }

    Instant now = Instant.now();
    OTPToken token = OTPTokenRepository.findByEmail(email).orElse(null);

    if (token != null) {
      rateLimiter.checkRateLimit(token, now);

      token.regenerateOtp(5);
      OTPTokenRepository.save(token);
      return token.getToken();
    }

    OTPToken newToken = OTPToken.createToken(email, 5);
    OTPTokenRepository.save(newToken);
    return newToken.getToken();
  }
}
