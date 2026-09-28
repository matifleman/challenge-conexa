package com.conexa.starwars.auth.service;

import java.time.Instant;

import com.conexa.starwars.auth.config.JwtProperties;
import com.conexa.starwars.auth.dto.TokenResponse;
import com.conexa.starwars.common.exception.ResourceAlreadyExistsException;
import com.conexa.starwars.common.exception.TooManyLoginAttemptsException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, JwtProperties jwtProperties,
            LoginAttemptService loginAttemptService) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.loginAttemptService = loginAttemptService;
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

    /**
     * Verifies the credentials and issues a signed access token for the user. After too many consecutive failures
     * the username is blocked for a while, and its credentials are not checked until the block lifts (ADR 0021).
     *
     * @throws BadCredentialsException       if the username does not exist or the password is wrong
     * @throws TooManyLoginAttemptsException if the username is temporarily blocked
     */
    public TokenResponse login(String username, String password) {
        loginAttemptService.checkNotBlocked(username);
        Authentication authentication = authenticate(username, password);

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return TokenResponse.bearer(token, jwtProperties.expiration());
    }

    private Authentication authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
            loginAttemptService.loginSucceeded(username);
            return authentication;
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(username);
            throw ex;
        }
    }
}
