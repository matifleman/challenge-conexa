package com.conexa.starwars.auth.service;

import java.util.Locale;

import com.conexa.starwars.auth.config.LoginAttemptProperties;
import com.conexa.starwars.common.exception.TooManyLoginAttemptsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Counts failed logins per username and blocks further attempts once the limit is reached (ADR 0021).
 * <p>
 * Counters are kept in memory and expire {@code blockDuration} after the last failure, so a block lifts on its own.
 * They belong to this instance and are lost on restart.
 */
@Service
public class LoginAttemptService {

    // Bounds the memory an attack with random usernames can take
    private static final long MAX_TRACKED_USERNAMES = 10_000;

    private final LoginAttemptProperties properties;
    private final Cache<String, Integer> failures;

    @Autowired
    public LoginAttemptService(LoginAttemptProperties properties) {
        this(properties, Ticker.systemTicker());
    }

    LoginAttemptService(LoginAttemptProperties properties, Ticker ticker) {
        this.properties = properties;
        this.failures = Caffeine.newBuilder()
                .expireAfterWrite(properties.blockDuration())
                .maximumSize(MAX_TRACKED_USERNAMES)
                .ticker(ticker)
                .build();
    }

    /**
     * @throws TooManyLoginAttemptsException if the username reached the failure limit and is still blocked
     */
    public void checkNotBlocked(String username) {
        Integer count = failures.getIfPresent(key(username));
        if (count != null && count >= properties.maxFailures()) {
            throw new TooManyLoginAttemptsException(properties.blockDuration());
        }
    }

    public void loginFailed(String username) {
        failures.asMap().merge(key(username), 1, Integer::sum);
    }

    public void loginSucceeded(String username) {
        failures.invalidate(key(username));
    }

    // Usernames are case-insensitive (citext), so "LUKE" and "luke" must share one counter
    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
