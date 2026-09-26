package com.conexa.starwars.common.exception;

import org.springframework.http.*;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Translates exceptions thrown by any controller into RFC 9457 {@link ProblemDetail} responses,
 * so every error shares the same format.
 * <p>
 * Extends {@link ResponseEntityExceptionHandler} so Spring MVC's own exceptions (invalid parameters,
 * unknown routes, unsupported methods, etc.) are rendered as {@code ProblemDetail} too.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Adds an {@code errors} property listing each invalid parameter, so clients know what to fix
     * instead of receiving a generic "Validation failure".
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ParameterError> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ParameterError(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();

        ProblemDetail body = ex.getBody();
        body.setDetail("Invalid request parameters");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private record ParameterError(String parameter, String message) {
    }
}
