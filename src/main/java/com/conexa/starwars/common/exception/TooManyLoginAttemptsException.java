package com.conexa.starwars.common.exception;

import java.time.Duration;

/**
 * Signals that a username is temporarily blocked after too many failed logins.
 * Translated to an HTTP 429 {@code ProblemDetail} with a {@code Retry-After} header by {@link GlobalExceptionHandler}.
 */
public class TooManyLoginAttemptsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyLoginAttemptsException(Duration retryAfter) {
        super("Too many failed login attempts");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
