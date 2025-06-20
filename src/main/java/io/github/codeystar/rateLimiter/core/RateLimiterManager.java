package io.github.codeystar.rateLimiter.core;

import io.github.codeystar.rateLimiter.core.impl.FixedWindowRateLimiter;
import io.github.codeystar.rateLimiter.core.impl.LeakyBucketRateLimiter;
import io.github.codeystar.rateLimiter.core.impl.SlidingWindowRateLimiter;
import io.github.codeystar.rateLimiter.core.impl.TokenBucketRateLimiter;
import io.github.codeystar.rateLimiter.model.RateLimitModeEnum;
import io.github.codeystar.rateLimiter.model.RateLimitResult;
import io.github.codeystar.rateLimiter.model.RateLimitRule;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhiyang.zhang
 */
public class RateLimiterManager {

    private final Map<String, RateLimiterAlgorithm> rateLimiterFactory = new ConcurrentHashMap<>();

    public RateLimiterManager(RedissonClient redissonClient) {
        this(redissonClient, null);
    }

    public RateLimiterManager(RedissonClient redissonClient, Map<String, RateLimiterAlgorithm> customAlgorithms) {
        rateLimiterFactory.put(RateLimitModeEnum.FIXED_WINDOW.name(), new FixedWindowRateLimiter(redissonClient));
        rateLimiterFactory.put(RateLimitModeEnum.SLIDING_WINDOW.name(), new SlidingWindowRateLimiter(redissonClient));
        rateLimiterFactory.put(RateLimitModeEnum.TOKEN_BUCKET.name(), new TokenBucketRateLimiter(redissonClient));
        rateLimiterFactory.put(RateLimitModeEnum.LEAKY_BUCKET.name(), new LeakyBucketRateLimiter(redissonClient));

        if (!CollectionUtils.isEmpty(customAlgorithms)) {
            rateLimiterFactory.putAll(customAlgorithms);
        }
    }

    private RateLimiterAlgorithm getRateLimiter(String mode) {

        return rateLimiterFactory.get(mode);
    }

    public RateLimitResult isAllowed(RateLimitRule rateLimitRule) {
        String mode = rateLimitRule.getRateLimitModeEnum();
        RateLimiterAlgorithm rateLimiter = getRateLimiter(mode);
        if (rateLimiter == null) {
            throw new IllegalArgumentException("Unsupported rate limit mode: " + mode);
        }
        return rateLimiter.isAllowed(rateLimitRule);
    }
}
