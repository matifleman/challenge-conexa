package com.conexa.starwars.common.ratelimit;

import java.time.Duration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Limit on requests per user to the endpoints backed by SWAPI, bound from {@code rate-limit.*} properties.
 *
 * @param maxRequests requests a user can make within one window
 * @param window      length of the window, counted from the user's first request in it
 */
@Validated
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(@Positive int maxRequests, @NotNull Duration window) {
}
