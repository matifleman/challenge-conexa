package com.conexa.starwars.auth.config;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Limit on failed logins, bound from {@code auth.login-attempts.*} properties.
 *
 * @param maxFailures   consecutive failed logins that block a username
 * @param blockDuration how long a username stays blocked, counted from its last failed login
 */
@Validated
@ConfigurationProperties(prefix = "auth.login-attempts")
public record LoginAttemptProperties(@Positive int maxFailures, @NotNull Duration blockDuration) {
}
