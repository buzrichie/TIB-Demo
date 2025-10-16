package com.amalitech.tib.user.dto;

import com.amalitech.tib.accountpreferences.model.AccountPreferences;
import com.amalitech.tib.generalsettings.model.GeneralSettings;
import com.amalitech.tib.notification.model.Notification;
import com.amalitech.tib.role.model.Role;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.user.enums.UserStatus;

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
