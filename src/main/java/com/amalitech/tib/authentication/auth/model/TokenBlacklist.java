package com.amalitech.tib.authentication.auth.model;

import com.amalitech.tib.shared.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing a blacklisted JWT token.
 * Tokens in this table are considered invalid even if their JWT signature is valid.
 */
@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist extends BaseEntity {

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private Instant blacklistedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(length = 100)
    private String reason;
}
