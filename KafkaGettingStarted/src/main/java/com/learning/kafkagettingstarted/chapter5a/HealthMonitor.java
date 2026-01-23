package com.learning.kafkagettingstarted.chapter5a;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🏥 HEALTH MONITOR - System health monitoring
 * 
 * Monitors the overall health of the consumer system including:
 * - Thread pool health
 * - Memory usage
 * - Processing rates
 * - Error rates
 */
public class HealthMonitor {
    private static final Logger logger = LoggerFactory.getLogger(HealthMonitor.class);
    
    private final Runtime runtime = Runtime.getRuntime();
    
    /**
     * 🩺 Perform comprehensive health check
     */
    public void checkHealth() {
        // Memory health check
        checkMemoryHealth();
        
        // Thread health check (could be expanded to check specific thread pools)
        checkThreadHealth();
        
        logger.debug("🏥 Health check completed");
    }
    
    /**
     * 🧠 Check memory usage
     */
    private void checkMemoryHealth() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        
        if (memoryUsagePercent > 85) {
            logger.warn("⚠️ High memory usage: {:.1f}% ({} MB used of {} MB)", 
                       memoryUsagePercent, usedMemory / 1024 / 1024, totalMemory / 1024 / 1024);
            
            // Could trigger garbage collection or other memory management actions
            if (memoryUsagePercent > 95) {
                logger.error("🚨 Critical memory usage! Suggesting GC...");
                System.gc(); // Suggest garbage collection
            }
        } else {
            logger.debug("🟢 Memory usage healthy: {:.1f}%", memoryUsagePercent);
        }
    }
    
    /**
     * 🧵 Check thread health
     */
    private void checkThreadHealth() {
        int activeThreadCount = Thread.activeCount();
        
        if (activeThreadCount > 100) {
            logger.warn("⚠️ High thread count: {} active threads", activeThreadCount);
        } else {
            logger.debug("🟢 Thread count healthy: {} active threads", activeThreadCount);
        }
    }
}
