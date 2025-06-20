package io.github.codeystar.rateLimiter.model;

/**
 * @author zhiyang.zhang
 */
public enum RateLimitModeEnum {
    /**
     * 固定窗口
     */
    FIXED_WINDOW,
    /**
     * 时间窗口
     */
    SLIDING_WINDOW,
    /**
     * 令牌桶
     */
    TOKEN_BUCKET,
    /**
     * 漏桶
     */
    LEAKY_BUCKET
}
