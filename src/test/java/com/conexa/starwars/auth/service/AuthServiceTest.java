package com.conexa.starwars.auth.service;

import com.conexa.starwars.common.exception.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsManager userDetailsManager;

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDetailsManager, passwordEncoder);
    }

    @Test
    void registerStoresEnabledUserWithHashedPasswordAndUserAuthority() {
        authService.register("luke", "password123");

        ArgumentCaptor<UserDetails> captor = ArgumentCaptor.forClass(UserDetails.class);
        verify(userDetailsManager).createUser(captor.capture());
        UserDetails user = captor.getValue();
        assertThat(user.getUsername()).isEqualTo("luke");
        assertThat(user.getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void registerRejectsTakenUsername() {
        doThrow(new DuplicateKeyException("duplicate key")).when(userDetailsManager).createUser(any());

        assertThatThrownBy(() -> authService.register("luke", "password123"))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("User 'luke' already exists");
    }
}
