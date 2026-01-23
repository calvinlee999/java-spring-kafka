package com.learning.kafkagettingstarted.chapter5a;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🏭 CUSTOM THREAD FACTORY - Production-grade thread creation
 * 
 * Creates properly named threads with appropriate daemon status,
 * exception handlers, and monitoring capabilities.
 */
public class CustomThreadFactory implements ThreadFactory {
    private static final Logger logger = LoggerFactory.getLogger(CustomThreadFactory.class);
    
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup threadGroup;
    private final boolean daemon;

    public CustomThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }
    
    public CustomThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
        this.threadGroup = new ThreadGroup(namePrefix + "-ThreadGroup");
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(threadGroup, runnable, 
                                 namePrefix + "-" + threadNumber.getAndIncrement());
        
        thread.setDaemon(daemon);
        thread.setPriority(Thread.NORM_PRIORITY);
        
        // Set uncaught exception handler
        thread.setUncaughtExceptionHandler((t, e) -> {
            logger.error("❌ Uncaught exception in thread {}", t.getName(), e);
        });
        
        logger.debug("🧵 Created thread: {}", thread.getName());
        return thread;
    }
    
    public int getActiveThreadCount() {
        return threadGroup.activeCount();
    }
    
    public String getNamePrefix() {
        return namePrefix;
    }
}
