package com.conexa.starwars.common.exception;

/**
 * Signals that a resource cannot be created because another one with the same identifier already exists.
 * Translated to an HTTP 409 {@code ProblemDetail} by {@link GlobalExceptionHandler}.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String resource, String identifier) {
        super("%s '%s' already exists".formatted(resource, identifier));
    }
}
