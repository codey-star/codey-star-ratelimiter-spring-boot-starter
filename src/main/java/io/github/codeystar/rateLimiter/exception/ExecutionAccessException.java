package io.github.codeystar.rateLimiter.exception;

/**
 * @author zhiyang.zhang
 */
public class ExecutionAccessException extends RuntimeException {

    public ExecutionAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}