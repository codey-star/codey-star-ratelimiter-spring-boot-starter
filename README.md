# RateLimiter Spring Boot Starter

## 项目简介

RateLimiter Spring Boot Starter 是一个基于 Spring Boot 的限流组件，提供了多种限流策略，可以轻松地在 Spring Boot 项目中实现接口级别的限流功能。

## 功能特性

- 支持多种限流模式：
  - 固定窗口（Fixed Window）
  - 滑动窗口（Sliding Window）
  - 令牌桶（Token Bucket）
  - 漏桶（Leaky Bucket）
- 基于 Redis 实现分布式限流
- 支持自定义限流规则
- 支持自定义限流后的回退逻辑
- 支持动态配置限流参数

## 快速开始

### 1. 添加依赖

在你的 Spring Boot 项目的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.codey-star</groupId>
    <artifactId>ratelimiter-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. 配置 Redis

在 `application.properties` 或 `application.yml` 中配置 Redis 连接信息：

```properties
spring.ratelimiter.redis.address=redis://127.0.0.1:6379
spring.ratelimiter.redis.password=xxx
```

### 3. 使用 @RateLimit 注解

限流功能会自动启用，你不需要额外的配置。直接在需要限流的方法上添加 `@RateLimit` 注解：

在需要限流的方法上添加 `@RateLimit` 注解：

```java

@RestController
public class ExampleController {

  @GetMapping("/example")
  @RateLimit(rate = 10, rateInterval = "1m")
  public String example() {
    return "Hello, World!";
  }

}
```

## 配置说明

`@RateLimit` 注解支持以下参数：

- `mode`：限流模式，默认为 `SLIDING_WINDOW`
- `rate`：限流速率
- `rateInterval`：时间窗口，如 "1s"、"1m"、"1h"
- `keys`：自定义限流 key
- `fallbackFunction`：限流后的回退方法名
- `customKeyFunction`：自定义业务 key 的方法名
- `rateExpression`：动态限流速率表达式
- `bucketCapacity`：令牌桶容量（仅用于令牌桶模式）
- `bucketCapacityExpression`：动态令牌桶容量表达式
- `requestedTokens`：每次请求消耗的令牌数（仅用于令牌桶模式）
- `leakyBucketRate`: 漏桶出水速率（每秒处理的请求数）
## 使用示例

### 1. 基本使用

```java
@GetMapping("/basic")
@RateLimit(rate = 100, rateInterval = "1m")
public String basicExample() {
    return "Basic rate limiting";
}
```

### 2. 使用固定窗口模式

```java
@GetMapping("/fixed-window")
@RateLimit(mode = "FIXED_WINDOW", rate = 100, rateInterval = "1m")
public String fixedWindowExample() {
    return "Fixed window rate limiting";
}
```

### 3. 使用滑动窗口模式

```java
@GetMapping("/sliding-window")
@RateLimit(mode = "SLIDING_WINDOW", rate = 10, rateInterval = "1s")
public String slidingWindowExample() {
    return "Sliding window rate limiting";
}
```

### 4. 使用令牌桶模式

```java
@GetMapping("/token-bucket")
@RateLimit(mode = "TOKEN_BUCKET", rate = 10, bucketCapacity = 100, requestedTokens = 1)
public String tokenBucketExample() {
    return "Token bucket rate limiting";
}
```

### 5. 使用漏桶模式

```java
@GetMapping("/leaky-bucket")
@RateLimit(mode = "LEAKY_BUCKET", bucketCapacity = 100, leakyBucketRate = 10.0)
public String leakyBucketExample() {
    return "Leaky bucket rate limiting";
}
```

### 6. 自定义限流 key

```java
@GetMapping("/custom-key")
@RateLimit(rate = 5, rateInterval = "1m", keys = {"#user.id"})
public String customKeyExample(@RequestBody User user) {
    return "Custom key rate limiting for user: " + user.getId();
}
```

### 7. 使用回退方法

```java
@GetMapping("/fallback")
@RateLimit(rate = 5, rateInterval = "1m", fallbackFunction = "fallbackMethod")
public String fallbackExample() {
    return "Normal response";
}

public String fallbackMethod(ProceedingJoinPoint joinPoint) {
    return "Rate limit exceeded, please try again later";
}
```

### 5. 自定义限流模式

你可以通过实现 `CustomRateLimitMode` 接口来创建自定义的限流模式：

```java
public class MyCustomRateLimitMode implements CustomRateLimitMode {
    @Override
    public String getModeName() {
        return "MY_CUSTOM_MODE";
    }
}
```

然后，实现对应的 `RateLimiterAlgorithm`：

```java
public class MyCustomRateLimiter implements RateLimiterAlgorithm {

    @Override
    public RateLimitResult isAllowed(RateLimitRule rule) {
        // 实现你的自定义限流逻辑
    }
}
```

在你的配置类中注册这个自定义算法：

```java
@Configuration
public class RateLimiterConfig {

    @Bean
    public Map<String, RateLimiterAlgorithm> customAlgorithms() {
        Map<String, RateLimiterAlgorithm> algorithms = new HashMap<>();
        MyCustomRateLimitMode customMode = new MyCustomRateLimitMode();
        algorithms.put(customMode.getModeName(), new MyCustomRateLimiter());
        return algorithms;
    }
}
```

现在你可以在 `@RateLimit` 注解中使用你的自定义模式：

```java
@GetMapping("/custom")
@RateLimit(mode = "MY_CUSTOM_MODE", rate = 10, rateInterval = "1m")
public String customExample() {
    return "Custom rate limiting";
}
```

## 注意事项

1. 确保 Redis 服务器已正确配置并可用。
2. 合理设置限流参数，避免过度限流或限流不足。
3. 在分布式环境中，确保所有服务实例使用相同的 Redis 实例来保证限流的一致性。
4. 对于高并发场景，建议使用 Redis 集群来提高性能和可用性。
5. 定期监控和调整限流规则，以适应业务需求的变化。
6. 在实现自定义限流算法时，确保算法的性能和正确性，并进行充分的测试。
7. 使用自定义限流模式时，确保在所有需要使用该模式的服务中都正确配置和注册了自定义算法。
