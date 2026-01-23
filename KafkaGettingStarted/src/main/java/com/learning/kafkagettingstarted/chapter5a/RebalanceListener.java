package com.learning.kafkagettingstarted.chapter5a;

import java.util.Collection;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 🔄 REBALANCE LISTENER - Handle partition rebalancing events
 * 
 * Manages partition assignment changes during consumer group rebalancing:
 * - Logs partition assignments for monitoring
 * - Could implement partition-specific cleanup logic
 * - Helps with debugging rebalancing issues
 */
public class RebalanceListener implements ConsumerRebalanceListener {
    private static final Logger logger = LoggerFactory.getLogger(RebalanceListener.class);
    
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        if (!partitions.isEmpty()) {
            logger.info("🔄 Partitions revoked: {}", partitions.size());
            for (TopicPartition partition : partitions) {
                logger.debug("   📤 Revoked: {}", partition);
            }
        }
    }
    
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        if (!partitions.isEmpty()) {
            logger.info("🔄 Partitions assigned: {}", partitions.size());
            for (TopicPartition partition : partitions) {
                logger.debug("   📥 Assigned: {}", partition);
            }
        }
    }
}
