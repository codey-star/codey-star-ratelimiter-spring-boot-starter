package io.github.codeystar.rateLimiter.exception;

/**
 * @author zhiyang.zhang
 */
public class RateLimitException extends RuntimeException {

    private final long extra;

    public RateLimitException(String message, long extra) {
        super(message);
        this.extra = extra;
    }

    public long getExtra() {
        return extra;
    }

}
