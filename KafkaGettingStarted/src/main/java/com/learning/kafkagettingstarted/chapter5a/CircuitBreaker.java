package com.learning.kafkagettingstarted.chapter5a;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ⚡ CIRCUIT BREAKER - Fault tolerance and resilience
 * 
 * Implements circuit breaker pattern to handle failures gracefully:
 * - CLOSED: Normal operation, all requests processed
 * - OPEN: Failure threshold exceeded, fast-fail requests
 * - HALF_OPEN: Testing if service has recovered
 */
public class CircuitBreaker {
    private static final Logger logger = LoggerFactory.getLogger(CircuitBreaker.class);
    
    // 🔧 Configuration
    private final int failureThreshold;
    private final Duration timeout;
    private final Duration backoffMultiplier;
    
    // 📊 State tracking
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private volatile State state = State.CLOSED;
    private final AtomicInteger successCount = new AtomicInteger(0);
    
    public enum State {
        CLOSED,    // Normal operation
        OPEN,      // Failing fast
        HALF_OPEN  // Testing recovery
    }
    
    public CircuitBreaker() {
        this(5, Duration.ofSeconds(60), Duration.ofMillis(100));
    }
    
    public CircuitBreaker(int failureThreshold, Duration timeout, Duration backoffMultiplier) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
        this.backoffMultiplier = backoffMultiplier;
    }
    
    /**
     * 📈 Record successful operation
     */
    public void recordSuccess() {
        consecutiveFailures.set(0);
        successCount.incrementAndGet();
        
        if (state == State.HALF_OPEN) {
            logger.info("🟢 Circuit breaker transitioning to CLOSED - service recovered");
            state = State.CLOSED;
        }
    }
    
    /**
     * 📉 Record failed operation
     */
    public void recordFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        
        if (failures >= failureThreshold && state == State.CLOSED) {
            logger.warn("🔴 Circuit breaker OPEN - failure threshold ({}) exceeded", failureThreshold);
            state = State.OPEN;
        }
    }
    
    /**
     * 🚪 Check if request should be allowed
     */
    public boolean isRequestAllowed() {
        switch (state) {
            case CLOSED:
                return true;
                
            case OPEN:
                if (isTimeoutExpired()) {
                    logger.info("🟡 Circuit breaker transitioning to HALF_OPEN - testing recovery");
                    state = State.HALF_OPEN;
                    return true;
                }
                return false;
                
            case HALF_OPEN:
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * ⏰ Calculate backoff delay based on failures
     */
    public long getBackoffDelay() {
        int failures = consecutiveFailures.get();
        long baseDelay = backoffMultiplier.toMillis();
        
        // Exponential backoff with max 30 seconds
        return Math.min(baseDelay * (1L << Math.min(failures, 10)), 30000);
    }
    
    /**
     * 🕒 Check if timeout has expired
     */
    private boolean isTimeoutExpired() {
        long lastFailure = lastFailureTime.get();
        return (System.currentTimeMillis() - lastFailure) >= timeout.toMillis();
    }
    
    // 📊 Getters for monitoring
    public State getState() { return state; }
    public int getConsecutiveFailures() { return consecutiveFailures.get(); }
    public int getSuccessCount() { return successCount.get(); }
}
