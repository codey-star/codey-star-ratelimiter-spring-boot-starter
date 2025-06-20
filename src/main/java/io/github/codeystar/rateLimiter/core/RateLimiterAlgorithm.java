package io.github.codeystar.rateLimiter.core;

import io.github.codeystar.rateLimiter.model.RateLimitResult;
import io.github.codeystar.rateLimiter.model.RateLimitRule;

/**
 * @author zhiyang.zhang
 */
public interface RateLimiterAlgorithm {
    RateLimitResult isAllowed(RateLimitRule rule);
}
