package com.amalitech.tib.auth.repository;

import com.amalitech.tib.auth.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenBlacklistRepository  extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);

    boolean existsByToken(String token);
}
