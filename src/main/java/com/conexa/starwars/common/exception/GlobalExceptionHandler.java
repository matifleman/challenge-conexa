package com.conexa.starwars.common.exception;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
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

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    ProblemDetail handleAlreadyExists(ResourceAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * An unknown username and a wrong password get the same response, so it cannot be used to find out
     * which users exist.
     */
    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    /**
     * Any other authentication failure, such as a missing or invalid bearer token (forwarded by the security
     * entry point). Handled explicitly so it is not reported as a 500 by the catch-all handler.
     */
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Authentication is required to access this resource");
    }

    /**
     * Tells the client when it may try again, in seconds, through the standard {@code Retry-After} header.
     */
    @ExceptionHandler(TooManyRequestsException.class)
    ResponseEntity<ProblemDetail> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(secondsRoundedUp(ex.getRetryAfter())))
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()));
    }

    /**
     * SWAPI limits requests per IP, and every user of this API shares the server's IP: reaching that limit is not
     * the client's fault, so it is reported as a temporary unavailability (503) instead of a 429. SWAPI's
     * {@code Retry-After} is forwarded when present.
     */
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    ResponseEntity<ProblemDetail> handleUpstreamRateLimit(HttpClientErrorException.TooManyRequests ex) {
        log.warn("Star Wars API rate limit reached");
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE);
        HttpHeaders upstreamHeaders = ex.getResponseHeaders();
        String retryAfter = upstreamHeaders == null ? null : upstreamHeaders.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter != null) {
            response.header(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return response.body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The Star Wars API is temporarily limiting requests. Try again later."));
    }

    /**
     * Any other HTTP error from SWAPI, including a 4xx: it means SWAPI rejected a request built by this API,
     * not that the client did something wrong. Services translate the 404s that mean "not found" before this point.
     */
    @ExceptionHandler(HttpStatusCodeException.class)
    ProblemDetail handleUpstreamError(HttpStatusCodeException ex) {
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
     * Last-resort handler for unexpected errors: the client gets a generic 500 without internal details,
     * while the full stack trace is logged for diagnosis.
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
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
     * Same {@code errors} format as invalid parameters, applied to the fields of an invalid request body.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ParameterError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ParameterError(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail body = ex.getBody();
        body.setDetail("Invalid request body");
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

    // Rounded up so the client never retries a moment before the limit lifts
    private static long secondsRoundedUp(Duration duration) {
        return duration.toNanosPart() == 0 ? duration.toSeconds() : duration.toSeconds() + 1;
    }

    private record ParameterError(String parameter, String message) {
    }
}
