package com.amalitech.tib.user.model;

import com.amalitech.tib.accountpreferences.model.AccountPreferences;
import com.amalitech.tib.generalsettings.model.GeneralSettings;
import com.amalitech.tib.notification.model.Notification;
import com.amalitech.tib.role.model.Role;
import com.amalitech.tib.shared.BaseEntity;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends BaseEntity {

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Instant lastActive;

    @Lob
    private String note;

    /**
     * Default role that user always retains (fallback)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_role_id")
    private Role defaultRole;

    /**
     * Additional roles assigned to the user.
     * Use @ManyToMany instead of @OneToMany to avoid duplicate role entities.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Trip> createdTrips;

    @OneToMany(mappedBy = "user")
    private Set<Notification> notifications;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GeneralSettings generalSettings;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AccountPreferences accountPreferences;

    /**
     * Convenience method that ensures the user always has their default role,
     * even if custom roles are lost.
     */
    public Set<Role> getEffectiveRoles() {
        Set<Role> effective = new HashSet<>(roles);
        if (defaultRole != null) {
            effective.add(defaultRole);
        }
        return effective;
    }
}
