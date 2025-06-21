package io.github.codeystar.rateLimiter.core;

import io.github.codeystar.rateLimiter.annotation.RateLimit;
import io.github.codeystar.rateLimiter.exception.ExecutionAccessException;
import io.github.codeystar.rateLimiter.model.RateLimitRule;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhiyang.zhang
 */
public class RuleLoader implements BeanFactoryAware {


    private static final Logger logger = LoggerFactory.getLogger(RuleLoader.class);

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    private static final TemplateParserContext PARSER_CONTEXT = new TemplateParserContext();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
    private BeanFactory beanFactory;

    public String getKeyName(JoinPoint joinPoint, RateLimit rateLimit) {
        Method method = getMethod(joinPoint);
        List<String> definitionKeys = getSpelDefinitionKey(rateLimit.keys(), method, joinPoint.getArgs());
        return String.join("-", definitionKeys);
    }

    public int getRate(RateLimit rateLimit) {
        if (StringUtils.hasText(rateLimit.rateExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.rateExpression()), PARSER_CONTEXT)
                    .getValue(String.class);
            if (value != null) {
                return Integer.parseInt(value);
            }
        }
        return rateLimit.rate();
    }

    public int getBucketCapacity(RateLimit rateLimit) {
        if (StringUtils.hasText(rateLimit.bucketCapacityExpression())) {
            String value = parser.parseExpression(resolve(rateLimit.bucketCapacityExpression()), PARSER_CONTEXT)
                    .getValue(String.class);
            if (value != null) {
                return Integer.parseInt(value);
            }
        }
        return rateLimit.bucketCapacity();
    }

    public int getLeakyBucketRate(RateLimit rateLimit) {
        return rateLimit.leakyBucketRate();
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    @SuppressWarnings("ConstantConditions")
    private List<String> getSpelDefinitionKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<>();
        for (String definitionKey : definitionKeys) {
            if (!ObjectUtils.isEmpty(definitionKey)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
                Object objKey = parser.parseExpression(definitionKey).getValue(context);
                definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return definitionKeyList;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    private String resolve(String value) {
        return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
    }

    /**
     * 获取基础的限流 key
     */
    private String getKey(MethodSignature signature) {
        return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());

    }

    RateLimitRule getRateLimiterRule(JoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String rateLimitKey = this.getKey(signature);
        String businessKeyName = this.getKeyName(joinPoint, rateLimit);
        if (StringUtils.hasLength(businessKeyName)){
            rateLimitKey = rateLimitKey  + ":" + businessKeyName;
        }
        if (StringUtils.hasLength(rateLimit.customKeyFunction())) {
            try {
                rateLimitKey = this.getKey(signature) + this.executeFunction(rateLimit.customKeyFunction(), joinPoint).toString();
            } catch (Throwable throwable) {
                logger.info("Gets the custom Key exception and degrades it to the default Key:{}", rateLimit, throwable);
            }
        }
        int rate = this.getRate(rateLimit);
        int bucketCapacity = this.getBucketCapacity(rateLimit);
        long rateInterval = DurationStyle.detectAndParse(rateLimit.rateInterval()).getSeconds();
        int leakyBucketRate = this.getLeakyBucketRate(rateLimit);

        RateLimitRule rateLimitRule = new RateLimitRule(rateLimitKey, rate, rateLimit.mode());
        rateLimitRule.setRateInterval(Long.valueOf(rateInterval).intValue());
        rateLimitRule.setFallbackFunction(rateLimit.fallbackFunction());
        rateLimitRule.setRequestedTokens(rateLimit.requestedTokens());
        rateLimitRule.setBucketCapacity(bucketCapacity);
        rateLimitRule.setLeakyBucketRate(leakyBucketRate);

        return rateLimitRule;
    }

    /**
     * 执行自定义函数
     */
    public Object executeFunction(String fallbackName, JoinPoint joinPoint) throws Throwable {
        // prepare invocation context
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(fallbackName, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy", e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object res;
        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new ExecutionAccessException("Fail to invoke custom lock timeout handler: " + fallbackName, e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return res;
    }

}
