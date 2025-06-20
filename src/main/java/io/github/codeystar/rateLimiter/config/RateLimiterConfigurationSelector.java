package io.github.codeystar.rateLimiter.config;

import io.github.codeystar.rateLimiter.web.RateLimitExceptionResolver;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author zhangzhiyang
 */
public class RateLimiterConfigurationSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{RateLimiterAutoConfig.class.getName(), RateLimitExceptionResolver.class.getName()};
    }
}
