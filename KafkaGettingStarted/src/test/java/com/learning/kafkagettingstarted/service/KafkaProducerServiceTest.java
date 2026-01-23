/*
 * KAFKA PRODUCER SERVICE TESTS - UNIT TESTING FOR MESSAGE SENDING
 * 
 * What is this file for?
 * This file tests the KafkaProducerService in isolation, like testing a single
 * component of a car engine without starting the whole car. It focuses on:
 * - Does the service send messages correctly?
 * - Does it handle errors properly?
 * - Does it work with different types of input?
 * 
 * Why unit tests vs integration tests?
 * - Unit tests are FAST (no Docker, no real Kafka server)
 * - They test specific methods and edge cases
 * - They help pinpoint exactly what's broken
 * - They run as part of every build
 */

package com.learning.kafkagettingstarted.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @ExtendWith(MockitoExtension.class) - Enables Mockito framework for creating mock objects
 *                                      Mocks are like puppet versions of real objects
 * 
 * @Mock - Creates a fake/puppet version of KafkaTemplate for testing
 * @InjectMocks - Automatically injects mock objects into KafkaProducerService
 */
@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    /**
     * MOCK KAFKA TEMPLATE - A puppet version of the real KafkaTemplate
     * 
     * Instead of actually sending messages to Kafka, this mock just pretends to.
     * This lets us test our service logic without needing a real Kafka server.
     * 
     * Think of it like practicing a speech in front of a mirror instead of
     * a real audience - you can still test your performance!
     */
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * SERVICE UNDER TEST - The actual KafkaProducerService we're testing
     * 
     * @InjectMocks automatically puts our mock KafkaTemplate inside this service
     * It's like giving our service a puppet to work with instead of the real thing
     */
    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    /**
     * MOCK SEND RESULT - What we pretend the Kafka send operation returns
     * 
     * This gets created fresh before each test to ensure clean state
     */
    private CompletableFuture<SendResult<String, String>> mockSendResult;

    /**
     * SETUP METHOD - Runs before each test to prepare mock objects
     * 
     * @BeforeEach means "run this method before each @Test method"
     * It's like setting up your desk before each homework assignment
     */
    @BeforeEach
    void setUp() {
        // Create a mock SendResult that represents a successful message send
        SendResult<String, String> sendResult = mock(SendResult.class);
        
        // Create a CompletableFuture that's already completed successfully
        mockSendResult = CompletableFuture.completedFuture(sendResult);
        
        System.out.println("🔧 Test setup complete - mock objects ready!");
    }

    /**
     * TEST: SUCCESSFUL MESSAGE SENDING
     * 
     * This test verifies that our service can send a message successfully
     * when everything works as expected.
     */
    @Test
    void testSendMessage_Success() {
        System.out.println("🧪 Testing successful message sending...");
        
        // ARRANGE - Set up test data and mock behavior
        String testTopic = "test-topic";
        String testKey = "test-key";
        String testMessage = "Hello, Kafka!";
        
        // Tell our mock KafkaTemplate: "When someone calls send(), return success"
        when(kafkaTemplate.send(testTopic, testKey, testMessage))
            .thenReturn(mockSendResult);
        
        // ACT - Call the method we're testing
        // This should complete without throwing any exceptions
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendMessage(testTopic, testKey, testMessage);
        }, "Sending a valid message should not throw exceptions");
        
        // ASSERT - Verify that our service called KafkaTemplate correctly
        verify(kafkaTemplate, times(1)).send(testTopic, testKey, testMessage);
        
        System.out.println("✅ Message sending test passed!");
    }

    /**
     * TEST: SENDING ORDER MESSAGE
     * 
     * This test specifically checks the sendOrderMessage convenience method
     */
    @Test
    void testSendOrderMessage_Success() {
        System.out.println("🧪 Testing order message sending...");
        
        // ARRANGE
        String testKey = "order-123";
        String testMessage = "Pizza order for John";
        
        // Mock the KafkaTemplate to return success for any send operation
        when(kafkaTemplate.send(anyString(), eq(testKey), eq(testMessage)))
            .thenReturn(mockSendResult);
        
        // ACT
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendOrderMessage(testKey, testMessage);
        }, "Sending order message should work correctly");
        
        // ASSERT - Verify the correct topic was used (orders topic)
        verify(kafkaTemplate, times(1)).send(contains("orders"), eq(testKey), eq(testMessage));
        
        System.out.println("✅ Order message test passed!");
    }

    /**
     * TEST: SENDING USE CASE MESSAGE
     * 
     * Tests the sendUseCaseMessage convenience method
     */
    @Test
    void testSendUseCaseMessage_Success() {
        System.out.println("🧪 Testing use case message sending...");
        
        // ARRANGE
        String testKey = "student-456";
        String testMessage = "Student enrollment data";
        
        when(kafkaTemplate.send(anyString(), eq(testKey), eq(testMessage)))
            .thenReturn(mockSendResult);
        
        // ACT
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendUseCaseMessage(testKey, testMessage);
        }, "Sending use case message should work correctly");
        
        // ASSERT - Verify the correct topic was used (use case topic)
        verify(kafkaTemplate, times(1)).send(contains("usecase"), eq(testKey), eq(testMessage));
        
        System.out.println("✅ Use case message test passed!");
    }

    /**
     * TEST: NULL MESSAGE HANDLING
     * 
     * Tests what happens when someone tries to send a null message
     */
    @Test
    void testSendMessage_WithNullMessage() {
        System.out.println("🧪 Testing null message handling...");
        
        // ARRANGE
        String testTopic = "test-topic";
        String testKey = "test-key";
        String nullMessage = null;
        
        when(kafkaTemplate.send(testTopic, testKey, nullMessage))
            .thenReturn(mockSendResult);
        
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendMessage(testTopic, testKey, nullMessage);
        }, "Service should handle null messages gracefully");
        
        verify(kafkaTemplate, times(1)).send(testTopic, testKey, nullMessage);
        
        System.out.println("✅ Null message handling test passed!");
    }

    /**
     * TEST: EMPTY MESSAGE HANDLING
     * 
     * Tests what happens with empty strings
     */
    @Test
    void testSendMessage_WithEmptyMessage() {
        System.out.println("🧪 Testing empty message handling...");
        
        // ARRANGE
        String testTopic = "test-topic";
        String testKey = "test-key";
        String emptyMessage = "";
        
        when(kafkaTemplate.send(testTopic, testKey, emptyMessage))
            .thenReturn(mockSendResult);
        
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendMessage(testTopic, testKey, emptyMessage);
        }, "Service should handle empty messages");
        
        verify(kafkaTemplate, times(1)).send(testTopic, testKey, emptyMessage);
        
        System.out.println("✅ Empty message handling test passed!");
    }

    /**
     * TEST: LARGE MESSAGE HANDLING
     * 
     * Tests sending a large message (simulating real-world scenarios)
     */
    @Test
    void testSendMessage_WithLargeMessage() {
        System.out.println("🧪 Testing large message handling...");
        
        // ARRANGE - Create a large message (1MB of text)
        StringBuilder largeMessage = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeMessage.append("This is a large message for testing purposes. ");
        }
        
        String testTopic = "test-topic";
        String testKey = "large-message-key";
        
        when(kafkaTemplate.send(testTopic, testKey, largeMessage.toString()))
            .thenReturn(mockSendResult);
        
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendMessage(testTopic, testKey, largeMessage.toString());
        }, "Service should handle large messages");
        
        verify(kafkaTemplate, times(1)).send(testTopic, testKey, largeMessage.toString());
        
        System.out.println("✅ Large message handling test passed!");
        System.out.println("📏 Message size: " + largeMessage.length() + " characters");
    }

    /**
     * TEST: SPECIAL CHARACTERS IN MESSAGE
     * 
     * Tests messages with special characters, unicode, etc.
     */
    @Test
    void testSendMessage_WithSpecialCharacters() {
        System.out.println("🧪 Testing special character handling...");
        
        // ARRANGE - Message with various special characters
        String testTopic = "test-topic";
        String testKey = "special-chars";
        String specialMessage = "Special chars: 🚀 café naïve résumé 中文 العربية русский";
        
        when(kafkaTemplate.send(testTopic, testKey, specialMessage))
            .thenReturn(mockSendResult);
        
        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            kafkaProducerService.sendMessage(testTopic, testKey, specialMessage);
        }, "Service should handle special characters");
        
        verify(kafkaTemplate, times(1)).send(testTopic, testKey, specialMessage);
        
        System.out.println("✅ Special character handling test passed!");
    }

    /*
     * WHAT THESE TESTS TEACH US:
     * 
     * 1. ISOLATION - Each test focuses on one specific behavior
     * 2. MOCKING - We can test our logic without external dependencies
     * 3. EDGE CASES - We test unusual inputs (null, empty, large, special chars)
     * 4. VERIFICATION - We confirm our service calls other components correctly
     * 5. DOCUMENTATION - Tests serve as examples of how to use the service
     * 
     * HOW TO RUN THESE TESTS:
     * 
     * From VS Code:
     * - Click "Run Test" above any @Test method
     * - Right-click class name and select "Run Tests"
     * 
     * From Terminal:
     * - mvn test -Dtest=KafkaProducerServiceTest
     * - mvn test (runs all tests)
     * 
     * WHY THESE TESTS ARE VALUABLE:
     * 
     * 1. FAST - No Docker, no real Kafka, just pure Java logic testing
     * 2. RELIABLE - Always pass/fail consistently (no network issues)
     * 3. PRECISE - Pinpoint exactly which method has problems
     * 4. COMPREHENSIVE - Cover edge cases you might not think to test manually
     * 5. REGRESSION PROTECTION - Catch when future changes break existing code
     */
}
