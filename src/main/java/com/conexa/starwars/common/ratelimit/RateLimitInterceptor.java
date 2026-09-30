package com.conexa.starwars.common.ratelimit;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Applies the per-user request limit before the controller runs. Requests arrive already authenticated, since
 * Spring Security's filters run earlier; an exception thrown here is rendered by the global exception handler.
 */
class RateLimitInterceptor implements HandlerInterceptor {

    private final RequestRateLimiter rateLimiter;

    RateLimitInterceptor(RequestRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Principal user = request.getUserPrincipal();
        if (user != null) {
            rateLimiter.checkAllowed(user.getName());
        }
        return true;
    }
}
