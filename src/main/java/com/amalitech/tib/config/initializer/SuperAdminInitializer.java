package com.amalitech.tib.config.initializer;

import com.amalitech.tib.user.enums.UserStatus;
import com.amalitech.tib.user.model.Role;
import com.amalitech.tib.user.model.User;
import com.amalitech.tib.user.repository.RoleRepository;
import com.amalitech.tib.user.repository.UserRepository;
import com.amalitech.tib.user.security.provider.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Ensures a Super Admin user exists after role initialization. */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class SuperAdminInitializer implements CommandLineRunner {

  @Value("${admin.default.password}")
  private String defaultAdminPassword;

  @Value("${admin.default.email}")
  private String defaultAdminEmail;

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicyService passwordPolicyService;

  @Override
  public void run(String... args) {

    Role superAdminRole =
        roleRepository
            .findByName("SUPER_ADMIN")
            .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found."));

    if (userRepository.findByDefaultRole(superAdminRole).isPresent()) {
      log.info("Super Admin already exists — skipping creation.");
      return;
    }

    String resolvedPassword = passwordPolicyService.resolvePassword(defaultAdminPassword);

    User superAdmin = new User();
    superAdmin.setUsername("System Super Admin");
    superAdmin.setEmail(defaultAdminEmail);
    superAdmin.setPassword(passwordEncoder.encode(resolvedPassword));
    superAdmin.setStatus(UserStatus.valueOf("ACTIVE"));
    superAdmin.getRoles().add(superAdminRole);
    superAdmin.setDefaultRole(superAdminRole);
    userRepository.save(superAdmin);

    log.info("Created default Super Admin account");
  }
}
