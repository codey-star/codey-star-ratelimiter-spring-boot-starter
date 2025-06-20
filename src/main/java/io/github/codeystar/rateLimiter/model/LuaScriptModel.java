package io.github.codeystar.rateLimiter.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author zhiyang.zhang
 */
public final class LuaScriptModel {

    private LuaScriptModel() {
    }

    private static final Logger log = LoggerFactory.getLogger(LuaScriptModel.class);
    private static final String fixedWindowRateLimiterScript = getRateLimiterScript("META-INF/fixedWindow-rateLimit.lua");
    private static final String slidingWindowRateLimiterScript = getRateLimiterScript("META-INF/slidingWindow-rateLimit.lua");
    private static final String tokenBucketRateLimiterScript = getRateLimiterScript("META-INF/tokenBucket-rateLimit.lua");
    private static final String leakBucketRateLimiterScript = getRateLimiterScript("META-INF/leakBucket-rateLimit.lua");

    private static String getRateLimiterScript(String scriptFileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(scriptFileName);
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("tokenBucket-rateLimit.lua Initialization failure", e);
            throw new RuntimeException(e);
        }
    }

    public static String getFixedWindowRateLimiterScript() {
        return fixedWindowRateLimiterScript;
    }

    public static String getSlidingWindowRateLimiterScript() {
        return slidingWindowRateLimiterScript;
    }

    public static String getLeakBucketRateLimiterScript() {
        return leakBucketRateLimiterScript;
    }

    public static String getTokenBucketRateLimiterScript() {
        return tokenBucketRateLimiterScript;
    }
}
