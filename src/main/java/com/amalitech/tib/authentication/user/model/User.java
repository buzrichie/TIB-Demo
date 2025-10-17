package com.amalitech.tib.authentication.user.model;

import com.amalitech.tib.admin.model.GeneralSettings;
import com.amalitech.tib.notification.model.Notification;
import com.amalitech.tib.shared.util.BaseEntity;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.authentication.user.enums.UserStatus;
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


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_role_id")
    private Role defaultRole;

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

    @Column(name = "profile_image_url")
    private String profile_image_url;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GeneralSettings generalSettings;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AccountPreferences accountPreferences;

    public Set<Role> getEffectiveRoles() {
        Set<Role> effective = new HashSet<>(roles);
        if (defaultRole != null) {
            effective.add(defaultRole);
        }
        return effective;
    }
}
