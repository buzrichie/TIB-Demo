package com.amalitech.tib.user.repository;

import com.amalitech.tib.user.model.Role;
import com.amalitech.tib.user.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  @Query(
      "SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.email = :email")
  boolean existsByEmail(@Param("email") String email);

  Optional<User> findByEmail(String email);

  Optional<User> findByDefaultRole(Role defaultRole);

  Optional<User> findById(UUID id);
}
