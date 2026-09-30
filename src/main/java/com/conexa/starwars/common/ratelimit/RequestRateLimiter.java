package com.conexa.starwars.common.ratelimit;

import java.time.Duration;
import java.util.Locale;

import com.conexa.starwars.common.exception.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * Limits how many requests each user can make per fixed window (ADR 0025), so a single user cannot exhaust the
 * SWAPI quota that all users share.
 * <p>
 * A user's window starts with their first request and its counter is dropped when the window ends; later requests
 * do not extend it. Counters are kept in memory, belong to this instance and are lost on restart.
 */
class RequestRateLimiter {

    // Bounds the memory taken by counters; far above the number of users expected at once
    private static final long MAX_TRACKED_USERS = 10_000;

    private final RateLimitProperties properties;
    private final Cache<String, Integer> requestCounts;

    RequestRateLimiter(RateLimitProperties properties) {
        this(properties, Ticker.systemTicker());
    }

    RequestRateLimiter(RateLimitProperties properties, Ticker ticker) {
        this.properties = properties;
        this.requestCounts = Caffeine.newBuilder()
                .expireAfter(Expiry.<String, Integer>creating((username, count) -> properties.window()))
                .maximumSize(MAX_TRACKED_USERS)
                .ticker(ticker)
                .build();
    }

    /**
     * Counts a request from the given user.
     *
     * @throws TooManyRequestsException if the user went over the limit in the current window
     */
    void checkAllowed(String username) {
        // Usernames are case-insensitive (citext), so "LUKE" and "luke" must share one counter
        String key = username.toLowerCase(Locale.ROOT);
        int count = requestCounts.asMap().merge(key, 1, Integer::sum);
        if (count > properties.maxRequests()) {
            throw new TooManyRequestsException("Too many requests. Try again later.", timeLeftInWindow(key));
        }
    }

    private Duration timeLeftInWindow(String key) {
        return requestCounts.policy().expireVariably()
                .flatMap(expiration -> expiration.getExpiresAfter(key))
                .orElse(properties.window());
    }
}
