package com.amalitech.tib.user.repository;

import com.amalitech.tib.user.model.TokenBlacklist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
  Optional<TokenBlacklist> findByToken(String token);

  boolean existsByToken(String token);
}
