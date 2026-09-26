package com.conexa.starwars.common.swapi;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection settings for the external Star Wars API (SWAPI), bound from {@code swapi.*} properties.
 * <p>
 * Externalized so the target can change per environment (e.g. a mock server in tests)
 * without code changes, and validated at startup to fail fast on missing configuration.
 *
 * @param baseUrl root URL of the SWAPI REST API
 */
@Validated
@ConfigurationProperties(prefix = "swapi")
public record SwapiProperties(@NotBlank String baseUrl) {
}
