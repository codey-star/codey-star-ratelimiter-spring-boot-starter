package io.github.codeystar.rateLimiter.model;

/**
 * @author zhiyang.zhang
 */
public class RateLimitRule {

    private String key;
    private int rate;
    private int rateInterval;
    private String rateLimitMode;
    private int bucketCapacity;
    private int requestedTokens;
    private String fallbackFunction;
    private double leakyBucketRate;

    public RateLimitRule(String key, int rate, String rateLimitMode) {
        this.key = key;
        this.rate = rate;
        this.rateLimitMode = rateLimitMode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getRateInterval() {
        return rateInterval;
    }

    public void setRateInterval(int rateInterval) {
        this.rateInterval = rateInterval;
    }

    public String getRateLimitModeEnum() {
        return rateLimitMode;
    }

    public void setRateLimitModeEnum(String rateLimitMode) {
        this.rateLimitMode = rateLimitMode;
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public void setBucketCapacity(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    public int getRequestedTokens() {
        return requestedTokens;
    }

    public void setRequestedTokens(int requestedTokens) {
        this.requestedTokens = requestedTokens;
    }

    public String getFallbackFunction() {
        return fallbackFunction;
    }

    public void setFallbackFunction(String fallbackFunction) {
        this.fallbackFunction = fallbackFunction;
    }

    public double getLeakyBucketRate() {
        return leakyBucketRate;
    }

    public void setLeakyBucketRate(double leakyBucketRate) {
        this.leakyBucketRate = leakyBucketRate;
    }
}
