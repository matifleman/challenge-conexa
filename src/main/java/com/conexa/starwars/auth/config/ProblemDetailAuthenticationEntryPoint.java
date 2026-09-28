package com.conexa.starwars.auth.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Answers requests without a valid bearer token with the same {@code ProblemDetail} body as every other error.
 * <p>
 * Token failures happen in the security filters, before Spring MVC, so {@code @RestControllerAdvice} never sees them.
 * The standard entry point sets the 401 status and the RFC 6750 {@code WWW-Authenticate} header; the exception is
 * then handed to Spring MVC's exception resolver, so {@code GlobalExceptionHandler} writes the body and the error
 * format stays defined in a single place.
 */
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final HandlerExceptionResolver handlerExceptionResolver;

    public ProblemDetailAuthenticationEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        bearerEntryPoint.commence(request, response, authException);
        handlerExceptionResolver.resolveException(request, response, null, authException);
    }
}
