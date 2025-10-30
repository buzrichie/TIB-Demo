package com.amalitech.tib.user.service;

import com.amalitech.tib.user.model.OTPToken;
import java.time.Instant;

public interface RateLimiter {
  void checkRateLimit(OTPToken token, Instant now);
}
