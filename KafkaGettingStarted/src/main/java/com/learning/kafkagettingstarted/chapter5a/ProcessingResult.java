package com.learning.kafkagettingstarted.chapter5a;

/**
 * 🎯 PROCESSING RESULT - Encapsulates processing outcomes
 * 
 * Simple result wrapper that indicates success/failure and carries
 * error information for failed processing attempts.
 */
public class ProcessingResult {
    private final boolean success;
    private final Exception error;
    
    private ProcessingResult(boolean success, Exception error) {
        this.success = success;
        this.error = error;
    }
    
    public static ProcessingResult success() {
        return new ProcessingResult(true, null);
    }
    
    public static ProcessingResult failure(Exception error) {
        return new ProcessingResult(false, error);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Exception getError() {
        return error;
    }
}
