package io.github.codeystar.rateLimiter.core.impl;

import io.github.codeystar.rateLimiter.core.AbstractRateLimiter;
import io.github.codeystar.rateLimiter.model.LuaScriptModel;
import io.github.codeystar.rateLimiter.model.RateLimitResult;
import io.github.codeystar.rateLimiter.model.RateLimitRule;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhiyang.zhang
 */
@Component
public class TokenBucketRateLimiter extends AbstractRateLimiter {

    private final RScript rScript;

    public TokenBucketRateLimiter(RedissonClient client) {
        this.rScript = client.getScript(LongCodec.INSTANCE);
    }

    @Override
    public RateLimitResult isAllowed(RateLimitRule rateLimitRule) {
        List<Object> keys = getKeys(rateLimitRule.getKey());
        String script = LuaScriptModel.getTokenBucketRateLimiterScript();
        List<Long> results = rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.MULTI,
                keys,
                rateLimitRule.getRate(),
                rateLimitRule.getBucketCapacity(),
                rateLimitRule.getRequestedTokens());
        boolean isAllowed = results.get(0) == 1L;
        long newTokens = results.get(1);

        return new RateLimitResult(isAllowed, newTokens);
    }

    private static List<Object> getKeys(String key) {
        String prefix = "request_rate_limiter.{" + key;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }
}
