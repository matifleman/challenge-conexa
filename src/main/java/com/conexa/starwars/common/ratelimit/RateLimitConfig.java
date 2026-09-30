package com.conexa.starwars.common.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the per-user request limit on the endpoints backed by SWAPI. Authentication endpoints are left out:
 * they do not call SWAPI, and failed logins have their own limit (ADR 0021).
 * <p>
 * The limiter is created here instead of being a bean of its own because {@code @WebMvcTest} slices load
 * {@link WebMvcConfigurer} classes but not services: this keeps every controller test working without extra setup.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig implements WebMvcConfigurer {

    private final RequestRateLimiter rateLimiter;

    RateLimitConfig(RateLimitProperties properties) {
        this.rateLimiter = new RequestRateLimiter(properties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/**");
    }
}
