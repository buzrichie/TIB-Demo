package com.amalitech.tib.security;

import com.amalitech.tib.permission.model.Permission;
import com.amalitech.tib.role.model.Role;
import com.amalitech.tib.user.model.User;
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

        // Get all effective roles (assigned + default)
        Set<Role> effectiveRoles = user.getEffectiveRoles();

        for (Role role : effectiveRoles) {
            // Add role as an authority (Spring Security expects 'ROLE_' prefix)
            if (role != null && role.getName() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            }

            // Add permissions attached to the role
            if (role != null && role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.name()));
                }
            }
        }

        return authorities;
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
