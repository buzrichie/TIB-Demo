package com.amalitech.tib.authentication.auth.repository;

import com.amalitech.tib.authentication.auth.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenBlacklistRepository  extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);

    boolean existsByToken(String token);
}
