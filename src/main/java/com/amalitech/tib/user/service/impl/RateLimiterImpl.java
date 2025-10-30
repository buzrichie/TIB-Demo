package com.amalitech.tib.user.service.impl;

import com.amalitech.tib.exception.RateLimitException;
import com.amalitech.tib.user.model.OTPToken;
import com.amalitech.tib.user.repository.OTPTokenRepository;
import com.amalitech.tib.user.service.RateLimiter;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implements rate limiting for OTP requests.
 *
 * <p>Rules:
 *
 * <ul>
 *   <li>Max 3 OTP requests allowed in a 10-minute window.
 *   <li>If exceeded, user is blocked for 15 minutes (cooldown period).
 *   <li>After the cooldown, the request counter resets.
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RateLimiterImpl implements RateLimiter {

  private static final int MAX_REQUESTS = 2;
  private static final int COOLDOWN_MINUTES = 15;
  private static final int WINDOW_MINUTES = 10;

  private final OTPTokenRepository otpTokenRepository;

  @Override
  @Transactional
  public void checkRateLimit(OTPToken token, Instant now) {
    enforceCooldown(token, now);
    resetWindowIfExpired(token, now);
    handleRateLimit(token, now);
  }

  private void enforceCooldown(OTPToken token, Instant now) {
    if (token.getCooldownUntil() != null && now.isBefore(token.getCooldownUntil())) {
      long remaining = ChronoUnit.MINUTES.between(now, token.getCooldownUntil());
      throw new RateLimitException(
          String.format(
              "Too many OTP requests. Please wait %d minutes before retrying.", remaining));
    }
  }

  private void resetWindowIfExpired(OTPToken token, Instant now) {
    if (token.getLastRequestedAt().isBefore(now.minus(WINDOW_MINUTES, ChronoUnit.MINUTES))) {
      token.setRequestCount(0);
    }
  }

  private void handleRateLimit(OTPToken token, Instant now) {
    if (token.getRequestCount() > MAX_REQUESTS) {
      token.setCooldownUntil(now.plus(COOLDOWN_MINUTES, ChronoUnit.MINUTES));
      throw new RateLimitException("OTP request limit reached. Please try again later.");
    }
  }
}
