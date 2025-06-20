package io.github.codeystar.rateLimiter.web;

import io.github.codeystar.rateLimiter.config.RateLimiterProperties;
import io.github.codeystar.rateLimiter.exception.RateLimitException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author zhiyang.zhang
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitExceptionResolver {

    private final RateLimiterProperties limiterProperties;
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";


    public RateLimitExceptionResolver(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @ExceptionHandler(value = RateLimitException.class)
    public ResponseEntity<String> exceptionHandler(RateLimitException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(REMAINING_HEADER, String.valueOf(e.getExtra()));
        return ResponseEntity.status(limiterProperties.getStatusCode())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(limiterProperties.getResponseBody());
    }
}
