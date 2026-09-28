package com.conexa.starwars.auth.config;

import com.conexa.starwars.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the configured user store against the real schema created by Flyway on PostgreSQL.
 */
@JdbcTest
@Import({TestcontainersConfiguration.class, UserConfig.class})
class UserConfigTest {

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createdUserIsLoadedWithHashedPasswordAndUserAuthority() {
        userDetailsManager.createUser(user("luke"));

        UserDetails loaded = userDetailsManager.loadUserByUsername("luke");

        assertThat(loaded.getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("password123", loaded.getPassword())).isTrue();
        assertThat(loaded.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
        assertThat(loaded.isEnabled()).isTrue();
    }

    @Test
    void usernamesAreCaseInsensitive() {
        userDetailsManager.createUser(user("luke"));

        assertThat(userDetailsManager.userExists("LUKE")).isTrue();
        assertThat(userDetailsManager.loadUserByUsername("Luke").getUsername()).isEqualTo("luke");
    }

    @Test
    void usernameDifferingOnlyInCaseIsRejectedAsDuplicate() {
        userDetailsManager.createUser(user("luke"));

        assertThatThrownBy(() -> userDetailsManager.createUser(user("Luke")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    private UserDetails user(String username) {
        return User.withUsername(username)
                .password(passwordEncoder.encode("password123"))
                .roles("USER")
                .build();
    }
}
