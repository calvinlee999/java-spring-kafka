/*
 * BASIC FLINK KAFKA STREAMING APPLICATION
 * 
 * Based on Confluent's "Building Apache Flink Applications in Java" course
 * 
 * This application demonstrates:
 * 1. Consuming messages from a Kafka topic
 * 2. Processing/transforming the data
 * 3. Writing results back to another Kafka topic
 * 
 * This follows the course pattern of iterative learning:
 * - Start simple with basic consume/produce
 * - Add transformations
 * - Add windowing and state management
 */

package com.learning.kafkagettingstarted.flink;

// Flink imports
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

// Kafka imports
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;

// Java imports
import java.util.Properties;

/**
 * Basic Flink Streaming Job for Kafka Integration
 * 
 * This is your first Flink job following the course methodology:
 * - Read from Kafka topic
 * - Transform the data 
 * - Write to another Kafka topic
 * 
 * Course Learning Objectives Covered:
 * ✅ Run a Flink job
 * ✅ Consume data from a Kafka topic
 * ✅ Transform data using Flink operators
 * ✅ Produce data to a Kafka topic
 */
public class BasicKafkaFlinkJob {

    // Configuration constants (following course best practices)
    private static final String KAFKA_BROKERS = "localhost:9092";
    private static final String INPUT_TOPIC = "kafka.learning.orders";
    private static final String OUTPUT_TOPIC = "kafka.learning.processed-orders";
    private static final String CONSUMER_GROUP = "flink-kafka-learning-group";

    public static void main(String[] args) throws Exception {
        
        /*
         * STEP 1: SET UP FLINK EXECUTION ENVIRONMENT
         * 
         * The StreamExecutionEnvironment is like your workspace in Flink.
         * It's where you define your data processing pipeline.
         */
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Set parallelism (how many parallel tasks to run)
        // Course recommendation: Start with 1 for learning, then increase
        env.setParallelism(1);
        
        /*
         * STEP 2: CREATE KAFKA SOURCE (Data Input)
         * 
         * This follows the course pattern for creating Flink data sources.
         * KafkaSource is how Flink reads from Kafka topics.
         */
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
            .setBootstrapServers(KAFKA_BROKERS)           // Where to find Kafka
            .setTopics(INPUT_TOPIC)                       // Which topic to read from
            .setGroupId(CONSUMER_GROUP)                   // Consumer group ID
            .setStartingOffsets(OffsetsInitializer.earliest())  // Start from beginning
            .setValueOnlyDeserializer(new SimpleStringSchema())  // How to deserialize messages
            .build();

        /*
         * STEP 3: CREATE DATA STREAM FROM KAFKA SOURCE
         * 
         * This creates a DataStream - the fundamental abstraction in Flink.
         * Think of it as a continuous flow of data that you can transform.
         */
        DataStream<String> orderStream = env
            .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Orders Source");

        /*
         * STEP 4: TRANSFORM THE DATA
         * 
         * This is where the course focuses on data transformations.
         * We'll apply various operations to process the streaming data.
         */
        DataStream<String> processedStream = orderStream
            .process(new OrderProcessFunction())  // Custom processing function
            .name("Order Processing");            // Name for monitoring/debugging

        /*
         * STEP 5: CREATE KAFKA SINK (Data Output)
         * 
         * KafkaSink is how Flink writes data back to Kafka topics.
         * This completes the streaming pipeline.
         */
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
            .setBootstrapServers(KAFKA_BROKERS)
            .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic(OUTPUT_TOPIC)
                .setValueSerializationSchema(new SimpleStringSchema())
                .build())
            .setDeliveryGuarantee(org.apache.flink.connector.base.DeliveryGuarantee.AT_LEAST_ONCE)
            .build();

        /*
         * STEP 6: CONNECT PROCESSED STREAM TO SINK
         * 
         * This completes the pipeline: Kafka → Flink → Kafka
         */
        processedStream.sinkTo(kafkaSink).name("Kafka Orders Sink");

        /*
         * STEP 7: EXECUTE THE JOB
         * 
         * This actually starts the Flink job. Until you call execute(),
         * you're just building the pipeline definition.
         */
        env.execute("Basic Kafka Flink Learning Job");
    }

    /**
     * CUSTOM PROCESSING FUNCTION
     * 
     * This demonstrates the course concept of custom transformations.
     * ProcessFunction is one of the most flexible transformation operators.
     */
    public static class OrderProcessFunction extends ProcessFunction<String, String> {
        
        @Override
        public void processElement(String orderMessage, Context context, Collector<String> out) {
            try {
                /*
                 * COURSE LEARNING: Data Transformation Patterns
                 * 
                 * Here we demonstrate common streaming transformations:
                 * - Parsing incoming data
                 * - Enriching with additional information
                 * - Filtering based on conditions
                 * - Formatting output
                 */
                
                // Add processing timestamp (enrichment)
                long processingTime = System.currentTimeMillis();
                
                // Create processed message with additional metadata
                String processedMessage = String.format(
                    "PROCESSED[%d]: %s", 
                    processingTime, 
                    orderMessage
                );
                
                // Filter out empty or null messages (data quality)
                if (orderMessage != null && !orderMessage.trim().isEmpty()) {
                    // Emit the processed message
                    out.collect(processedMessage);
                    
                    // Course pattern: Logging for learning and debugging
                    System.out.printf("Processed order: %s%n", processedMessage);
                }
                
            } catch (Exception e) {
                // Course best practice: Error handling in streaming applications
                System.err.printf("Error processing message: %s, Error: %s%n", 
                    orderMessage, e.getMessage());
            }
        }
    }
}

/*
 * COURSE LEARNING NOTES:
 * 
 * This application covers several key concepts from the course:
 * 
 * 1. **Datastream Programming**: Using DataStream API for processing
 * 2. **Flink Data Sources**: KafkaSource for consuming from Kafka
 * 3. **Serializers & Deserializers**: SimpleStringSchema for text data
 * 4. **Transforming Data**: ProcessFunction for custom logic
 * 5. **Flink Data Sinks**: KafkaSink for producing to Kafka
 * 
 * NEXT STEPS (following course progression):
 * - Add windowing for time-based aggregations
 * - Implement keyed state for stateful processing
 * - Add complex event processing patterns
 * - Integrate with multiple data sources
 * 
 * TO RUN THIS JOB:
 * 1. Start your Kafka cluster: docker-compose -f kafka-single-node.yml up
 * 2. Start Flink cluster: docker-compose -f docker-compose.flink.yml up
 * 3. Build JAR: mvn clean package
 * 4. Submit job via Flink Web UI (http://localhost:8082)
 */
