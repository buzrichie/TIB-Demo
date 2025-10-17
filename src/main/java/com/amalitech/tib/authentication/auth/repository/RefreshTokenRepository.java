package com.amalitech.tib.authentication.repository;

import com.amalitech.tib.authentication.model.RefreshToken;
import com.amalitech.tib.authentication.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(UUID uuid);
}
