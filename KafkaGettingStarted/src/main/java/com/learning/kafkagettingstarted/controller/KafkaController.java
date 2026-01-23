/*
 * KAFKA WEB CONTROLLER - YOUR KAFKA REMOTE CONTROL
 * 
 * Think of this class like a TV remote control:
 * - Instead of pointing it at a TV, you point your web browser at it
 * - Instead of changing channels, you send Kafka messages
 * - Each button (endpoint) does something different with Kafka
 * 
 * This is a REST API Controller - it handles web requests like:
 * - When someone visits http://localhost:8080/api/kafka/orders
 * - When someone sends data through a web form or mobile app
 * - It takes the web request and converts it into Kafka messages
 */

package com.learning.kafkagettingstarted.controller;

import java.util.HashMap;  // To create response data
import java.util.List;
import java.util.Map;      // Interface for key-value pairs
import java.util.Random;   // To generate random numbers

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;  // Tool to send responses back to web browser
// Tools to handle web requests
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.kafkagettingstarted.service.KafkaMessagePersistenceService; // Persistence service
// Import our own services and Spring Boot tools
import com.learning.kafkagettingstarted.service.KafkaProducerService;  // Our message sending service

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @RestController - Says "This class handles web requests and returns JSON data"
 *                  It's like putting a sign on your door saying "INFORMATION DESK"
 * 
 * @RequestMapping("/api/kafka") - Says "All URLs in this class start with /api/kafka"
 *                                It's like saying "I live on Kafka Street"
 *         It's like having a notebook to write down what's happening
 */
@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    private static final Logger log = LoggerFactory.getLogger(KafkaController.class);
    
    // Constants to avoid code duplication
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";
    private static final String TOPIC_KEY = "topic";
    private static final String MESSAGE_SENT = "Message sent";
    private static final String ORDERS_TOPIC = "kafka.learning.orders";
    private static final String USECASE_TOPIC = "kafka.learning.usecase";

    private final KafkaProducerService kafkaProducerService;
    private final KafkaMessagePersistenceService persistenceService;
    private final Random random = new Random();  // Our random number generator

    // Constructor for dependency injection
    public KafkaController(KafkaProducerService kafkaProducerService, 
                          KafkaMessagePersistenceService persistenceService) {
        this.kafkaProducerService = kafkaProducerService;
        this.persistenceService = persistenceService;
    }

    /**
     * ORDERS ENDPOINT - Send single order messages
     * 
     * What happens when someone visits: POST http://localhost:8080/api/kafka/orders
     * 
     * This is like having a "Send Order" button on a website:
     * 1. User clicks the button (sends POST request)
     * 2. We receive their data (key and message)
     * 3. We send it to Kafka
     * 4. We tell them "Message sent!" (return response)
     * 
     * @PostMapping("/orders") - Says "listen for POST requests to /api/kafka/orders"
     * @RequestBody - Says "the user will send us JSON data in the request"
     * 
     * Example request body: {"key": "123", "message": "Order for pizza"}
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, String>> sendOrderMessage(
            @RequestBody(required = false) Map<String, String> request) {
        
        // STEP 1: Extract key from request, or generate random one
        // It's like checking "did they write an order number? If not, I'll assign one"
        String key = request != null && request.containsKey("key") 
            ? request.get("key")                           // Use their key
            : String.valueOf(random.nextInt(1000));        // Generate random key (0-999)
            
        // STEP 2: Extract message from request, or create default one
        String message = request != null && request.containsKey(MESSAGE_KEY) 
            ? request.get(MESSAGE_KEY)                       // Use their message
            : "This is order : " + key;                   // Create default message

        // STEP 3: Log what we're doing (like writing in our notebook)
        log.info("Received request to send order message: key={}, message={}", key, message);
        
        // STEP 4: Actually send the message to Kafka using our service
        // This is like giving the order to our delivery person
        kafkaProducerService.sendOrderMessage(key, message);
        
        // STEP 5: Create a response to send back to the user
        // Like giving them a receipt: "Your order #123 has been sent!"
        Map<String, String> response = new HashMap<>();
        response.put(STATUS_KEY, MESSAGE_SENT);
        response.put("key", key);
        response.put(MESSAGE_KEY, message);
        response.put(TOPIC_KEY, ORDERS_TOPIC);
        
        // STEP 6: Send success response back to user (HTTP 200 OK)
        return ResponseEntity.ok(response);
    }

    /**
     * USE CASE ENDPOINT - Send messages to use case topic
     * 
     * This is identical to the orders endpoint, but sends to a different topic.
     * It's like having two different mailboxes - one for orders, one for general use cases.
     * 
     * Example: POST http://localhost:8080/api/kafka/usecase
     * Body: {"key": "test1", "message": "Testing Kafka"}
     */
    @PostMapping("/usecase")
    public ResponseEntity<Map<String, String>> sendUseCaseMessage(
            @RequestBody(required = false) Map<String, String> request) {
        
        // Same logic as orders endpoint, but for use case topic
        String key = request != null && request.containsKey("key") 
            ? request.get("key") 
            : String.valueOf(random.nextInt(1000));
            
        String message = request != null && request.containsKey(MESSAGE_KEY) 
            ? request.get(MESSAGE_KEY) 
            : "This is use case : " + key;

        log.info("Received request to send use case message: key={}, message={}", key, message);
        
        kafkaProducerService.sendUseCaseMessage(key, message);
        
        Map<String, String> response = new HashMap<>();
        response.put(STATUS_KEY, MESSAGE_SENT);
        response.put("key", key);
        response.put(MESSAGE_KEY, message);
        response.put(TOPIC_KEY, USECASE_TOPIC);
        
        return ResponseEntity.ok(response);
    }

    /**
     * BATCH ORDERS ENDPOINT - Send many messages at once
     * 
     * What happens: POST http://localhost:8080/api/kafka/orders/batch?count=5
     * 
     * This is like clicking "Send 5 test orders" instead of clicking "Send order" 5 times.
     * Great for testing how Kafka handles lots of messages!
     * 
     * @RequestParam - Gets the 'count' number from the URL
     * defaultValue = "10" means if they don't specify, send 10 messages
     */
    @PostMapping("/orders/batch")
    public ResponseEntity<Map<String, Object>> sendBatchOrderMessages(
            @RequestParam(defaultValue = "10") int count) {
        
        log.info("Received request to send {} batch order messages", count);
        
        // Generate a random starting number for our batch
        int startKey = random.nextInt(1000);
        
        // Send 'count' number of messages in a loop
        // Like writing several letters and putting them all in the mailbox
        for (int i = startKey; i < startKey + count; i++) {
            String key = String.valueOf(i);
            String message = "This is batch order : " + i;
            kafkaProducerService.sendOrderMessage(key, message);
        }
        
        // Create response showing what we did
        Map<String, Object> response = new HashMap<>();
        response.put(STATUS_KEY, "Batch messages sent");
        response.put("count", count);
        response.put("startKey", startKey);
        response.put("endKey", startKey + count - 1);
        response.put(TOPIC_KEY, ORDERS_TOPIC);
        
        return ResponseEntity.ok(response);
    }

    /**
     * HEALTH CHECK ENDPOINT - Is everything working?
     * 
     * What happens: GET http://localhost:8080/api/kafka/health
     * 
     * This is like a "ping" - just checking if our service is alive and working.
     * Web browsers, monitoring tools, and load balancers often call this.
     * 
     * @GetMapping - Says "listen for GET requests" (not POST)
     * GET is for reading information, POST is for sending data
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put(STATUS_KEY, "UP");
        response.put("service", "Kafka Learning Service");
        response.put("description", "Ready to send messages to Kafka!");
        return ResponseEntity.ok(response);
    }

    /**
     * GET ALL PERSISTED MESSAGES - Retrieve all stored Kafka messages with pagination
     * 
     * What happens: GET http://localhost:8080/api/kafka/messages?page=0&size=10
     * 
     * This is like opening a filing cabinet and browsing through all the stored messages.
     * Perfect for debugging, auditing, and seeing what messages have been processed.
     * 
     * @param page Page number (starting from 0)
     * @param size Number of messages per page (default 10)
     * @return Paginated list of stored Kafka messages
     */
    @GetMapping("/messages")
    public ResponseEntity<List<com.learning.kafkagettingstarted.entity.KafkaMessage>> getAllMessages() {
        log.info("📋 Received request to retrieve all persisted messages");
        
        try {
            List<com.learning.kafkagettingstarted.entity.KafkaMessage> messages = persistenceService.getAllMessages(0, 100).getContent();
            log.info("✅ Successfully retrieved {} messages", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("❌ Failed to retrieve messages: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET MESSAGES BY TOPIC - Retrieve messages from a specific topic
     * 
     * What happens: GET http://localhost:8080/api/kafka/messages/topic/kafka.learning.orders?page=0&size=10
     * 
     * This is like looking for all files in a specific folder.
     * Great for filtering messages by their source topic.
     * 
     * @param topic The Kafka topic to filter by
     * @param page Page number (starting from 0)
     * @param size Number of messages per page (default 10)
     * @return Paginated list of messages from the specified topic
     */
    @GetMapping("/messages/topic/{topic}")
    public ResponseEntity<List<com.learning.kafkagettingstarted.entity.KafkaMessage>> getMessagesByTopic(@PathVariable String topic) {
        log.info("📋 Received request to retrieve messages from topic: {}", topic);
        
        try {
            List<com.learning.kafkagettingstarted.entity.KafkaMessage> messages = persistenceService.getMessagesByTopic(topic);
            log.info("✅ Successfully retrieved {} messages from topic {}", messages.size(), topic);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("❌ Failed to retrieve messages from topic {}: {}", topic, e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET LATEST MESSAGES ENDPOINT - Retrieve the most recent messages
     * 
     * What happens when someone visits: GET http://localhost:8080/api/kafka/messages/latest?limit=10
     * 
     * This is like asking "Show me the last 10 messages that came in"
     * 
     * @param limit Number of messages to retrieve (default: 10)
     * @return List of the most recent KafkaMessage entities
     */
    @GetMapping("/messages/latest")
    public ResponseEntity<List<com.learning.kafkagettingstarted.entity.KafkaMessage>> getLatestMessages(@RequestParam(defaultValue = "10") int limit) {
        log.info("📋 Received request to retrieve latest {} messages", limit);
        
        try {
            List<com.learning.kafkagettingstarted.entity.KafkaMessage> messages = persistenceService.getRecentMessages(limit);
            log.info("✅ Successfully retrieved {} latest messages", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("❌ Failed to retrieve latest messages: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET MESSAGE BY ID - Retrieve a specific message by its database ID
     * 
     * What happens: GET http://localhost:8080/api/kafka/messages/123
     * 
     * This is like asking for a specific file by its ID number.
     * 
     * @param id The database ID of the message
     * @return The specific Kafka message or 404 if not found
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<com.learning.kafkagettingstarted.entity.KafkaMessage> getMessageById(
            @PathVariable Long id) {
        
        log.info("Retrieving message with ID: {}", id);
        
        return persistenceService.getMessageById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET MESSAGE STATISTICS - Get basic stats about stored messages
     * 
     * What happens: GET http://localhost:8080/api/kafka/messages/stats
     * 
     * This provides a dashboard-like view of your message storage.
     * 
     * @return Statistics about stored messages
     */
    @GetMapping("/messages/stats")
    public ResponseEntity<Map<String, Object>> getMessageStats() {
        
        log.info("Retrieving message statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", persistenceService.getTotalMessageCount());
        stats.put("topicCounts", persistenceService.getMessageCountByTopic());
        stats.put("latestMessageTime", persistenceService.getLatestMessageTime());
        
        return ResponseEntity.ok(stats);
    }
}
