package com.amalitech.tib.security;

import com.amalitech.tib.user.UserRepository;
import com.amalitech.tib.user.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(UUID.fromString(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new UserDetailsImpl(user);
    }

    public UserDetails loadUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
        return new UserDetailsImpl(user);
    }
}

