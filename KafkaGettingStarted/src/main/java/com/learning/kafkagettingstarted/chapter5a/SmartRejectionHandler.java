package com.learning.kafkagettingstarted.chapter5a;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🚨 SMART REJECTION HANDLER - Advanced backpressure management
 * 
 * Handles thread pool saturation with intelligent strategies:
 * - Caller runs policy for graceful degradation
 * - Metrics collection for monitoring
 * - Configurable fallback strategies
 */
public class SmartRejectionHandler implements RejectedExecutionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SmartRejectionHandler.class);
    
    private final MetricsCollector metricsCollector;
    
    public SmartRejectionHandler() {
        this.metricsCollector = new MetricsCollector();
    }
    
    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        // Record rejection for monitoring
        metricsCollector.recordTaskRejection();
        
        // Log the rejection
        logger.warn("⚠️ Task rejected - Pool: {}/{}, Queue: {}/{}", 
                   executor.getActiveCount(), 
                   executor.getMaximumPoolSize(),
                   executor.getQueue().size(),
                   executor.getQueue().remainingCapacity());
        
        if (executor.isShutdown()) {
            // Executor is shutting down, reject gracefully
            logger.info("🛑 Executor is shutting down, rejecting task");
            throw new RejectedExecutionException("Executor is shutting down");
        }
        
        // 🏃‍♂️ CALLER RUNS POLICY - Execute in caller's thread
        // This provides natural backpressure and prevents data loss
        try {
            logger.debug("🔄 Executing task in caller thread due to pool saturation");
            task.run();
            metricsCollector.recordCallerRunsExecution();
        } catch (Exception e) {
            logger.error("❌ Error executing task in caller thread", e);
            throw new RejectedExecutionException("Failed to execute task in caller thread", e);
        }
    }
}
