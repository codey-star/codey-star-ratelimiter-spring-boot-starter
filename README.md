# RateLimiter Spring Boot Starter

## 项目简介

RateLimiter Spring Boot Starter 是一个轻量级的分布式限流组件，基于 Spring Boot 框架开发，提供细粒度的接口限流能力。支持多种限流算法，可通过简单配置即可实现接口级别的流量控制。

## 功能特性

- 支持多种限流模式：
  - 固定窗口（Fixed Window）
  - 滑动窗口（Sliding Window）
  - 令牌桶（Token Bucket）
  - 漏桶（Leaky Bucket）
- 基于 Redis 实现分布式限流
- 支持自定义限流规则
- 支持自定义限流后的回退逻辑
- 支持自定义限流 key

## 快速开始

### 1. 添加依赖

在你的 Spring Boot 项目的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.codey-star</groupId>
    <artifactId>ratelimiter-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 配置 Redis

在 `application.properties` 或 `application.yml` 中配置 Redis 连接信息：

```properties
spring.ratelimiter.redis.address=redis://127.0.0.1:6379
spring.ratelimiter.redis.password=xxx
```

### 3. 启用限流组件

在启动类添加启用注解：

```java
@EnableRateLimiter
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
```

### 4. 使用 @RateLimit 注解

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

### 7. 使用回退方法（fallbackFunction）

RateLimiter Spring Boot Starter 提供了 `fallbackFunction` 参数，允许您定义一个自定义的回退方法，当限流被触发时会自动调用该方法。这为您提供了更灵活的方式来处理限流情况。

#### 7.1 基本用法

在 `@RateLimit` 注解中，使用 `fallbackFunction` 参数指定回退方法的名称：

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

在这个例子中，当请求超过限流阈值时，将调用 `fallbackMethod` 方法而不是原始的 `fallbackExample` 方法。

#### 7.2 回退方法的参数

回退方法应该接受一个 `ProceedingJoinPoint` 类型的参数。这个参数提供了对原始方法调用的访问，包括方法参数等信息。

#### 7.3 高级用法

您可以在回退方法中实现更复杂的逻辑，例如：

1. 记录日志
2. 发送警报
3. 返回缓存的结果
4. 实现重试逻辑

示例：

```java
@GetMapping("/advanced-fallback")
@RateLimit(rate = 5, rateInterval = "1m", fallbackFunction = "advancedFallbackMethod")
public String advancedFallbackExample() {
    return "Normal response";
}

public String advancedFallbackMethod(ProceedingJoinPoint joinPoint) {
    log.warn("Rate limit exceeded for method: " + joinPoint.getSignature().getName());
    
    // 尝试从缓存获取结果
    String cachedResult = cache.get(getCacheKey(joinPoint));
    if (cachedResult != null) {
        return cachedResult;
    }
    
    // 如果没有缓存，返回一个友好的错误消息
    return "Service is currently busy. Please try again later.";
}

private String getCacheKey(ProceedingJoinPoint joinPoint) {
    // 实现缓存key的生成逻辑
}
```

#### 7.4 注意事项

1. 确保回退方法的返回类型与原始方法兼容。
2. 回退方法应该是轻量级的，避免执行耗时的操作。
3. 考虑在回退方法中包含适当的错误处理和日志记录。
4. 如果回退方法本身抛出异常，将会被传播到调用者。

通过合理使用 `fallbackFunction`，您可以为您的应用程序提供更好的用户体验，即使在遇到限流的情况下也能优雅地处理请求。

### 8. 自定义限流模式

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
### 9. 自定义key

RateLimiter Spring Boot Starter 提供了两种方式来自定义限流key：使用SpEL表达式和自定义函数。这些方法允许您根据特定的业务需求来定制限流key，从而实现更精细的限流控制。

#### 9.1 使用SpEL表达式

通过在 `@RateLimit` 注解的 `keys` 属性中使用SpEL表达式，您可以动态地生成限流key。

示例：

```java
@GetMapping("/user/{id}")
@RateLimit(rate = 5, rateInterval = "1m", keys = {"#id"})
public String getUserInfo(@PathVariable("id") Long id) {
    return "User info for id: " + id;
}
```

在这个例子中，限流key将基于URL中的id参数。这意味着每个不同的用户ID都有自己的限流计数器。

更复杂的示例：

```java
@PostMapping("/order")
@RateLimit(rate = 2, rateInterval = "1m", keys = {"#order.userId", "#order.productId"})
public String createOrder(@RequestBody Order order) {
    return "Order created for user " + order.getUserId() + " and product " + order.getProductId();
}
```

这个例子中，限流key是基于用户ID和产品ID的组合。这允许您对每个用户-产品组合进行单独的限流。

#### 9.2 使用自定义函数

对于更复杂的场景，您可以使用 `customKeyFunction` 属性来指定一个自定义方法来生成限流key。

示例：

```java
@GetMapping("/complex")
@RateLimit(rate = 10, rateInterval = "1m", customKeyFunction = "generateCustomKey")
public String complexExample(HttpServletRequest request) {
    return "Complex example";
}

public String generateCustomKey(JoinPoint joinPoint) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String ip = request.getRemoteAddr();
    String uri = request.getRequestURI();
    return ip + ":" + uri;
}
```

在这个例子中，`generateCustomKey` 方法根据客户端IP和请求URI生成一个自定义的限流key。这允许您基于IP和URI的组合进行限流。

## 注意事项

1. 确保 Redis 服务器已正确配置并可用。
2. 合理设置限流参数，避免过度限流或限流不足。
3. 在分布式环境中，确保所有服务实例使用相同的 Redis 实例来保证限流的一致性。
4. 对于高并发场景，建议使用 Redis 集群来提高性能和可用性。
5. 定期监控和调整限流规则，以适应业务需求的变化。
6. 在实现自定义限流算法时，确保算法的性能和正确性，并进行充分的测试。
7. 使用自定义限流模式时，确保在所有需要使用该模式的服务中都正确配置和注册了自定义算法。
