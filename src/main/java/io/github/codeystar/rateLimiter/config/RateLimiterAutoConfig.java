package io.github.codeystar.rateLimiter.config;

import io.github.codeystar.rateLimiter.core.RateLimitAspectHandler;
import io.github.codeystar.rateLimiter.core.RateLimiterAlgorithm;
import io.github.codeystar.rateLimiter.core.RateLimiterManager;
import io.github.codeystar.rateLimiter.core.RuleLoader;
import io.github.codeystar.rateLimiter.model.RateLimitModeEnum;
import io.github.codeystar.rateLimiter.web.RateLimitExceptionResolver;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhiyang.zhang
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimitAspectHandler.class, RateLimitExceptionResolver.class})
public class RateLimiterAutoConfig {

    private final RateLimiterProperties limiterProperties;
    public final static String REDISSON_BEAN_NAME = "rateLimiterRedissonBeanClass";

    public RateLimiterAutoConfig(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

    @Bean(name = REDISSON_BEAN_NAME, destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        if (limiterProperties.getClusterServer() != null
                && limiterProperties.getClusterServer().getNodeAddresses().length > 0) {
            config.useClusterServers().setPassword(limiterProperties.getPassword())
                    .addNodeAddress(limiterProperties.getClusterServer().getNodeAddresses());
        } else {
            config.useSingleServer().setAddress(limiterProperties.getAddress())
                    .setDatabase(limiterProperties.getDatabase())
                    .setPassword(limiterProperties.getPassword());
        }
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public RuleLoader bizKeyProvider() {
        return new RuleLoader();
    }

    @Bean
    public RateLimiterManager rateLimiterManager(Map<String, RateLimiterAlgorithm> customAlgorithms) {
        return new RateLimiterManager(redisson(), customAlgorithms);
    }

    @Bean
    @ConditionalOnMissingBean
    public Map<RateLimitModeEnum, RateLimiterAlgorithm> customAlgorithms() {
        return new HashMap<>();
    }
}
