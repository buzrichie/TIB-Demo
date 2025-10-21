package com.amalitech.tib.auth.service;


import com.amalitech.tib.auth.model.TokenBlacklist;
import com.amalitech.tib.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository repository;

    public void blacklistToken(String token, Instant expiresAt, String reason) {
        if (!repository.existsByToken(token)) {
            TokenBlacklist entry = TokenBlacklist.builder()
                    .token(token)
                    .blacklistedAt(Instant.now())
                    .expiresAt(expiresAt)
                    .reason(reason)
                    .build();
            repository.save(entry);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return repository.existsByToken(token);
    }

    public void removeExpiredTokens() {
        repository.findAll().stream()
                .filter(t -> t.getExpiresAt().isBefore(Instant.now()))
                .forEach(repository::delete);
    }

}
