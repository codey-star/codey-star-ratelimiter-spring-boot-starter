package io.github.codeystar.rateLimiter.core.impl;

import io.github.codeystar.rateLimiter.core.AbstractRateLimiter;
import io.github.codeystar.rateLimiter.model.LuaScriptModel;
import io.github.codeystar.rateLimiter.model.RateLimitResult;
import io.github.codeystar.rateLimiter.model.RateLimitRule;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collections;
import java.util.List;

import static io.github.codeystar.rateLimiter.config.RateLimiterAutoConfig.REDISSON_BEAN_NAME;

/**
 * @author zhiyang.zhang
 */
public class SlidingWindowRateLimiter extends AbstractRateLimiter {

    private final RScript rScript;

    public SlidingWindowRateLimiter(@Qualifier(REDISSON_BEAN_NAME) RedissonClient client) {
        this.rScript = client.getScript(LongCodec.INSTANCE);
    }

    @Override
    public RateLimitResult isAllowed(RateLimitRule rateLimitRule) {
        List<Object> keys = getKeys(rateLimitRule.getKey());
        String script = LuaScriptModel.getSlidingWindowRateLimiterScript();
        List<Long> results = rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.MULTI,
                keys,
                rateLimitRule.getRate(),
                rateLimitRule.getRateInterval(),
                System.currentTimeMillis() / 1000
        );
        boolean isAllowed = results.get(0) == 1L;
        long ttl = results.get(1);

        return new RateLimitResult(isAllowed, ttl);
    }

    private static List<Object> getKeys(String key) {
        String prefix = "request_rate_limiter.{" + key;
        String keys = prefix + "}";
        return Collections.singletonList(keys);
    }
}
