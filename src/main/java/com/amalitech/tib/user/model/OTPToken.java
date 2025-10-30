package com.amalitech.tib.user.model;

import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTPToken extends BaseEntity {

  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private int requestCount;

  @Column(nullable = false)
  private Instant lastRequestedAt;

  private Instant cooldownUntil;

  public static OTPToken createToken(String email, int expiryMinutes) {
    return OTPToken.builder()
        .token(generateOtp())
        .email(email)
        .expiresAt(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES))
        .requestCount(1)
        .lastRequestedAt(Instant.now())
        .cooldownUntil(null)
        .build();
  }

  private static String generateOtp() {
    int otp = (int) (Math.random() * 900000) + 100000;
    return String.valueOf(otp);
  }

  public void regenerateOtp(int expiryMinutes) {
    this.token = generateOtp();
    this.expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES);
    this.lastRequestedAt = Instant.now();
    this.requestCount++;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
