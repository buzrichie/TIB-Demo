package com.amalitech.tib.user.dto;

import com.amalitech.tib.admin.model.GeneralSettings;
import com.amalitech.tib.user.enums.UserStatus;
import com.amalitech.tib.user.model.AccountPreferences;
import com.amalitech.tib.user.model.Role;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto(
    UUID id,
    String firstName,
    String lastName,
    String email,
    UserStatus status,
    Instant lastActive,
    String note,
    Role defaultRole,
    Set<Role> roles,
    GeneralSettings generalSettings,
    AccountPreferences accountPreferences) {}
