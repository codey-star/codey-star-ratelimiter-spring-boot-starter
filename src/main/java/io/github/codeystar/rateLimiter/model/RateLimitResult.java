package io.github.codeystar.rateLimiter.model;

/**
 * @author zhiyang.zhang
 */
public class RateLimitResult {
    private boolean isAllow;
    private Long extra;

    public RateLimitResult(boolean isAllow, Long extra) {
        this.isAllow = isAllow;
        this.extra = extra;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public void setAllow(boolean allow) {
        isAllow = allow;
    }

    public Long getExtra() {
        return extra;
    }

    public void setExtra(Long extra) {
        this.extra = extra;
    }
}
