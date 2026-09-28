package com.conexa.starwars.auth.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;

import com.conexa.starwars.auth.config.JwtProperties;
import com.conexa.starwars.auth.dto.TokenResponse;
import com.conexa.starwars.common.exception.ResourceAlreadyExistsException;
import com.conexa.starwars.common.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsManager userDetailsManager;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private LoginAttemptService loginAttemptService;

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final KeyPair keyPair = rsaKeyPair();
    private final JwtProperties jwtProperties = new JwtProperties("starwars-api", Duration.ofHours(1));
    private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDetailsManager, passwordEncoder, authenticationManager,
                NimbusJwtEncoder.withKeyPair((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate())
                        .build(),
                jwtProperties, loginAttemptService);
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

    @Test
    void loginIssuesSignedTokenForTheStoredUsername() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated("luke", null, List.of()));

        TokenResponse response = authService.login("LUKE", "password123");

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        Jwt jwt = jwtDecoder.decode(response.accessToken());
        assertThat(jwt.getSubject()).isEqualTo("luke");
        assertThat(jwt.getClaimAsString(JwtClaimNames.ISS)).isEqualTo("starwars-api");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(1));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("LUKE");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password123");
        verify(loginAttemptService).loginSucceeded("LUKE");
    }

    @Test
    void loginRecordsFailedAttemptAndPropagatesBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login("luke", "wrong-password"))
                .isInstanceOf(BadCredentialsException.class);
        verify(loginAttemptService).loginFailed("luke");
        verify(loginAttemptService, never()).loginSucceeded(any());
    }

    @Test
    void loginRejectsBlockedUsernameWithoutCheckingCredentials() {
        doThrow(new TooManyLoginAttemptsException(Duration.ofMinutes(15)))
                .when(loginAttemptService).checkNotBlocked("luke");

        assertThatThrownBy(() -> authService.login("luke", "password123"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
        verify(authenticationManager, never()).authenticate(any());
    }

    private static KeyPair rsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
