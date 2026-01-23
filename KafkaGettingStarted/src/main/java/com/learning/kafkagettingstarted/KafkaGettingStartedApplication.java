/*
 * KAFKA GETTING STARTED APPLICATION - MAIN ENTRY POINT
 * 
 * What is Spring Boot?
 * Think of Spring Boot like a pre-built house framework that has:
 * - Electricity already wired (web server ready)
 * - Plumbing already installed (database connections ready)
 * - Foundation already laid (security, logging, etc.)
 * 
 * Instead of building everything from scratch, we just move in and add our furniture (our Kafka code)!
 * 
 * This is the MAIN application that starts everything up.
 * It's like the power button that turns on your entire Kafka learning environment.
 */

package com.learning.kafkagettingstarted;

import org.slf4j.Logger;                                    // Proper logging instead of System.out
import org.slf4j.LoggerFactory;                             // Logger factory
// Import the Spring Boot tools we need
import org.springframework.boot.SpringApplication;           // The engine that starts our app
import org.springframework.boot.autoconfigure.SpringBootApplication;  // Magic annotation that sets up everything

/**
 * @SpringBootApplication - This is a MAGIC annotation! 🎩✨
 * 
 * What it does for us automatically:
 * 1. @Configuration - Says "this class has settings for the app"
 * 2. @EnableAutoConfiguration - Says "Spring, please set up everything automatically"
 * 3. @ComponentScan - Says "Spring, please find all the other classes in this project"
 * 
 * It's like hiring a super smart assistant who knows how to set up your entire office
 * just by you saying "please get everything ready!"
 */
@SpringBootApplication
public class KafkaGettingStartedApplication {

    private static final Logger logger = LoggerFactory.getLogger(KafkaGettingStartedApplication.class);

    /**
     * MAIN METHOD - The starting point of our entire application
     * 
     * Think of this like the ignition key in a car:
     * - You turn the key (run this method)
     * - The engine starts (Spring Boot starts)
     * - All the car systems come online (web server, Kafka connections, etc.)
     * - The car is ready to drive (your app is ready to handle requests)
     * 
     * @param args - Command line arguments (like giving instructions when starting the car)
     */
    public static void main(String[] args) {
        // This single line of code does TONS of work behind the scenes:
        // 1. Starts a web server (usually on port 8080)
        // 2. Scans for all our classes (controllers, services, configs)
        // 3. Sets up Kafka connections based on our properties
        // 4. Prepares the application to handle web requests
        // 5. Makes everything ready for us to use
        
        logger.info("🚀 Starting Kafka Learning Application...");
        logger.info("📚 This app will help you learn Apache Kafka!");
        logger.info("🌐 Once started, you can access the web interface at: http://localhost:8080");
        
        SpringApplication.run(KafkaGettingStartedApplication.class, args);
        
        logger.info("✅ Application started successfully!");
        logger.info("💡 Try accessing: http://localhost:8080/kafka/send/hello");
    }
}
