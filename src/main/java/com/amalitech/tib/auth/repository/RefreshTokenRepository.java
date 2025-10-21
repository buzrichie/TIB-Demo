package com.amalitech.tib.auth.repository;

import com.amalitech.tib.auth.model.RefreshToken;
import com.amalitech.tib.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(UUID uuid);
}
