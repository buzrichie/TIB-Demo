package com.amalitech.tib.user.model;

import com.amalitech.tib.admin.model.GeneralSettings;
import com.amalitech.tib.notification.model.Notification;
import com.amalitech.tib.trip.model.Trip;
import com.amalitech.tib.user.enums.UserStatus;
import com.amalitech.tib.util.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends BaseEntity {

  private String firstName;

  private String lastName;

  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private UserStatus status;

  private Instant lastActive;

  @Column(columnDefinition = "TEXT")
  private String note;

  private String provider;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "default_role_id")
  private Role defaultRole;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "user")
  private Set<Trip> createdTrips;

  @OneToMany(mappedBy = "user")
  private Set<Notification> notifications;

  @Column(name = "profile_image_url")
  private String profileImageUrl;

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
