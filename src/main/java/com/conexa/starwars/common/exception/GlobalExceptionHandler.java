package com.conexa.starwars.common.exception;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Translates exceptions thrown by any controller into RFC 9457 {@link ProblemDetail} responses,
 * so every error shares the same format.
 * <p>
 * Extends {@link ResponseEntityExceptionHandler} so Spring MVC's own exceptions (invalid parameters,
 * unknown routes, unsupported methods, etc.) are rendered as {@code ProblemDetail} too.
 * Failures of the upstream Star Wars API are reported as gateway errors (502/503/504) with a generic
 * message; technical details are only logged.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    ProblemDetail handleUpstreamError(HttpServerErrorException ex) {
        log.warn("Star Wars API responded with an error: {}", ex.getStatusCode());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "The Star Wars API responded with an error");
    }

    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail handleUpstreamUnavailable(ResourceAccessException ex) {
        log.warn("Star Wars API could not be reached: {}", ex.getMessage());
        if (isTimeout(ex)) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT,
                    "The Star Wars API did not respond in time");
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The Star Wars API is currently unavailable");
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

    /**
     * The HTTP client wraps timeouts in a generic I/O error, so the cause chain is inspected. Both the
     * JDK client's and the classic socket timeout types are checked to stay independent of the client in use.
     */
    private static boolean isTimeout(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof HttpTimeoutException || cause instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private record ParameterError(String parameter, String message) {
    }
}
