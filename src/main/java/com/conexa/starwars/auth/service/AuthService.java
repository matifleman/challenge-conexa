package com.conexa.starwars.auth.service;

import com.conexa.starwars.common.exception.ResourceAlreadyExistsException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates an enabled user with the single {@code ROLE_USER} authority the user store requires (ADR 0015).
     * Uniqueness is enforced by the database, which also covers two concurrent registrations of the same username.
     *
     * @throws ResourceAlreadyExistsException if the username is already taken, ignoring case
     */
    @Transactional
    public void register(String username, String password) {
        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build();
        try {
            userDetailsManager.createUser(user);
        } catch (DuplicateKeyException ex) {
            throw new ResourceAlreadyExistsException("User", username);
        }
    }
}
