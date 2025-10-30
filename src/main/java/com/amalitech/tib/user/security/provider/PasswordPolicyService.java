package com.amalitech.tib.user.security.provider;

import java.security.SecureRandom;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Handles password validation and generation logic. */
@Service
@Slf4j
public class PasswordPolicyService {

  @Value("${app.security.strict-password-check:true}")
  private boolean strictPasswordCheck;

  private static final String STRONG_PASSWORD_REGEX =
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";

  public String resolvePassword(String providedPassword) {
    if (isStrong(providedPassword)) {
      return providedPassword;
    }

    if (strictPasswordCheck) {
      throw new IllegalArgumentException(
          "Weak password! Must include uppercase, lowercase, digit, and special character, min 8 chars.");
    }

    // Non-strict mode: auto-generate strong password
    String generated = generateStrongRandomPassword(16);
    log.warn("Weak or missing password detected. Auto-generated secure password: {}", generated);
    return generated;
  }

  private boolean isStrong(String password) {
    return password != null && Pattern.matches(STRONG_PASSWORD_REGEX, password);
  }

  private String generateStrongRandomPassword(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&+=";
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }
}
