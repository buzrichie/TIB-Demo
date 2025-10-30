package com.amalitech.tib.user.service;

import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.user.model.TokenBlacklist;
import com.amalitech.tib.user.repository.TokenBlacklistRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final TokenBlacklistRepository repository;

  public void blacklistToken(String token, Instant expiresAt, String reason) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token must not be null or empty");
    }

    if (expiresAt == null) {
      throw new IllegalArgumentException("Expiration time must not be null");
    }

    if (!repository.existsByToken(token)) {
      TokenBlacklist entry =
          TokenBlacklist.builder()
              .token(token)
              .blacklistedAt(Instant.now())
              .expiresAt(expiresAt)
              .reason(reason)
              .build();
      repository.save(entry);
    }
  }

  public boolean isTokenBlacklisted(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token must not be null or empty");
    }
    return repository.existsByToken(token);
  }

  public void removeExpiredTokens() {
    repository.findAll().stream()
        .filter(t -> t.getExpiresAt().isBefore(Instant.now()))
        .forEach(repository::delete);
  }

  public String validateRequestToken(String token) {
    if (token == null || token.isBlank()) {
      throw new InvalidTokenException("Token is missing.");
    }
    if (isTokenBlacklisted(token)) {
      throw new InvalidTokenException("Token is expired — please log in again");
    }

    return token;
  }
}
