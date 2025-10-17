package com.amalitech.tib.authentication.user.dto;

import com.amalitech.tib.authentication.user.model.AccountPreferences;
import com.amalitech.tib.admin.model.GeneralSettings;
import com.amalitech.tib.notification.model.Notification;
import com.amalitech.tib.authentication.user.model.Role;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.authentication.user.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String email,
        UserStatus status,
        Instant lastActive,
        String note,
        Role defaultRole,
        Set<Role> roles,
        Set<Trip> createdTrips,
        Set<Notification> notifications,
        GeneralSettings generalSettings,
        AccountPreferences accountPreferences
) {
}
