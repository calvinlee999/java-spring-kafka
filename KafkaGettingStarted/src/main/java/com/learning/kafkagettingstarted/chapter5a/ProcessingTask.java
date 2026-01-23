package com.learning.kafkagettingstarted.chapter5a;

/**
 * 📋 PROCESSING TASK - Task wrapper for thread pool execution
 * 
 * Encapsulates a processing task with metadata for better tracking
 * and monitoring in the thread pool.
 */
public class ProcessingTask implements Runnable {
    private final Runnable task;
    private final long createdTime;
    private final String description;
    
    public ProcessingTask(Runnable task, String description) {
        this.task = task;
        this.description = description;
        this.createdTime = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        task.run();
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public String getDescription() {
        return description;
    }
    
    public long getWaitTime() {
        return System.currentTimeMillis() - createdTime;
    }
}
