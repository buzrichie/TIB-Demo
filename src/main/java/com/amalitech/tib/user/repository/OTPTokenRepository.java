package com.amalitech.tib.user.repository;

import com.amalitech.tib.user.model.OTPToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OTPTokenRepository extends JpaRepository<OTPToken, UUID> {
  Optional<OTPToken> findByToken(String token);

  Optional<OTPToken> findByEmail(String email);
}
