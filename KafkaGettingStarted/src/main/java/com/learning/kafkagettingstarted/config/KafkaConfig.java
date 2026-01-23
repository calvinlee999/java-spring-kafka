/*
 * KAFKA CONFIGURATION - SETTING UP OUR MAILBOXES
 * 
 * Think of this class like a post office manager who sets up mailboxes:
 * - We tell Spring Boot "please create these specific mailboxes (topics) for us"
 * - Each mailbox has a name, and rules about how many sections it has
 * - Spring Boot automatically creates these mailboxes when the app starts
 * 
 * This is CONFIGURATION - it's like writing instructions for how to set up your workspace
 */

package com.learning.kafkagettingstarted.config;

// Import the tools we need to create Kafka topics
import org.apache.kafka.clients.admin.NewTopic;     // Blueprint for creating a new topic
import org.springframework.context.annotation.Bean;  // Tells Spring "please create this for me"
import org.springframework.context.annotation.Configuration;  // Says "this class contains setup instructions"
import org.springframework.kafka.config.TopicBuilder;  // Tool to build topic configurations

/**
 * @Configuration - This annotation tells Spring Boot:
 * "Hey! This class contains setup instructions. Please read it when you start the app!"
 * 
 * It's like putting a label on a box that says "IMPORTANT SETUP INSTRUCTIONS - READ FIRST"
 */
@Configuration
public class KafkaConfig {

    /*
     * TOPIC NAMES - Like addresses for our mailboxes
     * 
     * We define these as constants (final variables) so:
     * 1. We can't accidentally change them
     * 2. We can use the same names throughout our app
     * 3. If we need to change them, we only change them in one place
     * 
     * Think of these like having the postal addresses written on a master list
     */
    public static final String ORDERS_TOPIC = "kafka.learning.orders";
    public static final String USECASE_TOPIC = "kafka.learning.usecase";

    /**
     * ORDERS TOPIC BEAN - Creates our first mailbox
     * 
     * @Bean tells Spring: "Please create this topic automatically when you start up"
     * It's like filing a form with the post office saying "please install this mailbox"
     * 
     * What is a PARTITION?
     * Think of a topic like an apartment building mailbox area:
     * - The building (topic) might have 3 mailbox sections (partitions)
     * - Each section can be handled by a different mail worker (consumer)
     * - This allows multiple workers to process mail at the same time
     * - More partitions = more parallel processing power!
     * 
     * What is REPLICATION?
     * Think of replication like having backup copies:
     * - If one mailbox gets damaged, you still have copies
     * - Kafka automatically keeps copies on different servers
     * - This prevents data loss if one server goes down
     * 
     * @return A configured NewTopic that Spring will create in Kafka
     */
    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder
            .name(ORDERS_TOPIC)          // Name: "kafka.learning.orders"
            .partitions(3)               // Create 3 partitions for parallel processing
            .replicas(1)                 // Keep 1 copy (for development - use 3+ in production)
            .compact()                   // Use log compaction (keeps latest value for each key)
            .build();
        /*
         * WHY THESE SETTINGS?
         * 
         * Partitions = 3:
         * - Allows up to 3 consumers to work on messages simultaneously
         * - Good balance between parallelism and complexity for learning
         * - Can handle moderate message volumes
         * 
         * Replicas = 1:
         * - Sufficient for development environment
         * - In production, use 3+ replicas for high availability
         * - Each replica is stored on a different Kafka broker
         * 
         * Compact = true:
         * - If you send multiple messages with the same key, only keeps the latest
         * - Perfect for order updates: "order123 = pending", then "order123 = completed"
         * - Saves storage space and improves performance
         */
    }

    /**
     * USE CASE TOPIC BEAN - Creates our second mailbox
     * 
     * This demonstrates how you can have different topics for different purposes:
     * - Orders go to the orders topic
     * - General use cases go to this topic
     * - Analytics data might go to an analytics topic
     * - Error messages might go to an errors topic
     * 
     * This is called "Topic Design" - organizing your messages logically.
     */
    @Bean
    public NewTopic useCaseTopic() {
        return TopicBuilder
            .name(USECASE_TOPIC)         // Name: "kafka.learning.usecase"
            .partitions(2)               // Fewer partitions - maybe less traffic expected
            .replicas(1)                 // Same replication for development
            .build();                    // No compaction (keeps all messages)
        /*
         * DIFFERENT CONFIGURATION EXAMPLE:
         * 
         * Notice this topic has different settings:
         * - Only 2 partitions (vs 3 for orders)
         * - No compaction (keeps all messages, not just latest)
         * 
         * This shows how different topics can have different characteristics
         * based on their expected usage patterns.
         */
    }

    /*
     * KAFKA CONFIGURATION CONCEPTS FOR BEGINNERS:
     * 
     * 1. TOPICS:
     *    - Like different mailboxes or channels
     *    - Each topic handles a specific type of message
     *    - Examples: orders, payments, notifications, errors
     * 
     * 2. PARTITIONS:
     *    - Splits within a topic for parallel processing
     *    - More partitions = more consumers can work simultaneously
     *    - Messages with the same key always go to the same partition
     * 
     * 3. REPLICATION:
     *    - Backup copies of your data
     *    - Prevents data loss if servers fail
     *    - Essential for production environments
     * 
     * 4. COMPACTION:
     *    - Storage optimization technique
     *    - Keeps only the latest value for each message key
     *    - Great for entity updates (user profiles, order status, etc.)
     * 
     * 5. BEANS:
     *    - Spring Boot's way of creating and managing objects
     *    - @Bean methods run automatically at startup
     *    - Spring takes care of creating these topics in Kafka
     * 
     * Think of this entire configuration like setting up a new post office:
     * - You decide how many mailboxes to install
     * - You decide how many sections each mailbox should have
     * - You set up backup systems in case something breaks
     * - Once configured, the post office runs automatically!
     */
}
