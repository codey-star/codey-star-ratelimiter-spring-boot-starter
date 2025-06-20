package io.github.codeystar.rateLimiter.annotation;

import io.github.codeystar.rateLimiter.config.RateLimiterConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhiyang.zhang
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RateLimiterConfigurationSelector.class})
public @interface EnableRateLimiter {
}
