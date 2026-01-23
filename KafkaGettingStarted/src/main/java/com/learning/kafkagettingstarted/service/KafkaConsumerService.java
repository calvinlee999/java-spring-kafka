/*
 * KAFKA CONSUMER SERVICE - THE MESSAGE RECEIVER WORKER
 * 
 * Think of this class like a mail clerk who automatically processes incoming mail:
 * - They sit by the mailbox and check for new messages every few milliseconds
 * - When mail arrives, they automatically open it and process it
 * - They keep track of which mail they've already processed
 * - If something goes wrong, they can retry or set the mail aside
 * 
 * This is a SERVICE class that LISTENS for messages from Kafka topics.
 * Unlike the simple consumer example, this one runs automatically in the background!
 */

package com.learning.kafkagettingstarted.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;  // Magic annotation for listening
import org.springframework.kafka.support.Acknowledgment;   // Tool to confirm message processing
import org.springframework.kafka.support.KafkaHeaders;     // Access to message metadata
import org.springframework.messaging.handler.annotation.Header;   // Gets header values
import org.springframework.messaging.handler.annotation.Payload;  // Gets message content
import org.springframework.stereotype.Service;             // Tells Spring this is a service

// Import our configuration and Spring Boot tools
import com.learning.kafkagettingstarted.config.KafkaConfig;  // Our topic names

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @Service - Tells Spring "This is a service class that contains business logic"
 *           It's like putting a name tag that says "EXPERT MESSAGE PROCESSOR"
 * 
 * @Slf4j - Creates a 'log' variable for debugging and monitoring
 */
@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    
    private final KafkaMessagePersistenceService persistenceService;

    public KafkaConsumerService(KafkaMessagePersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * ORDERS MESSAGE LISTENER - Automatically processes order messages
     * 
     * This method is MAGICAL! ✨
     * - You don't call it directly - Spring calls it automatically
     * - Whenever a new message arrives in the orders topic, this method runs
     * - It's like having a dedicated worker who never sleeps!
     * 
     * @KafkaListener - The magic annotation that makes this work:
     * - topics = which mailboxes to watch
     * - groupId = our consumer group name (like our team name)
     * 
     * CONSUMER GROUPS explained:
     * Think of a consumer group like a team of workers:
     * - All workers in the same group share the workload
     * - Each message goes to only ONE worker in the group
     * - If you have 3 partitions and 3 workers, each worker gets 1 partition
     * - If a worker goes offline, others pick up their work
     * 
     * @param message - The actual message content (like order details)
     * @param key - The message key (like order number)
     * @param partition - Which partition this message came from (0, 1, 2, etc.)
     * @param offset - The position of this message in the partition (like a line number)
     * @param acknowledgment - Tool to say "I finished processing this message"
     */
    @KafkaListener(topics = KafkaConfig.ORDERS_TOPIC, groupId = "orders-consumer-group")
    public void consumeOrderMessage(
            @Payload String message,                                    // The message content
            @Header(KafkaHeaders.RECEIVED_KEY) String key,             // The message key
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,    // Which partition (0, 1, 2)
            @Header(KafkaHeaders.OFFSET) long offset,                  // Position in partition
            Acknowledgment acknowledgment) {                           // Tool to confirm processing
        
        // STEP 1: Log that we received a message (for debugging)
        log.info("📬 Received order message - Key: {}, Message: {}, Partition: {}, Offset: {}", 
                key, message, partition, offset);
        
        // STEP 2: Save message to PostgreSQL database for persistence and auditing
        try {
            persistenceService.saveKafkaMessage(
                KafkaConfig.ORDERS_TOPIC, 
                key, 
                message, 
                partition, 
                offset
            );
            log.debug("💾 Message saved to database successfully");
        } catch (Exception e) {
            log.error("❌ Failed to save message to database: {}", e.getMessage(), e);
            // Continue processing even if database save fails
        }
        
        // STEP 3: Try to process the message
        try {
            // Call our business logic method to actually handle the order
            processOrderMessage(key, message);
            
            // STEP 4: Tell Kafka "I successfully processed this message"
            // This is like signing for a package delivery
            acknowledgment.acknowledge();
            log.info("✅ Order message processed and acknowledged successfully");
            
            /*
             * ACKNOWLEDGMENT explained:
             * - When we acknowledge, Kafka marks this message as "processed"
             * - Kafka won't send us this message again
             * - If we don't acknowledge, Kafka will retry sending it
             * - This ensures messages don't get lost if our app crashes
             */
            
        } catch (Exception e) {
            // STEP 4: If something goes wrong, log the error
            log.error("❌ Error processing order message: {}", e.getMessage(), e);
            
            // DON'T acknowledge on error!
            // This tells Kafka "something went wrong, please try again later"
            // Kafka will redeliver this message (with backoff delays)
            
            /*
             * ERROR HANDLING strategies:
             * 1. Retry automatically (what we're doing here)
             * 2. Send to a "dead letter queue" after max retries
             * 3. Alert monitoring systems
             * 4. Store failed messages for manual review
             */
        }
    }

    /**
     * USE CASE MESSAGE LISTENER - Processes use case messages
     * 
     * This is identical to the orders listener, but for a different topic.
     * This demonstrates how you can have separate processing logic
     * for different types of messages.
     * 
     * Same parameters and logic as the orders listener above.
     */
    @KafkaListener(topics = KafkaConfig.USECASE_TOPIC, groupId = "usecase-consumer-group")
    public void consumeUseCaseMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("📬 Received use case message - Key: {}, Message: {}, Partition: {}, Offset: {}", 
                key, message, partition, offset);
        
        try {
            // Process the use case message
            processUseCaseMessage(key, message);
            
            acknowledgment.acknowledge();
            log.info("✅ Use case message processed and acknowledged successfully");
            
        } catch (Exception e) {
            log.error("❌ Error processing use case message: {}", e.getMessage(), e);
            // Don't acknowledge on error - message will be retried
        }
    }

    /**
     * BUSINESS LOGIC: Process an order message
     * 
     * This is where you put your actual business logic for handling orders:
     * - Validate the order data
     * - Save to database
     * - Send confirmation emails
     * - Update inventory
     * - Call external APIs
     * - etc.
     * 
     * For this learning example, we just log and simulate some processing time.
     * 
     * @param key - The order identifier (like order number)
     * @param message - The order details (like "Pizza order for John")
     */
    private void processOrderMessage(String key, String message) {
        log.info("🏪 Processing order: {} -> {}", key, message);
        
        // Simulate some processing time (like calling an external API)
        // In real life, you might:
        // - Validate the order data
        // - Check inventory
        // - Calculate taxes
        // - Save to database
        // - Send confirmation email
        try {
            Thread.sleep(100); // Sleep for 100 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ Order processed successfully: {}", key);
    }

    /**
     * BUSINESS LOGIC: Process a use case message
     * 
     * Similar to order processing, but for use case messages.
     * This demonstrates how different message types can have
     * different processing logic.
     * 
     * @param key - The use case identifier
     * @param message - The use case details
     */
    private void processUseCaseMessage(String key, String message) {
        log.info("🔧 Processing use case: {} -> {}", key, message);
        
        // Different processing logic for use case messages
        // Maybe this involves:
        // - Running analytics
        // - Updating metrics
        // - Triggering workflows
        // - etc.
        
        try {
            Thread.sleep(50); // Simulate lighter processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ Use case processed successfully: {}", key);
    }

    /*
     * KAFKA CONSUMER CONCEPTS FOR BEGINNERS:
     * 
     * 1. AUTOMATIC LISTENING:
     *    - These methods run automatically when messages arrive
     *    - You don't need to call them manually
     *    - Spring Boot starts background threads to listen
     * 
     * 2. CONSUMER GROUPS:
     *    - Multiple consumers can work together as a team
     *    - Each message goes to only one consumer in the group
     *    - This allows for horizontal scaling
     * 
     * 3. PARTITIONS:
     *    - Topics are divided into partitions (like multiple lanes on a highway)
     *    - Each partition is handled by one consumer in a group
     *    - More partitions = more parallel processing
     * 
     * 4. OFFSETS:
     *    - Like bookmarks in a book
     *    - Kafka remembers where each consumer left off
     *    - If your app crashes and restarts, it continues from where it left off
     * 
     * 5. ACKNOWLEDGMENTS:
     *    - Like saying "I got it!" after receiving a message
     *    - Tells Kafka the message was processed successfully
     *    - Prevents the same message from being processed twice
     * 
     * Think of it like a restaurant:
     * - Kafka = The kitchen sending out orders
     * - Consumer = The waiter delivering orders to tables
     * - Consumer Group = The team of waiters working together
     * - Partitions = Different sections of the restaurant
     * - Acknowledgment = Confirming the customer received their order
     */
}
