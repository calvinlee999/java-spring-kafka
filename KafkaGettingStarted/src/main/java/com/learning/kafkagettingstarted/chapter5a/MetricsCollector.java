package com.learning.kafkagettingstarted.chapter5a;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 📊 METRICS COLLECTOR - Comprehensive performance monitoring
 * 
 * Collects and reports detailed metrics for monitoring consumer performance:
 * - Throughput metrics (messages/sec, batches/sec)
 * - Latency metrics (processing time, poll time)
 * - Error metrics (failures, retries, rejections)
 * - Resource metrics (thread pool usage, queue sizes)
 */
public class MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    // 📈 THROUGHPUT METRICS
    private final LongAdder totalMessagesProcessed = new LongAdder();
    private final LongAdder totalBatchesProcessed = new LongAdder();
    private final LongAdder totalPollsExecuted = new LongAdder();
    private final LongAdder totalCommitsExecuted = new LongAdder();
    
    // ⏱️ LATENCY METRICS (in nanoseconds)
    private final LongAdder totalProcessingTime = new LongAdder();
    private final LongAdder totalBatchProcessingTime = new LongAdder();
    private final AtomicLong maxProcessingTime = new AtomicLong(0);
    private final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
    
    // 🚨 ERROR METRICS
    private final LongAdder totalErrors = new LongAdder();
    private final LongAdder totalTaskRejections = new LongAdder();
    private final LongAdder totalCallerRunsExecutions = new LongAdder();
    private final LongAdder totalCommitErrors = new LongAdder();
    private final LongAdder totalCommitSuccesses = new LongAdder();
    
    // 📊 PARTITION-SPECIFIC METRICS
    private final ConcurrentHashMap<TopicPartition, PartitionMetrics> partitionMetrics = new ConcurrentHashMap<>();
    
    // ⏰ TIMING
    private final Instant startTime = Instant.now();
    private volatile Instant lastReportTime = Instant.now();
    
    /**
     * 📦 Record a batch poll operation
     */
    public void recordPollCount(int messageCount) {
        totalPollsExecuted.increment();
        totalMessagesProcessed.add(messageCount);
    }
    
    /**
     * 🕒 Record batch processing time
     */
    public void recordBatchProcessingTime(long processingTimeNanos, int messageCount) {
        totalBatchesProcessed.increment();
        totalBatchProcessingTime.add(processingTimeNanos);
        
        if (messageCount > 0) {
            long avgTimePerMessage = processingTimeNanos / messageCount;
            updateMinMax(avgTimePerMessage);
        }
    }
    
    /**
     * ⚡ Record individual message processing time
     */
    public void recordMessageProcessingTime(long processingTimeNanos) {
        totalProcessingTime.add(processingTimeNanos);
        updateMinMax(processingTimeNanos);
    }
    
    /**
     * 🎯 Record partition-specific metrics
     */
    public void recordPartitionProcessingTime(TopicPartition partition, long processingTimeNanos, 
                                            int successCount, int errorCount) {
        PartitionMetrics metrics = partitionMetrics.computeIfAbsent(partition, k -> new PartitionMetrics());
        metrics.recordProcessing(processingTimeNanos, successCount, errorCount);
    }
    
    /**
     * 🚨 Record error occurrence
     */
    public void recordError() {
        totalErrors.increment();
    }
    
    /**
     * 🚫 Record task rejection
     */
    public void recordTaskRejection() {
        totalTaskRejections.increment();
    }
    
    /**
     * 🏃‍♂️ Record caller runs execution
     */
    public void recordCallerRunsExecution() {
        totalCallerRunsExecutions.increment();
    }
    
    /**
     * ✅ Record successful commit
     */
    public void recordCommitSuccess() {
        totalCommitSuccesses.increment();
    }
    
    /**
     * ❌ Record commit error
     */
    public void recordCommitError() {
        totalCommitErrors.increment();
    }
    
    /**
     * 🔄 Update min/max processing times
     */
    private void updateMinMax(long processingTimeNanos) {
        // Update max
        maxProcessingTime.updateAndGet(current -> Math.max(current, processingTimeNanos));
        
        // Update min
        minProcessingTime.updateAndGet(current -> Math.min(current, processingTimeNanos));
    }
    
    /**
     * 📊 Generate and log comprehensive metrics report
     */
    public void reportMetrics() {
        Instant now = Instant.now();
        Duration totalRuntime = Duration.between(startTime, now);
        Duration sinceLastReport = Duration.between(lastReportTime, now);
        
        // Calculate rates
        double messagesPerSecond = calculateRate(totalMessagesProcessed.sum(), totalRuntime);
        double batchesPerSecond = calculateRate(totalBatchesProcessed.sum(), totalRuntime);
        double errorsPerSecond = calculateRate(totalErrors.sum(), totalRuntime);
        
        // Calculate average processing times (convert to milliseconds)
        double avgMessageProcessingMs = nanoToMillis((long)calculateAverage(totalProcessingTime.sum(), totalMessagesProcessed.sum()));
        double avgBatchProcessingMs = nanoToMillis((long)calculateAverage(totalBatchProcessingTime.sum(), totalBatchesProcessed.sum()));
        
        // Calculate error rate
        double errorRate = calculatePercentage(totalErrors.sum(), totalMessagesProcessed.sum());
        double commitErrorRate = calculatePercentage(totalCommitErrors.sum(), totalCommitsExecuted.sum());
        
        // 📋 LOG COMPREHENSIVE REPORT
        logger.info("📊 ===== OPTIMIZED CONSUMER METRICS REPORT =====");
        logger.info("🕒 Runtime: {} minutes", totalRuntime.toMinutes());
        logger.info("📈 THROUGHPUT:");
        logger.info("   Messages/sec: {:.2f} ({} total)", messagesPerSecond, totalMessagesProcessed.sum());
        logger.info("   Batches/sec: {:.2f} ({} total)", batchesPerSecond, totalBatchesProcessed.sum());
        logger.info("   Polls executed: {}", totalPollsExecuted.sum());
        
        logger.info("⏱️ LATENCY:");
        logger.info("   Avg message processing: {:.2f}ms", avgMessageProcessingMs);
        logger.info("   Avg batch processing: {:.2f}ms", avgBatchProcessingMs);
        logger.info("   Min processing time: {:.2f}ms", nanoToMillis(minProcessingTime.get()));
        logger.info("   Max processing time: {:.2f}ms", nanoToMillis(maxProcessingTime.get()));
        
        logger.info("🚨 ERRORS:");
        logger.info("   Error rate: {:.2f}% ({} errors)", errorRate, totalErrors.sum());
        logger.info("   Commit error rate: {:.2f}%", commitErrorRate);
        logger.info("   Task rejections: {}", totalTaskRejections.sum());
        logger.info("   Caller runs executions: {}", totalCallerRunsExecutions.sum());
        
        logger.info("📊 COMMITS:");
        logger.info("   Successful: {}", totalCommitSuccesses.sum());
        logger.info("   Failed: {}", totalCommitErrors.sum());
        
        // Report partition-specific metrics
        if (!partitionMetrics.isEmpty()) {
            logger.info("🎯 PARTITION METRICS:");
            partitionMetrics.forEach((partition, metrics) -> {
                logger.info("   {}: {} processed, {} errors, avg: {:.2f}ms", 
                           partition, 
                           metrics.getProcessedCount(), 
                           metrics.getErrorCount(),
                           nanoToMillis(metrics.getAverageProcessingTime()));
            });
        }
        
        logger.info("📊 ============================================");
        
        lastReportTime = now;
    }
    
    /**
     * 🧮 Helper methods for calculations
     */
    private double calculateRate(long count, Duration duration) {
        if (duration.toSeconds() == 0) return 0.0;
        return (double) count / duration.toSeconds();
    }
    
    private double calculateAverage(long total, long count) {
        return count == 0 ? 0.0 : (double) total / count;
    }
    
    private double calculatePercentage(long part, long total) {
        return total == 0 ? 0.0 : (double) part * 100 / total;
    }
    
    private double nanoToMillis(long nanos) {
        return nanos / 1_000_000.0;
    }
    
    // 📊 Getters for testing and monitoring
    public long getTotalMessagesProcessed() { return totalMessagesProcessed.sum(); }
    public long getTotalErrors() { return totalErrors.sum(); }
    public double getErrorRate() { return calculatePercentage(totalErrors.sum(), totalMessagesProcessed.sum()); }
    public Duration getUptime() { return Duration.between(startTime, Instant.now()); }
    
    /**
     * 🎯 PARTITION-SPECIFIC METRICS CLASS
     */
    private static class PartitionMetrics {
        private final LongAdder processedCount = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final LongAdder totalProcessingTime = new LongAdder();
        
        void recordProcessing(long processingTimeNanos, int successCount, int errorCount) {
            this.processedCount.add(successCount);
            this.errorCount.add(errorCount);
            this.totalProcessingTime.add(processingTimeNanos);
        }
        
        long getProcessedCount() { return processedCount.sum(); }
        long getErrorCount() { return errorCount.sum(); }
        long getAverageProcessingTime() { 
            long processed = processedCount.sum();
            return processed == 0 ? 0 : totalProcessingTime.sum() / processed;
        }
    }
}
