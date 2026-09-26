package com.conexa.starwars.common.exception;

/**
 * Signals that a requested resource does not exist.
 * Translated to an HTTP 404 {@code ProblemDetail} by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, int id) {
        super("%s with id %d not found".formatted(resource, id));
    }
}
