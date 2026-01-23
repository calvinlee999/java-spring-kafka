/*
 * CHAPTER 6: KAFKA USE CASE PRODUCER - ADVANCED LEARNING EXAMPLE
 * 
 * This is similar to Chapter 5's simple producer, but with a real-world use case:
 * STUDENT ENROLLMENT SYSTEM
 * 
 * Imagine this scenario:
 * - A university has an online enrollment system
 * - When students register for classes, the system sends messages
 * - Other systems listen for these messages to:
 *   * Update student records
 *   * Send welcome emails
 *   * Generate student ID cards
 *   * Update billing systems
 *   * Send notifications to professors
 * 
 * This demonstrates how Kafka enables "event-driven architecture"
 * - One action (student enrollment) triggers multiple automated processes
 * - Systems are loosely coupled (don't directly call each other)
 * - Easy to add new features without changing existing code
 */

package com.learning.kafkagettingstarted.chapter6;

// Same imports as Chapter 5, but we're using them for a different purpose
import org.apache.kafka.clients.producer.KafkaProducer;     // Tool to send messages
import org.apache.kafka.clients.producer.ProducerConfig;    // Configuration settings
import org.apache.kafka.clients.producer.ProducerRecord;    // Individual message

import java.util.Properties;  // Configuration holder
import java.util.Random;      // Random number generator

/**
 * KafkaUseCaseProducer - Student Enrollment Event Producer
 * 
 * Real-world scenario: This could be part of a student registration web app
 * 
 * What this simulates:
 * 1. 10 students enrolling in the university
 * 2. Each enrollment creates a message with student info
 * 3. The message goes to "kafka.usecase.students" topic
 * 4. Other systems can listen to this topic and react automatically
 * 
 * In a real system, this code might be triggered by:
 * - A student submitting an online form
 * - An admin importing a CSV file of new students
 * - A mobile app completing registration
 * - An integration with another university system
 */
public class KafkaUseCaseProducer {

    public static void main(String[] args) {

        // STEP 1: Configure the Producer (same as Chapter 5)
        // Think of this as setting up the "student enrollment notification system"
        Properties kafkaProps = new Properties();

        // Connect to Kafka broker (our message delivery service)
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");

        // Set up serializers (convert text to bytes for computer transmission)
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        // Create our student enrollment event producer
        KafkaProducer<String, String> studentProducer = new KafkaProducer<>(kafkaProps);

        // STEP 2: Simulate 10 student enrollments
        try{
            // Generate a random starting student ID (like starting from ID 500, 501, 502...)
            int startStudentId = (new Random()).nextInt(1000);

            System.out.println("🎓 Starting Student Enrollment Simulation...");
            System.out.println("📝 Enrolling 10 students starting from ID: " + startStudentId);

            // Process each student enrollment
            for( int i = startStudentId; i < startStudentId + 10; i++) {

                // STEP 3: Create a student enrollment event message
                // In a real system, this might contain:
                // - Student name, email, phone
                // - Selected courses
                // - Payment information
                // - Enrollment timestamp
                // - Campus location
                ProducerRecord<String,String> enrollmentEvent =
                        new ProducerRecord<>(
                                "kafka.usecase.students",           // Topic: where enrollment events go
                                String.valueOf(i),                  // Key: student ID
                                "Student enrolled: ID=" + i +       // Message: enrollment details
                                ", Name=Student" + i + 
                                ", Status=ENROLLED" +
                                ", Timestamp=" + System.currentTimeMillis()
                        );

                // STEP 4: Log what we're doing
                System.out.println("📤 Sending Enrollment Event: " + enrollmentEvent.toString());

                // STEP 5: Send the enrollment event to Kafka
                // This triggers all the downstream processes:
                // - Email service sends welcome email
                // - ID card system creates student ID
                // - Billing system sets up payment plan
                // - Academic system creates transcript
                studentProducer.send(enrollmentEvent);

                // STEP 6: Wait 2 seconds before next enrollment
                // This simulates realistic timing (students don't all enroll at exactly the same time)
                Thread.sleep(2000);

                System.out.println("✅ Student " + i + " enrollment event sent successfully!");
            }

            System.out.println("🎉 All student enrollments completed!");

        }
        catch(Exception e) {
            // Handle any errors that occur during enrollment processing
            System.err.println("❌ Error during student enrollment: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            // STEP 7: Always clean up resources
            studentProducer.close();
            System.out.println("📚 Student enrollment system shutdown complete.");
        }

        /*
         * WHAT HAPPENS NEXT?
         * 
         * After running this producer, you could have these consumers:
         * 
         * 1. EMAIL SERVICE CONSUMER:
         *    - Listens to "kafka.usecase.students" topic
         *    - Sends welcome email to each new student
         * 
         * 2. ID CARD SERVICE CONSUMER:
         *    - Generates and prints student ID cards
         * 
         * 3. BILLING SYSTEM CONSUMER:
         *    - Sets up payment plans and tuition accounts
         * 
         * 4. ACADEMIC SYSTEM CONSUMER:
         *    - Creates student transcripts and academic records
         * 
         * 5. NOTIFICATION SERVICE CONSUMER:
         *    - Sends SMS notifications to students
         * 
         * 6. ANALYTICS CONSUMER:
         *    - Tracks enrollment trends and statistics
         * 
         * Each system works independently but responds to the same events!
         * This is the power of event-driven architecture with Kafka.
         */
    }
}
