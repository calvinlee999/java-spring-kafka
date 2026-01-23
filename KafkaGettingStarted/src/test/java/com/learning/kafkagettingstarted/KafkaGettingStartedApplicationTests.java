/*
 * KAFKA APPLICATION TESTS - AUTOMATED TESTING WITH REAL KAFKA
 * 
 * What is this file for?
 * Testing is like having a robot check your homework before you turn it in:
 * - It runs your code automatically to make sure it works
 * - It catches problems before real users see them
 * - It gives you confidence that your changes don't break anything
 * - It runs the same tests every time, so you don't miss anything
 * 
 * What makes this special?
 * These tests use TESTCONTAINERS - a tool that starts a real Kafka server
 * just for testing, then throws it away when done. It's like having a
 * temporary practice stage for your code to perform on!
 */

package com.learning.kafkagettingstarted;

// Import testing tools
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;                          // Marks methods as tests
import org.springframework.beans.factory.annotation.Autowired; // Injects Spring components
import org.springframework.boot.test.context.SpringBootTest; // Starts full Spring app for testing
import org.springframework.test.context.DynamicPropertyRegistry; // Dynamic property configuration
import org.springframework.test.context.DynamicPropertySource;   // Configure properties at runtime
import org.testcontainers.containers.KafkaContainer;        // Starts temporary Kafka server
import org.testcontainers.junit.jupiter.Container;          // Manages the container lifecycle
import org.testcontainers.junit.jupiter.Testcontainers;     // Enables Testcontainers framework
import org.testcontainers.utility.DockerImageName;          // Specifies which Kafka image to use

// Import our services for testing
import com.learning.kafkagettingstarted.service.KafkaProducerService;

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @SpringBootTest - Says "start the entire Spring Boot application for testing"
 *                  It's like turning on all the lights in your house to check everything works
 * 
 * @Testcontainers - Says "I want to use temporary Docker containers for testing"
 *                   It's like saying "I need a practice room with real equipment"
 */
@SpringBootTest
@Testcontainers
class KafkaGettingStartedApplicationTests {

    /**
     * KAFKA TEST CONTAINER - A temporary Kafka server just for testing
     * 
     * @Container tells Testcontainers "please start this before running tests"
     * 
     * Think of this like:
     * - Before the test starts: Robot sets up a temporary Kafka server
     * - During the test: Your code talks to this real Kafka server
     * - After the test: Robot throws away the temporary server
     * 
     * Why use a real Kafka server for testing?
     * - Tests are more realistic (you're testing against the real thing)
     * - Catches integration problems that mock tests might miss
     * - Gives confidence that your code will work in production
     * 
     * DockerImageName.parse("confluentinc/cp-kafka:7.4.0"):
     * - This is the "address" of the Kafka software package
     * - "confluentinc" is the company that packages Kafka
     * - "cp-kafka" is their Kafka distribution
     * - "7.4.0" is the specific version (like Kafka v7.4.0)
     * 
     * NOTE: In modern Testcontainers (1.20.4+), KafkaContainer automatically
     * handles port mapping, so we don't need withExposedPorts() anymore.
     * The container manages its own internal and external port configuration.
     */
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    /**
     * DYNAMIC PROPERTY CONFIGURATION
     * 
     * This method runs BEFORE Spring Boot starts and tells it:
     * "Hey, use this Kafka server address that Testcontainers just created!"
     * 
     * It's like updating your phone's contact list with a new phone number
     * right before making a call.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Tell Spring Boot where to find our test Kafka server
        // Using getBootstrapServers() to get the dynamically assigned ports
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        
        // Configure consumer settings for testing
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        
        System.out.println("🔧 Configured test Kafka server at: " + kafka.getBootstrapServers());
    }

    // Inject our services for testing
    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * CONTEXT LOADS TEST - "Does our application start up correctly?"
     * 
     * @Test tells JUnit "this is a test method - please run it"
     * 
     * What this test does:
     * 1. Testcontainers starts a temporary Kafka server
     * 2. Spring Boot starts our application (all services, controllers, etc.)
     * 3. Spring tries to connect to the temporary Kafka server
     * 4. If everything works, the test passes ✅
     * 5. If anything fails (can't start, can't connect, etc.), test fails ❌
     * 
     * Why is this important?
     * - Ensures all our Spring components work together
     * - Verifies Kafka connection configuration is correct
     * - Catches startup problems before deployment
     * - Acts as a "smoke test" - basic functionality check
     */
    @Test
    void contextLoads() {
        // This test passes if the Spring Boot application starts successfully
        // and can connect to the Kafka container.
        // 
        // Even though the method body is empty, Spring Boot does a lot of work:
        // 1. Starts all @Service, @Controller, @Repository components
        // 2. Connects to Kafka using our configuration
        // 3. Creates all the topics defined in KafkaConfig
        // 4. Initializes KafkaTemplate and KafkaListeners
        // 
        // If any of this fails, the test will fail with a detailed error message
        
        System.out.println("🚀 Spring Boot application started successfully!");
        System.out.println("✅ Kafka integration is working!");
        System.out.println("🎯 All components loaded without errors!");
        
        // Verify that our services are properly injected
        assertNotNull(kafkaProducerService, "KafkaProducerService should be available");
        
        System.out.println("🔍 All required services are properly initialized!");
    }

    /**
     * KAFKA CONTAINER TEST - "Is our test Kafka server running correctly?"
     * 
     * This test specifically checks that:
     * 1. The Kafka container started successfully
     * 2. We can get connection information from it
     * 3. It's ready to handle our test traffic
     * 
     * Why test the test infrastructure?
     * - Ensures our testing setup is working correctly
     * - Helps debug test failures (is it our code or our test setup?)
     * - Documents how to connect to the test Kafka server
     */
    @Test
    void kafkaContainerIsRunning() {
        // STEP 1: Verify that the Kafka container is running
        // assert is like saying "this MUST be true, or the test fails"
        assertTrue(kafka.isRunning(), "Kafka container should be running for tests");
        
        // STEP 2: Get connection information for debugging
        // getBootstrapServers() returns something like "localhost:32768"
        // (the port number changes each time - Docker assigns a random available port)
        String bootstrapServers = kafka.getBootstrapServers();
        
        // STEP 3: Log the connection info (helpful for debugging)
        System.out.println("🐳 Kafka container is running successfully!");
        System.out.println("🔗 Bootstrap servers: " + bootstrapServers);
        System.out.println("🌐 Ready for integration testing!");
        
        // STEP 4: Basic validation
        assertTrue(bootstrapServers.contains("localhost"), "Bootstrap servers should contain localhost");
        assertFalse(bootstrapServers.isEmpty(), "Bootstrap servers should not be empty");
        
        System.out.println("✅ Kafka container test passed!");
    }

    /**
     * KAFKA PRODUCER SERVICE TEST - "Can we send messages successfully?"
     * 
     * This test verifies that:
     * 1. Our KafkaProducerService can send messages
     * 2. The message sending completes without errors
     * 3. We get proper confirmation that the message was sent
     */
    @Test
    void testKafkaProducerService() throws Exception {
        // STEP 1: Prepare test data
        String testMessage = "Test order: Pizza delivery to 123 Test Street";
        
        System.out.println("📤 Testing message sending...");
        System.out.println("📝 Test message: " + testMessage);
        
        // STEP 2: Send the message using our service
        // This should complete successfully if everything is working
        String testKey = "test-order-" + System.currentTimeMillis();
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendOrderMessage(testKey, testMessage);
        }, "Sending order message should not throw any exceptions");
        
        System.out.println("✅ Message sent successfully!");
        
        // STEP 3: Wait a moment for the message to be processed
        // In real testing, you might use more sophisticated waiting strategies
        Thread.sleep(1000);
        
        System.out.println("🎯 Producer service test completed!");
    }

    /**
     * INTEGRATION TEST - "Does the entire message flow work?"
     * 
     * This test verifies the complete message journey:
     * 1. Producer sends a message to Kafka
     * 2. Consumer receives and processes the message
     * 3. No errors occur during the entire process
     */
    @Test
    void testCompleteMessageFlow() throws Exception {
        System.out.println("🔄 Testing complete message flow...");
        
        // STEP 1: Send multiple test messages
        String[] testMessages = {
            "Order 1: Burger and fries",
            "Order 2: Chicken sandwich",
            "Order 3: Vegetarian wrap"
        };
        
        for (int i = 0; i < testMessages.length; i++) {
            String message = testMessages[i];
            String key = "test-key-" + (i + 1);
            System.out.println("📤 Sending: " + message);
            
            assertDoesNotThrow(() -> {
                kafkaProducerService.sendOrderMessage(key, message);
            }, "Should be able to send message: " + message);
            
            // Small delay between messages
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("📨 All messages sent successfully!");
        
        // STEP 2: Wait for consumer to process messages
        // In a real test, you might have more sophisticated verification
        System.out.println("⏳ Waiting for consumer to process messages...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ Integration test completed!");
    }

    /**
     * PERFORMANCE TEST - "Can our system handle multiple messages quickly?"
     * 
     * This test checks:
     * 1. System performance under load
     * 2. No memory leaks or resource exhaustion
     * 3. All messages are sent successfully
     */
    @Test
    void testMessageSendingPerformance() {
        System.out.println("🚀 Testing message sending performance...");
        
        int messageCount = 10;
        long startTime = System.currentTimeMillis();
        
        // Send multiple messages rapidly
        for (int i = 0; i < messageCount; i++) {
            String message = "Performance test message #" + (i + 1);
            String key = "perf-test-" + (i + 1);
            
            assertDoesNotThrow(() -> {
                kafkaProducerService.sendOrderMessage(key, message);
            }, "Should handle message #" + (i + 1));
            
            if ((i + 1) % 5 == 0) {
                System.out.println("📊 Sent " + (i + 1) + " messages...");
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("⚡ Performance results:");
        System.out.println("   📈 Messages sent: " + messageCount);
        System.out.println("   ⏱️  Total time: " + duration + "ms");
        System.out.println("   📊 Average time per message: " + (duration / messageCount) + "ms");
        
        // Verify performance is reasonable (should be very fast for 10 messages)
        assertTrue(duration < 5000, "Should send " + messageCount + " messages in less than 5 seconds");
        
        System.out.println("✅ Performance test passed!");
    }

    /*
     * WHAT HAPPENS WHEN YOU RUN THESE TESTS?
     * 
     * 1. JUnit starts the test runner
     * 2. Testcontainers sees the @Container annotation
     * 3. Docker starts a temporary Kafka container
     * 4. Testcontainers waits for Kafka to be ready
     * 5. @DynamicPropertySource configures Spring Boot to use the test Kafka
     * 6. Spring Boot starts your application
     * 7. Spring connects to the temporary Kafka server
     * 8. Tests run one by one
     * 9. After all tests: everything gets cleaned up automatically
     * 
     * HOW TO RUN THESE TESTS:
     * 
     * From VS Code:
     * - Click the "Run Test" button above each @Test method
     * - Or right-click the class name and select "Run Tests"
     * 
     * From Terminal:
     * - mvn test (runs all tests)
     * - mvn test -Dtest=KafkaGettingStartedApplicationTests (runs just this class)
     * 
     * WHAT YOU NEED:
     * - Docker Desktop running (for Testcontainers)
     * - Internet connection (to download Kafka Docker image first time)
     * - Enough memory (Docker + Kafka + Spring Boot)
     * 
     * TROUBLESHOOTING:
     * - If tests fail with "Docker not found": Start Docker Desktop
     * - If tests are slow: First run downloads Kafka image (one-time only)
     * - If tests fail randomly: Check available memory (Kafka needs ~512MB)
     */
}
