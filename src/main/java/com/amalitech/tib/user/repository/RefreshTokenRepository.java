package com.amalitech.tib.user.repository;

import com.amalitech.tib.user.model.RefreshToken;
import com.amalitech.tib.user.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUser(User user);

  Optional<RefreshToken> findByUserId(UUID uuid);
}
