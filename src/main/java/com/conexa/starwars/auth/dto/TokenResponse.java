package com.conexa.starwars.auth.dto;

import java.time.Duration;

/**
 * Access token issued on login. Field names follow the OAuth 2.0 token response (RFC 6749), in camelCase like
 * the rest of the API.
 *
 * @param accessToken signed JWT to send in the {@code Authorization: Bearer} header
 * @param tokenType   always {@code Bearer}
 * @param expiresIn   lifetime of the token in seconds
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    public static TokenResponse bearer(String accessToken, Duration lifetime) {
        return new TokenResponse(accessToken, "Bearer", lifetime.toSeconds());
    }
}
