/*
 * FLINK SQL JOB RUNNER
 * 
 * This application demonstrates how to execute Flink SQL scripts programmatically
 * Based on the Confluent course exercise: "Batch and Stream Processing with Flink SQL"
 * 
 * Features:
 * - Execute SQL scripts from files
 * - Switch between batch and streaming modes
 * - Create tables and run queries programmatically
 * - Integrate with Kafka topics
 */

package com.learning.kafkagettingstarted.flink;

// Flink Table API imports
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.configuration.RestOptions;

// Flink streaming imports
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Java imports
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Flink SQL Job Runner - Execute SQL scripts following course exercises
 * 
 * Course Learning Objectives:
 * ✅ Execute Flink SQL in both batch and streaming modes
 * ✅ Create tables using faker connector  
 * ✅ Run aggregation queries
 * ✅ Integrate with Kafka topics
 * ✅ Switch execution modes programmatically
 */
public class FlinkSqlJobRunner {

    private static final Logger logger = LoggerFactory.getLogger(FlinkSqlJobRunner.class);
    
    // Configuration
    private static final String FLINK_JOBMANAGER_HOST = "localhost";
    private static final int FLINK_JOBMANAGER_PORT = 8082;
    private static final String SQL_SCRIPTS_PATH = "./flink-sql/";

    public static void main(String[] args) throws Exception {
        
        // Parse command line arguments
        String mode = args.length > 0 ? args[0] : "streaming"; // batch or streaming
        String operation = args.length > 1 ? args[1] : "pageviews"; // pageviews, kafka, or custom
        
        logger.info("Starting Flink SQL Job Runner - Mode: {}, Operation: {}", mode, operation);
        
        // Create table environment based on mode
        TableEnvironment tableEnv = createTableEnvironment(mode);
        
        // Execute based on operation type
        switch (operation.toLowerCase()) {
            case "pageviews":
                runPageviewsExercise(tableEnv, mode);
                break;
            case "kafka":
                runKafkaIntegrationExample(tableEnv);
                break;
            case "script":
                String scriptFile = args.length > 2 ? args[2] : "exercise-sql-scripts.sql";
                runSqlScript(tableEnv, scriptFile);
                break;
            default:
                logger.error("Unknown operation: {}. Use: pageviews, kafka, or script", operation);
                System.exit(1);
        }
    }

    /**
     * Create Table Environment based on execution mode
     */
    private static TableEnvironment createTableEnvironment(String mode) {
        
        // Configuration for connecting to remote Flink cluster
        Configuration config = new Configuration();
        config.setString(RestOptions.ADDRESS, FLINK_JOBMANAGER_HOST);
        config.setInteger(RestOptions.PORT, FLINK_JOBMANAGER_PORT);
        
        // Set execution mode
        if ("batch".equalsIgnoreCase(mode)) {
            config.setString("execution.runtime-mode", "BATCH");
            logger.info("Configured for BATCH execution mode");
        } else {
            config.setString("execution.runtime-mode", "STREAMING");
            logger.info("Configured for STREAMING execution mode");
        }
        
        // Create environment settings
        EnvironmentSettings settings;
        if ("batch".equalsIgnoreCase(mode)) {
            settings = EnvironmentSettings.newInstance()
                .inBatchMode()
                .withConfiguration(config)
                .build();
        } else {
            settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .withConfiguration(config)
                .build();
        }
        
        return TableEnvironment.create(settings);
    }

    /**
     * Run the pageviews exercise from the course
     */
    private static void runPageviewsExercise(TableEnvironment tableEnv, String mode) {
        logger.info("Running Pageviews Exercise in {} mode", mode);
        
        try {
            // Create bounded table for batch processing
            String createBoundedTable = """
                CREATE TABLE IF NOT EXISTS `bounded_pageviews` (
                  `url` STRING,
                  `user_id` STRING,
                  `browser` STRING,
                  `ts` TIMESTAMP(3)
                )
                WITH (
                  'connector' = 'faker',
                  'number-of-rows' = '500',
                  'rows-per-second' = '100',
                  'fields.url.expression' = '/#{GreekPhilosopher.name}.html',
                  'fields.user_id.expression' = '#{numerify ''user_##''}',
                  'fields.browser.expression' = '#{Options.option ''chrome'', ''firefox'', ''safari''}',
                  'fields.ts.expression' = '#{date.past ''5'',''1'',''SECONDS''}'
                )
                """;
            
            tableEnv.executeSql(createBoundedTable);
            logger.info("Created bounded_pageviews table");
            
            if ("streaming".equalsIgnoreCase(mode)) {
                // Create unbounded table for streaming
                String createUnboundedTable = """
                    CREATE TABLE IF NOT EXISTS `pageviews` (
                      `url` STRING,
                      `user_id` STRING,
                      `browser` STRING,
                      `ts` TIMESTAMP(3)
                    )
                    WITH (
                      'connector' = 'faker',
                      'rows-per-second' = '10',
                      'fields.url.expression' = '/#{GreekPhilosopher.name}.html',
                      'fields.user_id.expression' = '#{numerify ''user_##''}',
                      'fields.browser.expression' = '#{Options.option ''chrome'', ''firefox'', ''safari''}',
                      'fields.ts.expression' = '#{date.past ''5'',''1'',''SECONDS''}'
                    )
                    """;
                
                tableEnv.executeSql(createUnboundedTable);
                logger.info("Created pageviews table for streaming");
            }
            
            // Execute sample queries
            executeSampleQueries(tableEnv, mode);
            
        } catch (Exception e) {
            logger.error("Error in pageviews exercise", e);
        }
    }

    /**
     * Execute sample queries based on the course exercise
     */
    private static void executeSampleQueries(TableEnvironment tableEnv, String mode) {
        
        if ("batch".equalsIgnoreCase(mode)) {
            // Batch mode queries
            logger.info("Executing batch queries...");
            
            // Sample data query
            TableResult sampleResult = tableEnv.executeSql(
                "SELECT * FROM bounded_pageviews LIMIT 10"
            );
            logger.info("Sample data from bounded_pageviews:");
            sampleResult.print();
            
            // Count query
            TableResult countResult = tableEnv.executeSql(
                "SELECT count(*) AS `count` FROM bounded_pageviews"
            );
            logger.info("Total count from bounded_pageviews:");
            countResult.print();
            
            // Group by browser
            TableResult groupResult = tableEnv.executeSql(
                "SELECT browser, count(*) as page_count FROM bounded_pageviews GROUP BY browser"
            );
            logger.info("Page count by browser:");
            groupResult.print();
            
        } else {
            // Streaming mode queries
            logger.info("Executing streaming queries (will run for 30 seconds)...");
            
            // Create a temporary view for the count query
            tableEnv.executeSql(
                "CREATE TEMPORARY VIEW streaming_count AS " +
                "SELECT count(*) AS `count` FROM pageviews"
            );
            
            // Execute streaming count (this will run continuously)
            TableResult streamingResult = tableEnv.executeSql(
                "SELECT * FROM streaming_count"
            );
            
            logger.info("Streaming count (observe the updates):");
            // In a real application, you might want to limit execution time
            streamingResult.print();
        }
    }

    /**
     * Run Kafka integration example
     */
    private static void runKafkaIntegrationExample(TableEnvironment tableEnv) {
        logger.info("Running Kafka Integration Example");
        
        try {
            // Create Kafka source table
            String createKafkaSource = """
                CREATE TABLE IF NOT EXISTS kafka_orders (
                  `order_id` STRING,
                  `customer_id` STRING,
                  `amount` DECIMAL(10,2),
                  `status` STRING,
                  `event_time` TIMESTAMP(3) METADATA FROM 'timestamp'
                ) WITH (
                  'connector' = 'kafka',
                  'topic' = 'kafka.learning.orders',
                  'properties.bootstrap.servers' = 'kafka:29092',
                  'properties.group.id' = 'flink-sql-orders-group',
                  'format' = 'json',
                  'scan.startup.mode' = 'earliest-offset'
                )
                """;
            
            tableEnv.executeSql(createKafkaSource);
            logger.info("Created Kafka orders source table");
            
            // Create Kafka sink table
            String createKafkaSink = """
                CREATE TABLE IF NOT EXISTS kafka_order_summary (
                  `customer_id` STRING,
                  `order_count` BIGINT,
                  `total_amount` DECIMAL(10,2),
                  `window_start` TIMESTAMP(3),
                  `window_end` TIMESTAMP(3)
                ) WITH (
                  'connector' = 'kafka',
                  'topic' = 'kafka.learning.order-summary',
                  'properties.bootstrap.servers' = 'kafka:29092',
                  'format' = 'json'
                )
                """;
            
            tableEnv.executeSql(createKafkaSink);
            logger.info("Created Kafka order summary sink table");
            
            // Show available data
            TableResult ordersResult = tableEnv.executeSql(
                "SELECT * FROM kafka_orders LIMIT 10"
            );
            logger.info("Sample orders from Kafka:");
            ordersResult.print();
            
        } catch (Exception e) {
            logger.error("Error in Kafka integration example", e);
        }
    }

    /**
     * Execute SQL script from file
     */
    private static void runSqlScript(TableEnvironment tableEnv, String scriptFile) {
        logger.info("Executing SQL script: {}", scriptFile);
        
        try {
            String scriptPath = SQL_SCRIPTS_PATH + scriptFile;
            String sqlContent = Files.readString(Path.of(scriptPath));
            
            // Split by semicolon and execute each statement
            List<String> statements = Arrays.stream(sqlContent.split(";"))
                .map(String::trim)
                .filter(stmt -> !stmt.isEmpty() && !stmt.startsWith("--"))
                .toList();
            
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        logger.info("Executing: {}", statement.substring(0, Math.min(50, statement.length())) + "...");
                        TableResult result = tableEnv.executeSql(statement);
                        
                        // Print results for SELECT statements
                        if (statement.trim().toUpperCase().startsWith("SELECT")) {
                            result.print();
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to execute statement: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error executing SQL script", e);
        }
    }
}

/*
 * USAGE EXAMPLES:
 * 
 * 1. Run pageviews exercise in batch mode:
 *    java FlinkSqlJobRunner batch pageviews
 * 
 * 2. Run pageviews exercise in streaming mode:
 *    java FlinkSqlJobRunner streaming pageviews
 * 
 * 3. Run Kafka integration example:
 *    java FlinkSqlJobRunner streaming kafka
 * 
 * 4. Execute custom SQL script:
 *    java FlinkSqlJobRunner streaming script my-script.sql
 * 
 * COURSE LEARNING OBJECTIVES COVERED:
 * ✅ Batch vs Streaming execution modes
 * ✅ Creating tables with faker connector
 * ✅ Running aggregation queries
 * ✅ Kafka topic integration
 * ✅ Programmatic SQL execution
 */
