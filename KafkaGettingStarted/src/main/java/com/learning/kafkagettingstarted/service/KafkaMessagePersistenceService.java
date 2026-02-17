/*
 * KAFKA MESSAGE PERSISTENCE SERVICE - DATABASE OPERATIONS FOR KAFKA MESSAGES
 * 
 * Think of this service like a smart filing clerk:
 * - They know how to properly store Kafka messages in the database
 * - They can quickly retrieve messages based on various criteria
 * - They handle all the complex database operations so other parts don't have to
 * 
 * This is a SERVICE class - it contains business logic for managing Kafka message persistence.
 * It acts as a bridge between the Kafka consumers/producers and the database.
 */

package com.learning.kafkagettingstarted.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.kafkagettingstarted.entity.KafkaMessage;
import com.learning.kafkagettingstarted.repository.KafkaMessageRepository;

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @Service - Tells Spring "This is a service component with business logic"
 *           It's like putting a badge that says "BUSINESS LOGIC EXPERT"
 * 
 * @Transactional - Ensures database operations are atomic (all-or-nothing)
 *                  Like having an "undo" button for database changes
 */
@Service
@Transactional
public class KafkaMessagePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(KafkaMessagePersistenceService.class);
    
    private final KafkaMessageRepository kafkaMessageRepository;

    /**
     * DEPENDENCY INJECTION - Spring automatically provides the repository
     * 
     * Constructor injection is the recommended approach.
     * It's like having the database specialist delivered to your office.
     */
    public KafkaMessagePersistenceService(KafkaMessageRepository kafkaMessageRepository) {
        this.kafkaMessageRepository = kafkaMessageRepository;
    }

    /**
     * SAVE KAFKA MESSAGE - Store a new message in the database
     * 
     * This method takes Kafka message details and creates a persistent record.
     * Perfect for event sourcing and message auditing.
     * 
     * @param topic The Kafka topic name
     * @param messageKey The message key (can be null)
     * @param messageValue The actual message content
     * @param partitionId The Kafka partition number
     * @param offsetValue The Kafka offset
     * @return The saved KafkaMessage entity with generated ID
     */
    public KafkaMessage saveKafkaMessage(String topic, String messageKey, String messageValue, 
                                       Integer partitionId, Long offsetValue) {
        try {
            log.debug("💾 Saving Kafka message to database: topic={}, key={}, partition={}, offset={}", 
                     topic, messageKey, partitionId, offsetValue);

            KafkaMessage kafkaMessage = new KafkaMessage(topic, messageKey, messageValue, partitionId, offsetValue);
            KafkaMessage savedMessage = kafkaMessageRepository.save(kafkaMessage);

            log.info("✅ Kafka message saved successfully with ID: {}", savedMessage.getId());
            return savedMessage;

        } catch (Exception e) {
            log.error("❌ Failed to save Kafka message: topic={}, error={}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to save Kafka message to database", e);
        }
    }

    /**
     * UPDATE MESSAGE STATUS - Change the processing status of a message
     * 
     * Essential for tracking message processing lifecycle.
     * Like updating a package tracking status from "shipped" to "delivered"
     */
    public KafkaMessage updateMessageStatus(Long messageId, KafkaMessage.MessageStatus status) {
        try {
            Optional<KafkaMessage> messageOpt = kafkaMessageRepository.findById(messageId);
            
            if (messageOpt.isPresent()) {
                KafkaMessage message = messageOpt.get();
                message.setStatus(status);
                
                KafkaMessage updatedMessage = kafkaMessageRepository.save(message);
                log.info("✅ Updated message {} status to {}", messageId, status);
                return updatedMessage;
            } else {
                log.warn("⚠️ Message with ID {} not found for status update", messageId);
                throw new RuntimeException("Message not found with ID: " + messageId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to update message status: messageId={}, status={}, error={}", 
                     messageId, status, e.getMessage(), e);
            throw new RuntimeException("Failed to update message status", e);
        }
    }

    /**
     * MARK MESSAGE AS FAILED - Record a message processing failure
     * 
     * This method updates the message status to FAILED and stores the error details.
     * Essential for debugging and retry logic.
     */
    public KafkaMessage markMessageAsFailed(Long messageId, String errorMessage) {
        try {
            Optional<KafkaMessage> messageOpt = kafkaMessageRepository.findById(messageId);
            
            if (messageOpt.isPresent()) {
                KafkaMessage message = messageOpt.get();
                message.markAsFailed(errorMessage);
                message.incrementRetryCount();
                
                KafkaMessage updatedMessage = kafkaMessageRepository.save(message);
                log.warn("⚠️ Marked message {} as failed: {}", messageId, errorMessage);
                return updatedMessage;
            } else {
                throw new RuntimeException("Message not found with ID: " + messageId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to mark message as failed: messageId={}, error={}", 
                     messageId, e.getMessage(), e);
            throw new RuntimeException("Failed to mark message as failed", e);
        }
    }

    /**
     * GET MESSAGES BY TOPIC - Retrieve all messages from a specific topic
     * 
     * Useful for analyzing message patterns and debugging topic-specific issues.
     */
    @Transactional(readOnly = true)
    public List<KafkaMessage> getMessagesByTopic(String topic) {
        log.debug("🔍 Retrieving messages for topic: {}", topic);
        List<KafkaMessage> messages = kafkaMessageRepository.findByTopic(topic);
        log.info("📋 Found {} messages for topic: {}", messages.size(), topic);
        return messages;
    }

    /**
     * GET MESSAGES BY TOPIC WITH PAGINATION - Handle large result sets efficiently
     * 
     * When a topic has thousands of messages, pagination prevents memory issues.
     */
    @Transactional(readOnly = true)
    public Page<KafkaMessage> getMessagesByTopic(String topic, int page, int size) {
        log.debug("🔍 Retrieving messages for topic: {} (page={}, size={})", topic, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KafkaMessage> messages = kafkaMessageRepository.findByTopic(topic, pageable);
        
        log.info("📋 Found {} messages for topic: {} (page {} of {})", 
                messages.getNumberOfElements(), topic, page + 1, messages.getTotalPages());
        return messages;
    }

    /**
     * GET FAILED MESSAGES FOR RETRY - Find messages that can be retried
     * 
     * This method is essential for building resilient message processing.
     * It finds failed messages that haven't exceeded the retry limit.
     */
    @Transactional(readOnly = true)
    public List<KafkaMessage> getFailedMessagesForRetry(int maxRetries) {
        log.debug("🔄 Searching for failed messages with retry count < {}", maxRetries);
        List<KafkaMessage> failedMessages = kafkaMessageRepository.findFailedMessagesForRetry(maxRetries);
        log.info("🔄 Found {} failed messages eligible for retry", failedMessages.size());
        return failedMessages;
    }

    /**
     * GET RECENT MESSAGES - Get the most recently received messages
     * 
     * Perfect for monitoring and real-time dashboards.
     */
    @Transactional(readOnly = true)
    public List<KafkaMessage> getRecentMessages(int limit) {
        log.debug("🕒 Retrieving {} most recent messages", limit);
        Pageable pageable = PageRequest.of(0, limit);
        List<KafkaMessage> recentMessages = kafkaMessageRepository.findRecentMessages(pageable);
        log.info("🕒 Retrieved {} recent messages", recentMessages.size());
        return recentMessages;
    }

    /**
     * GET MESSAGE STATISTICS - Provide summary statistics for monitoring
     * 
     * Returns counts of messages by status for dashboard and monitoring purposes.
     */
    @Transactional(readOnly = true)
    public MessageStatistics getMessageStatistics() {
        log.debug("📊 Calculating message statistics");
        
        long totalMessages = kafkaMessageRepository.count();
        long processedMessages = kafkaMessageRepository.countByStatus(KafkaMessage.MessageStatus.PROCESSED);
        long failedMessages = kafkaMessageRepository.countByStatus(KafkaMessage.MessageStatus.FAILED);
        long processingMessages = kafkaMessageRepository.countByStatus(KafkaMessage.MessageStatus.PROCESSING);
        long receivedMessages = kafkaMessageRepository.countByStatus(KafkaMessage.MessageStatus.RECEIVED);

        MessageStatistics stats = new MessageStatistics(totalMessages, processedMessages, 
                                                       failedMessages, processingMessages, receivedMessages);
        
        log.info("📊 Message statistics: {}", stats);
        return stats;
    }

    /**
     * SEARCH MESSAGES BY CONTENT - Find messages containing specific text
     * 
     * Useful for debugging and finding messages with specific data.
     */
    @Transactional(readOnly = true)
    public List<KafkaMessage> searchMessagesByContent(String searchTerm) {
        log.debug("🔍 Searching messages containing: {}", searchTerm);
        List<KafkaMessage> foundMessages = kafkaMessageRepository.findByMessageValueContaining(searchTerm);
        log.info("🔍 Found {} messages containing: {}", foundMessages.size(), searchTerm);
        return foundMessages;
    }

    /**
     * DELETE OLD MESSAGES - Clean up old data for storage management
     * 
     * This method helps maintain database performance by removing old messages.
     * Essential for compliance with data retention policies.
     */
    public int deleteOldMessages(LocalDateTime cutoffDate) {
        log.info("🗑️ Deleting messages older than: {}", cutoffDate);
        
        try {
            List<KafkaMessage> oldMessages = kafkaMessageRepository.findByCreatedAtBefore(cutoffDate);
            int deleteCount = oldMessages.size();
            
            kafkaMessageRepository.deleteByCreatedAtBefore(cutoffDate);
            
            log.info("✅ Deleted {} old messages", deleteCount);
            return deleteCount;
        } catch (Exception e) {
            log.error("❌ Failed to delete old messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete old messages", e);
        }
    }

    /**
     * FIND MESSAGE BY KAFKA COORDINATES - Get message by topic/partition/offset
     * 
     * Every Kafka message has unique coordinates. This method finds the exact message.
     */
    @Transactional(readOnly = true)
    public Optional<KafkaMessage> findByKafkaCoordinates(String topic, Integer partitionId, Long offsetValue) {
        log.debug("🎯 Finding message by coordinates: topic={}, partition={}, offset={}", 
                 topic, partitionId, offsetValue);
        
        Optional<KafkaMessage> message = kafkaMessageRepository
            .findByTopicAndPartitionIdAndOffsetValue(topic, partitionId, offsetValue);
        
        if (message.isPresent()) {
            log.info("✅ Found message by coordinates: {}", message.get().getId());
        } else {
            log.info("❌ No message found for coordinates: topic={}, partition={}, offset={}", 
                    topic, partitionId, offsetValue);
        }
        
        return message;
    }

    /**
     * GET ALL MESSAGES WITH PAGINATION - Retrieve all messages with pagination support
     * 
     * Perfect for admin interfaces and message browsing.
     */
    @Transactional(readOnly = true)
    public Page<KafkaMessage> getAllMessages(int page, int size) {
        log.debug("📄 Retrieving all messages with pagination: page={}, size={}", page, size);
        
        // Create pageable with sorting by created date (newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KafkaMessage> messages = kafkaMessageRepository.findAll(pageable);
        
        log.info("📄 Retrieved {} messages on page {} of {}", 
                messages.getNumberOfElements(), page + 1, messages.getTotalPages());
        
        return messages;
    }

    /**
     * GET MESSAGE BY ID - Retrieve a specific message by its database ID
     * 
     * Useful for detailed message inspection.
     */
    @Transactional(readOnly = true)
    public Optional<KafkaMessage> getMessageById(Long id) {
        log.debug("🔍 Retrieving message by ID: {}", id);
        
        Optional<KafkaMessage> message = kafkaMessageRepository.findById(id);
        
        if (message.isPresent()) {
            log.info("✅ Found message with ID: {}", id);
        } else {
            log.warn("❌ No message found with ID: {}", id);
        }
        
        return message;
    }

    /**
     * GET TOTAL MESSAGE COUNT - Get the total number of messages
     * 
     * Used for statistics and monitoring.
     */
    @Transactional(readOnly = true)
    public long getTotalMessageCount() {
        long count = kafkaMessageRepository.count();
        log.debug("📊 Total message count: {}", count);
        return count;
    }

    /**
     * GET MESSAGE COUNT BY TOPIC - Get message counts grouped by topic
     * 
     * Provides insight into message distribution across topics.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getMessageCountByTopic() {
        log.debug("📊 Calculating message counts by topic");
        
        List<KafkaMessage> allMessages = kafkaMessageRepository.findAll();
        Map<String, Long> topicCounts = allMessages.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                KafkaMessage::getTopic,
                java.util.stream.Collectors.counting()
            ));
        
        log.info("📊 Message counts by topic: {}", topicCounts);
        return topicCounts;
    }

    /**
     * GET LATEST MESSAGE TIME - Get the timestamp of the most recent message
     * 
     * Useful for monitoring and health checks.
     */
    @Transactional(readOnly = true)
    public LocalDateTime getLatestMessageTime() {
        log.debug("📊 Finding latest message time");
        
        Optional<KafkaMessage> latestMessage = kafkaMessageRepository
            .findFirstByOrderByCreatedAtDesc();
        
        LocalDateTime latestTime = latestMessage
            .map(KafkaMessage::getCreatedAt)
            .orElse(null);
        
        log.info("📊 Latest message time: {}", latestTime);
        return latestTime;
    }

    /**
     * MESSAGE STATISTICS DATA CLASS - Holds statistical information
     * 
     * This inner class provides a clean way to return multiple statistics.
     * Like a summary report card for message processing.
     */
    public static class MessageStatistics {
        private final long totalMessages;
        private final long processedMessages;
        private final long failedMessages;
        private final long processingMessages;
        private final long receivedMessages;

        public MessageStatistics(long totalMessages, long processedMessages, long failedMessages, 
                               long processingMessages, long receivedMessages) {
            this.totalMessages = totalMessages;
            this.processedMessages = processedMessages;
            this.failedMessages = failedMessages;
            this.processingMessages = processingMessages;
            this.receivedMessages = receivedMessages;
        }

        // Getters
        public long getTotalMessages() { return totalMessages; }
        public long getProcessedMessages() { return processedMessages; }
        public long getFailedMessages() { return failedMessages; }
        public long getProcessingMessages() { return processingMessages; }
        public long getReceivedMessages() { return receivedMessages; }
        
        public double getSuccessRate() {
            return totalMessages > 0 ? (double) processedMessages / totalMessages * 100 : 0.0;
        }
        
        public double getFailureRate() {
            return totalMessages > 0 ? (double) failedMessages / totalMessages * 100 : 0.0;
        }

        @Override
        public String toString() {
            return "MessageStats{total=%d, processed=%d, failed=%d, processing=%d, received=%d, success=%.2f%%, failure=%.2f%%}".formatted(
                totalMessages, processedMessages, failedMessages, processingMessages, receivedMessages,
                getSuccessRate(), getFailureRate());
        }
    }
}
