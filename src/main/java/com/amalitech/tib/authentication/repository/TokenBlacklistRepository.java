package com.amalitech.tib.authentication.repository;

import com.amalitech.tib.authentication.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenBlacklistRepository  extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);

    boolean existsByToken(String token);
}
