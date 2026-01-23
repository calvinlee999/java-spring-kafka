/*
 * KAFKA PRODUCER SERVICE - THE MESSAGE DELIVERY WORKER
 * 
 * Think of this class like a postal worker who specializes in delivering messages:
 * - They know exactly how to package messages properly
 * - They know all the postal routes (topics)
 * - They can tell you if delivery was successful or failed
 * - They handle the complicated parts so you don't have to worry about them
 * 
 * This is a SERVICE class - it contains the business logic for sending messages to Kafka.
 * Controllers call this service, and this service does the actual work.
 */

package com.learning.kafkagettingstarted.service;

import java.util.concurrent.CompletableFuture;  // For handling asynchronous (non-blocking) operations

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;  // Spring's Kafka sending tool
import org.springframework.kafka.support.SendResult;  // Result information after sending
import org.springframework.stereotype.Service;        // Tells Spring this is a service

// Import our configuration and Spring Boot tools
import com.learning.kafkagettingstarted.config.KafkaConfig;  // Our topic names

import lombok.RequiredArgsConstructor;  // Auto-generates constructor

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @Service - Tells Spring "This is a service class that contains business logic"
 *           It's like putting a name tag that says "EXPERT MESSAGE SENDER"
 * 
 * @RequiredArgsConstructor - Lombok magic! Creates a constructor that takes KafkaTemplate
 * 
 * @Slf4j - Creates a 'log' variable for debugging and monitoring
 */
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    /*
     * KAFKA TEMPLATE - Our message sending tool
     * 
     * Think of KafkaTemplate like a smart mailbox:
     * - You give it a message, it figures out how to deliver it
     * - It handles all the networking, serialization, and error handling
     * - It can send messages asynchronously (non-blocking)
     * - Spring Boot automatically creates this for us based on our application.properties
     * 
     * <String, String> means:
     * - First String: the type of message keys (like order numbers)
     * - Second String: the type of message values (like order details)
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * GENERIC MESSAGE SENDER - The core method that does all the work
     * 
     * This is like the main postal worker who can deliver to any address:
     * 
     * @param topic - Which mailbox to deliver to (like "kafka.learning.orders")
     * @param key - The identifier for this message (like an order number)
     * @param message - The actual content to send (like order details)
     * 
     * What happens step by step:
     * 1. We log what we're about to send (for debugging)
     * 2. We ask KafkaTemplate to send the message
     * 3. We get back a "future" - a promise that the sending will complete later
     * 4. We set up callback functions for success and failure
     */
    public void sendMessage(String topic, String key, String message) {
        // STEP 1: Log what we're about to do
        log.info("📤 Sending message to topic {}: key={}, message={}", topic, key, message);
        
        // STEP 2: Send the message asynchronously
        // This doesn't block - the method returns immediately with a "promise"
        // Meanwhile, the actual sending happens in the background
        CompletableFuture<SendResult<String, String>> future = 
            kafkaTemplate.send(topic, key, message);
        
        // STEP 3: Set up what to do when sending completes (success or failure)
        // This is like saying "when the postal worker gets back, here's what to do..."
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                // SUCCESS! The message was delivered successfully
                log.info("✅ Message sent successfully: key={}, message={}, offset={}", 
                    key, message, result.getRecordMetadata().offset());
                /*
                 * OFFSET explained:
                 * Think of offset like a receipt number at a deli counter:
                 * - Each message gets a sequential number (0, 1, 2, 3...)
                 * - This helps track which messages have been processed
                 * - Consumers use offsets to know where they left off reading
                 */
            } else {
                // FAILURE! Something went wrong during delivery
                log.error("❌ Failed to send message: key={}, message={}, error={}", 
                    key, message, exception.getMessage());
                /*
                 * Common reasons for failure:
                 * - Kafka server is down
                 * - Network connection issues
                 * - Topic doesn't exist
                 * - Message is too large
                 * - Authentication problems
                 */
                log.error("❌ Failed to send message: key={}, message={}", key, message, exception);
                /*
                 * In a real application, you might:
                 * - Retry sending the message
                 * - Store it in a dead letter queue
                 * - Alert monitoring systems
                 * - Return an error to the user
                 */
            }
        });
        
        /*
         * IMPORTANT: This method returns immediately!
         * The actual sending happens asynchronously in the background.
         * This is good because:
         * - Your web app doesn't freeze while sending messages
         * - You can send many messages quickly
         * - Kafka can batch multiple messages for efficiency
         */
    }

    /**
     * CONVENIENCE METHOD: Send Order Messages
     * 
     * This is like having a specialized postal worker who only delivers to the Orders mailbox.
     * Instead of remembering the topic name, you just say "send this order"
     * 
     * @param key - Order identifier (like order number)
     * @param message - Order details (like "Pizza order for John")
     */
    public void sendOrderMessage(String key, String message) {
        // Call our generic method with the orders topic
        // KafkaConfig.ORDERS_TOPIC = "kafka.learning.orders"
        sendMessage(KafkaConfig.ORDERS_TOPIC, key, message);
    }

    /**
     * CONVENIENCE METHOD: Send Use Case Messages
     * 
     * Same as sendOrderMessage, but for the use case topic.
     * This demonstrates how you can have different message types
     * going to different topics for organization.
     * 
     * @param key - Use case identifier
     * @param message - Use case details
     */
    public void sendUseCaseMessage(String key, String message) {
        // Call our generic method with the use case topic
        // KafkaConfig.USECASE_TOPIC = "kafka.learning.usecase"
        sendMessage(KafkaConfig.USECASE_TOPIC, key, message);
    }

    /*
     * WHY USE A SERVICE CLASS?
     * 
     * 1. SEPARATION OF CONCERNS:
     *    - Controllers handle web requests/responses
     *    - Services handle business logic (like sending messages)
     *    - This makes code easier to understand and test
     * 
     * 2. REUSABILITY:
     *    - Multiple controllers can use the same service
     *    - You can also call this service from scheduled jobs, event handlers, etc.
     * 
     * 3. TESTABILITY:
     *    - You can easily test the service separately from the web layer
     *    - You can mock the service when testing controllers
     * 
     * 4. CONFIGURATION MANAGEMENT:
     *    - All Kafka-related configuration is handled in one place
     *    - If you need to change how messages are sent, you only change it here
     * 
     * Think of it like this:
     * - Controller = Waiter (takes your order)
     * - Service = Chef (prepares the food)
     * - Repository = Pantry (stores ingredients)
     * 
     * Each has a specific job, and they work together to serve you!
     */
}
