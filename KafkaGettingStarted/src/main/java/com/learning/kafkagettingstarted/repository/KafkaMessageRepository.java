/*
 * KAFKA MESSAGE REPOSITORY - DATABASE ACCESS LAYER
 * 
 * Think of this interface like a specialized librarian:
 * - They know exactly how to find, store, and organize Kafka messages
 * - They provide simple methods like "find all orders" or "count failed messages"
 * - Spring Data JPA automatically implements all the complex SQL for us
 * 
 * This is a REPOSITORY interface - it handles all database operations for KafkaMessage entities.
 * We just define what we want to do, and Spring creates the implementation automatically.
 */

package com.learning.kafkagettingstarted.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.learning.kafkagettingstarted.entity.KafkaMessage;

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @Repository - Tells Spring "This is a data access component"
 *              It's like putting a sign that says "DATABASE SPECIALIST"
 * 
 * JpaRepository<KafkaMessage, Long> means:
 * - We're working with KafkaMessage entities
 * - The primary key type is Long
 * - We get basic CRUD operations for free (Create, Read, Update, Delete)
 */
@Repository
public interface KafkaMessageRepository extends JpaRepository<KafkaMessage, Long> {

    /**
     * FIND BY TOPIC - Get all messages from a specific Kafka topic
     * 
     * Spring Data JPA automatically converts this method name into SQL:
     * SELECT * FROM kafka_messages WHERE topic = ?
     * 
     * It's like asking the librarian: "Show me all books from the Science section"
     */
    List<KafkaMessage> findByTopic(String topic);

    /**
     * FIND BY TOPIC WITH PAGINATION - Get messages from a topic, page by page
     * 
     * Useful when a topic has thousands of messages.
     * Like getting books 1-20, then 21-40, etc.
     */
    Page<KafkaMessage> findByTopic(String topic, Pageable pageable);

    /**
     * FIND BY STATUS - Get all messages with a specific processing status
     * 
     * Perfect for finding failed messages that need retry, or completed messages.
     * Like asking: "Show me all orders that failed processing"
     */
    List<KafkaMessage> findByStatus(KafkaMessage.MessageStatus status);

    /**
     * FIND BY MESSAGE KEY - Get messages with a specific key
     * 
     * In Kafka, messages can have keys for ordering and partitioning.
     * This helps find all messages related to a specific entity (like a customer ID).
     */
    List<KafkaMessage> findByMessageKey(String messageKey);

    /**
     * FIND BY DATE RANGE - Get messages created within a time period
     * 
     * Essential for reporting and analytics.
     * Like asking: "Show me all messages from yesterday"
     */
    List<KafkaMessage> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * FIND RECENT MESSAGES - Get the most recent messages
     * 
     * Using @Query annotation allows us to write custom SQL.
     * This gets the newest messages first, limited to a specific count.
     */
    @Query("SELECT m FROM KafkaMessage m ORDER BY m.createdAt DESC")
    List<KafkaMessage> findRecentMessages(Pageable pageable);

    /**
     * COUNT BY STATUS - Count how many messages have each status
     * 
     * Perfect for monitoring and dashboards.
     * Like asking: "How many orders are still pending?"
     */
    long countByStatus(KafkaMessage.MessageStatus status);

    /**
     * COUNT BY TOPIC - Count messages per topic
     * 
     * Helps understand message volume across different topics.
     */
    long countByTopic(String topic);

    /**
     * FIND FAILED MESSAGES WITH RETRY COUNT - Get failed messages that can be retried
     * 
     * Custom query to find messages that failed but haven't exceeded retry limits.
     * Essential for building resilient message processing.
     */
    @Query("SELECT m FROM KafkaMessage m WHERE m.status = 'FAILED' AND m.retryCount < :maxRetries ORDER BY m.createdAt ASC")
    List<KafkaMessage> findFailedMessagesForRetry(@Param("maxRetries") int maxRetries);

    /**
     * FIND BY TOPIC AND STATUS - Combination search
     * 
     * Get messages from a specific topic with a specific status.
     * Like asking: "Show me all failed messages from the orders topic"
     */
    List<KafkaMessage> findByTopicAndStatus(String topic, KafkaMessage.MessageStatus status);

    /**
     * FIND TOP MESSAGES BY TOPIC - Get latest messages from a specific topic
     * 
     * Useful for showing recent activity in a specific topic.
     */
    @Query("SELECT m FROM KafkaMessage m WHERE m.topic = :topic ORDER BY m.createdAt DESC")
    List<KafkaMessage> findTopMessagesByTopic(@Param("topic") String topic, Pageable pageable);

    /**
     * GET MESSAGE STATISTICS - Custom query for dashboard data
     * 
     * Returns aggregated statistics about messages.
     * This is a more complex query that demonstrates advanced repository capabilities.
     */
    @Query("SELECT " +
           "COUNT(m) as totalMessages, " +
           "COUNT(CASE WHEN m.status = 'PROCESSED' THEN 1 END) as processedMessages, " +
           "COUNT(CASE WHEN m.status = 'FAILED' THEN 1 END) as failedMessages, " +
           "COUNT(CASE WHEN m.status = 'PROCESSING' THEN 1 END) as processingMessages " +
           "FROM KafkaMessage m WHERE m.topic = :topic")
    Object[] getMessageStatisticsByTopic(@Param("topic") String topic);

    /**
     * FIND BY PARTITION AND OFFSET - Get specific message by Kafka coordinates
     * 
     * In Kafka, every message has a unique position defined by topic + partition + offset.
     * This method finds the exact message at those coordinates.
     */
    Optional<KafkaMessage> findByTopicAndPartitionIdAndOffsetValue(String topic, Integer partitionId, Long offsetValue);

    /**
     * DELETE OLD MESSAGES - Cleanup method for data retention
     * 
     * Removes messages older than a specified date.
     * Essential for managing database size and compliance with data retention policies.
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * FIND OLD MESSAGES - Find messages before a cutoff date
     * 
     * Helper method to find messages that will be deleted.
     */
    List<KafkaMessage> findByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * FIND MESSAGES BY VALUE CONTENT - Search within message content
     * 
     * Uses SQL LIKE operator to find messages containing specific text.
     * Perfect for debugging and searching for specific data.
     */
    @Query("SELECT m FROM KafkaMessage m WHERE m.messageValue LIKE %:searchTerm%")
    List<KafkaMessage> findByMessageValueContaining(@Param("searchTerm") String searchTerm);

    /**
     * FIND FIRST BY ORDER BY CREATED AT DESC - Get the most recent message
     * 
     * This method finds the latest message based on creation timestamp.
     * Perfect for health checks and monitoring.
     */
    Optional<KafkaMessage> findFirstByOrderByCreatedAtDesc();
}

/*
 * USAGE EXAMPLES:
 * 
 * 1. Get all order messages:
 *    List<KafkaMessage> orders = repository.findByTopic("kafka.learning.orders");
 * 
 * 2. Find failed messages to retry:
 *    List<KafkaMessage> failedMessages = repository.findFailedMessagesForRetry(3);
 * 
 * 3. Count processed messages:
 *    long processed = repository.countByStatus(MessageStatus.PROCESSED);
 * 
 * 4. Get recent messages (last 10):
 *    List<KafkaMessage> recent = repository.findRecentMessages(PageRequest.of(0, 10));
 * 
 * 5. Search for specific content:
 *    List<KafkaMessage> pizzaOrders = repository.findByMessageValueContaining("pizza");
 */
