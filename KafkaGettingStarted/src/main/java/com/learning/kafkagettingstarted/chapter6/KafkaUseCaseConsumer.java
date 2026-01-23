/*
 * CHAPTER 6: KAFKA USE CASE CONSUMER - EMAIL SERVICE EXAMPLE
 * 
 * Continuing our student enrollment scenario from the producer:
 * This consumer represents the EMAIL SERVICE that automatically sends
 * welcome emails to newly enrolled students.
 * 
 * Real-world scenario:
 * 1. Student enrolls online (producer sends message)
 * 2. Email service (this consumer) receives the enrollment event
 * 3. Email service extracts student info and sends welcome email
 * 4. Process repeats for every new enrollment
 * 
 * This demonstrates:
 * - How different services can react to the same event
 * - Loose coupling between systems (enrollment system doesn't know about email service)
 * - Easy to add new services without changing existing code
 * - Each service can have its own processing logic and speed
 */

package com.learning.kafkagettingstarted.chapter6;

// Same imports as Chapter 5 consumer, but used for a specific business purpose
import org.apache.kafka.clients.consumer.ConsumerConfig;    // Consumer settings
import org.apache.kafka.clients.consumer.ConsumerRecord;    // Individual message received
import org.apache.kafka.clients.consumer.ConsumerRecords;   // Batch of messages
import org.apache.kafka.clients.consumer.KafkaConsumer;     // Tool to receive messages

import java.time.Duration;     // Time specification for polling
import java.util.Arrays;       // To create topic list
import java.util.Properties;   // Configuration holder

/**
 * KafkaUseCaseConsumer - Email Service for Student Enrollment
 * 
 * Think of this as the "Welcome Email Robot" that:
 * 1. Monitors the student enrollment topic 24/7
 * 2. When it sees a new enrollment, it immediately processes it
 * 3. Sends a personalized welcome email to the student
 * 4. Logs the activity for tracking and debugging
 * 
 * In a real system, this might:
 * - Extract student email from the message
 * - Load email template from database
 * - Personalize the email with student name and course info
 * - Send via SendGrid, AWS SES, or similar email service
 * - Track email delivery status
 * - Handle bounced emails and retries
 */
public class KafkaUseCaseConsumer {
    public static void main(String[] args) {

        // STEP 1: Configure the Email Service Consumer
        Properties kafkaProps = new Properties();

        // Connect to the same Kafka broker as the enrollment system
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");

        // Set up deserializers (convert bytes back to readable text)
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

        // STEP 2: Set Consumer Group ID
        // This identifies our email service - if we run multiple instances,
        // they'll work together as a team to process emails faster
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG,
                "email-service-consumer-group");

        // STEP 3: Configure message reading behavior
        // "earliest" means if this is the first time running, process all existing enrollments
        // This ensures no student misses their welcome email!
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");

        // STEP 4: Create the Email Service Consumer
        KafkaConsumer<String, String> emailServiceConsumer =
                new KafkaConsumer<>(kafkaProps);

        // STEP 5: Subscribe to student enrollment events
        // We're specifically listening to the "kafka.usecase.students" topic
        // where the enrollment system publishes new student registrations
        emailServiceConsumer.subscribe(Arrays.asList("kafka.usecase.students"));

        System.out.println("📧 Email Service Started!");
        System.out.println("👀 Monitoring for new student enrollments...");
        System.out.println("🎓 Ready to send welcome emails!");
        System.out.println("Press Ctrl+C to stop the email service");

        // STEP 6: Continuously monitor for new enrollments
        // This runs forever, like a dedicated email worker who never sleeps
        while(true) {

            // STEP 7: Check for new enrollment messages
            // Poll every 100ms - fast enough to be responsive, not so fast as to waste CPU
            ConsumerRecords<String, String> enrollmentEvents =
                    emailServiceConsumer.poll(Duration.ofMillis(100));

            // STEP 8: Process each enrollment event
            for (ConsumerRecord<String, String> enrollmentEvent : enrollmentEvents) {
                
                // Extract enrollment information
                String studentId = enrollmentEvent.key();           // Student ID
                String enrollmentDetails = enrollmentEvent.value(); // Full enrollment info
                long eventTimestamp = enrollmentEvent.timestamp();  // When enrollment happened
                int partition = enrollmentEvent.partition();        // Which partition (for tracking)
                long offset = enrollmentEvent.offset();            // Message position

                // STEP 9: Process the enrollment and send welcome email
                System.out.println("📬 New Student Enrollment Detected!");
                System.out.println("  👤 Student ID: " + studentId);
                System.out.println("  📝 Details: " + enrollmentDetails);
                System.out.println("  ⏰ Enrolled at: " + new java.util.Date(eventTimestamp));
                System.out.println("  📊 Message Info: Partition=" + partition + ", Offset=" + offset);
                
                // Simulate email processing
                System.out.println("  📧 Sending welcome email to student " + studentId + "...");
                
                try {
                    // Simulate email sending time (database lookup, template rendering, SMTP sending)
                    Thread.sleep(500); // 500ms = realistic email processing time
                    
                    System.out.println("  ✅ Welcome email sent successfully to student " + studentId);
                    
                    /*
                     * In a real email service, this is where you would:
                     * 
                     * 1. Parse the enrollment details (JSON -> Student object)
                     * 2. Extract student email address
                     * 3. Look up email template from database
                     * 4. Personalize template with student name, courses, etc.
                     * 5. Send email via email service (SendGrid, AWS SES, etc.)
                     * 6. Log email delivery attempt
                     * 7. Handle any delivery failures with retries
                     * 8. Update email delivery status in database
                     */
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("  ❌ Email processing interrupted for student " + studentId);
                    break; // Exit the processing loop
                }
                
                System.out.println("  ─────────────────────────────────");
            }

            // After processing all messages in this batch, the loop continues
            // to check for new enrollments. This provides real-time email delivery!
        }

        // NOTE: This line never executes due to infinite loop
        // In production, you'd add graceful shutdown handling
        // emailServiceConsumer.close();

        /*
         * REAL-WORLD EXTENSIONS:
         * 
         * 1. MULTIPLE EMAIL TYPES:
         *    - Welcome emails for new students
         *    - Course confirmation emails
         *    - Payment reminder emails
         *    - Grade notification emails
         * 
         * 2. ERROR HANDLING:
         *    - Retry failed email sends
         *    - Dead letter queue for permanent failures
         *    - Alert administrators of email service issues
         * 
         * 3. SCALING:
         *    - Run multiple instances for high enrollment periods
         *    - Each instance processes different partitions
         *    - Load balancing happens automatically
         * 
         * 4. MONITORING:
         *    - Track email delivery rates
         *    - Monitor processing latency
         *    - Alert on service health issues
         * 
         * 5. INTEGRATION:
         *    - Connect to CRM systems
         *    - Integrate with student information systems
         *    - Link to learning management systems
         */
    }
}
