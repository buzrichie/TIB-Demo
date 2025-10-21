package com.amalitech.tib.config.initializer;

import com.amalitech.tib.auth.enums.Permission;
import com.amalitech.tib.auth.dto.CreateRoleDto;
import com.amalitech.tib.auth.mapper.RoleMapper;
import com.amalitech.tib.auth.model.Role;
import com.amalitech.tib.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Initializes default system roles and assigns permissions at application startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    /**
     * Defines the default roles and their corresponding permissions.
     */
    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            "USER", EnumSet.of(
                    Permission.MANAGE_TRIP,
                    Permission.MANAGE_BOOKING,
                    Permission.MANAGE_FEEDBACK
            ),
            "ADMIN", EnumSet.of(
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_ROLES,
                    Permission.MANAGE_PERMISSIONS,
                    Permission.MANAGE_CATEGORY,
                    Permission.MANAGE_ATTRACTION,
                    Permission.MANAGE_TRIP,
                    Permission.MANAGE_ACTIVITY,
                    Permission.MANAGE_BUDGET,
                    Permission.MANAGE_EXPENSE,
                    Permission.EXPORT_REPORT
            ),
            "SUPER_ADMIN", EnumSet.allOf(Permission.class)
    );

    @Override
    public void run(String... args) {
        log.info("🔄 Starting Role & Permission Initialization...");

        ROLE_PERMISSIONS.forEach((roleName, permissions) -> {
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                CreateRoleDto dto = new CreateRoleDto(roleName);
                Role newRole = roleMapper.fromCreateRoleDto(dto);
                newRole.setPermissions(permissions);
                Role saved = roleRepository.save(newRole);
                log.info("✅ Created default role: {} with {} permissions", saved.getName(), permissions.size());
                return saved;
            });

            // If role already exists, ensure permissions are up to date
            if (role.getPermissions() == null || !role.getPermissions().equals(permissions)) {
                role.setPermissions(permissions);
                roleRepository.save(role);
                log.info("🔁 Updated role '{}' with latest permissions ({} total)", role.getName(), permissions.size());
            }
        });

        log.info("✅ Role & Permission initialization completed.");
    }
}
