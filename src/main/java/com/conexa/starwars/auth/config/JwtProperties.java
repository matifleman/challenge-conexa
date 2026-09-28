package com.conexa.starwars.auth.config;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Settings of the access tokens issued by the API, bound from {@code jwt.*} properties.
 *
 * @param issuer     value of the {@code iss} claim, verified on every request
 * @param expiration lifetime of an access token
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(@NotBlank String issuer, @NotNull Duration expiration) {
}
