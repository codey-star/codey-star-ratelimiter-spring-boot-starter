package io.github.codeystar.rateLimiter.core;

import io.github.codeystar.rateLimiter.annotation.RateLimit;
import io.github.codeystar.rateLimiter.exception.RateLimitException;
import io.github.codeystar.rateLimiter.model.RateLimitResult;
import io.github.codeystar.rateLimiter.model.RateLimitRule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author zhiyang.zhang
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitAspectHandler {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspectHandler.class);

    private final RateLimiterManager rateLimiterManager;

    private final RuleLoader ruleLoader;

    public RateLimitAspectHandler(RateLimiterManager lockInfoProvider, RuleLoader ruleLoader) {
        this.rateLimiterManager = lockInfoProvider;
        this.ruleLoader = ruleLoader;
    }

    @Around(value = "@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RateLimitRule rateLimitRule = ruleLoader.getRateLimiterRule(joinPoint, rateLimit);

        RateLimitResult rateLimitResult = rateLimiterManager.isAllowed(rateLimitRule);
        boolean allowed = rateLimitResult.isAllow();
        if (!allowed) {
            logger.info("Trigger current limiting,key:{}", rateLimitRule.getKey());
            if (StringUtils.hasLength(rateLimitRule.getFallbackFunction())) {
                return ruleLoader.executeFunction(rateLimitRule.getFallbackFunction(), joinPoint);
            }
            long extra = rateLimitResult.getExtra();
            throw new RateLimitException("Too Many Requests", extra);
        }
        return joinPoint.proceed();
    }

}
