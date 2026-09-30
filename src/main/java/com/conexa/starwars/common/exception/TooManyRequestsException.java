package com.conexa.starwars.common.exception;

import java.time.Duration;

/**
 * Signals that the client must wait before trying again, such as a username blocked after too many failed logins
 * or a user over the request limit. Translated to an HTTP 429 {@code ProblemDetail} with a {@code Retry-After}
 * header by {@link GlobalExceptionHandler}; the message is shown to the client as the problem's detail.
 */
public class TooManyRequestsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyRequestsException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
