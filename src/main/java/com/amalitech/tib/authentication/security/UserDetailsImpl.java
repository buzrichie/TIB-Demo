package com.amalitech.tib.security;

import com.amalitech.tib.authentication.user.enums.Permission;
import com.amalitech.tib.authentication.user.model.Role;
import com.amalitech.tib.authentication.user.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.*;


@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private User user;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        Set<Role> effectiveRoles = user.getEffectiveRoles();

        for (Role role : effectiveRoles) {
            if (role != null && role.getName() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            }

            if (role != null && role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.name()));
                }
            }
        }

        return authorities;
    }


    public User getUser() {
        return user;
    }

    public UUID getId() { return user.getId(); }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(user.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
