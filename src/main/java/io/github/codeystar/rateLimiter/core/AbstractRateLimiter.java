package io.github.codeystar.rateLimiter.core;


import io.github.codeystar.rateLimiter.model.RateLimitResult;
import io.github.codeystar.rateLimiter.model.RateLimitRule;

/**
 * @author zhiyang.zhang
 */
public abstract class AbstractRateLimiter implements RateLimiterAlgorithm {

    @Override
    public abstract RateLimitResult isAllowed(RateLimitRule rule);
}
