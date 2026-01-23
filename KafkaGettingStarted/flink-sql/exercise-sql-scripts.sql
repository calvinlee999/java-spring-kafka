-- =====================================================
-- FLINK SQL EXERCISE SCRIPTS
-- Based on Confluent's "Apache Flink 101" course
-- Exercise: Batch and Stream Processing with Flink SQL
-- =====================================================

-- 1. CREATE BOUNDED TABLE FOR BATCH PROCESSING
-- This creates a table with exactly 500 rows using the faker connector
CREATE TABLE `bounded_pageviews` (
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
);

-- 2. CREATE UNBOUNDED TABLE FOR STREAM PROCESSING  
-- This creates a continuous stream of data (no row limit)
CREATE TABLE `pageviews` (
  `url` STRING,
  `user_id` STRING,
  `browser` STRING,
  `ts` TIMESTAMP(3)
)
WITH (
  'connector' = 'faker',
  'rows-per-second' = '100',
  'fields.url.expression' = '/#{GreekPhilosopher.name}.html',
  'fields.user_id.expression' = '#{numerify ''user_##''}',
  'fields.browser.expression' = '#{Options.option ''chrome'', ''firefox'', ''safari''}',
  'fields.ts.expression' = '#{date.past ''5'',''1'',''SECONDS''}'
);

-- =====================================================
-- EXERCISE COMMANDS (Execute these interactively)
-- =====================================================

-- Set execution mode to BATCH
-- SET 'execution.runtime-mode' = 'batch';

-- Set execution mode to STREAMING  
-- SET 'execution.runtime-mode' = 'streaming';

-- Set result display mode to TABLE (default)
-- SET 'sql-client.execution.result-mode' = 'table';

-- Set result display mode to CHANGELOG (shows all updates)
-- SET 'sql-client.execution.result-mode' = 'changelog';

-- =====================================================
-- SAMPLE QUERIES FOR TESTING
-- =====================================================

-- View sample data from bounded table
-- SELECT * FROM bounded_pageviews LIMIT 10;

-- Count all records in bounded table
-- SELECT count(*) AS `count` FROM bounded_pageviews;

-- Count all records in unbounded table (streaming)
-- SELECT count(*) AS `count` FROM pageviews;

-- Group by browser and count
-- SELECT browser, count(*) as page_count 
-- FROM bounded_pageviews 
-- GROUP BY browser;

-- Time-based analysis
-- SELECT 
--   TUMBLE_START(ts, INTERVAL '1' MINUTE) as window_start,
--   TUMBLE_END(ts, INTERVAL '1' MINUTE) as window_end,
--   browser,
--   count(*) as page_count
-- FROM pageviews
-- GROUP BY 
--   TUMBLE(ts, INTERVAL '1' MINUTE),
--   browser;

-- =====================================================
-- KAFKA INTEGRATION TABLES
-- =====================================================

-- Create table connected to existing Kafka topic
CREATE TABLE kafka_orders (
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
);

-- Create table for processed results
CREATE TABLE kafka_order_summary (
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
);

-- =====================================================
-- ADVANCED STREAMING QUERIES
-- =====================================================

-- Real-time order aggregation by customer
-- INSERT INTO kafka_order_summary
-- SELECT 
--   customer_id,
--   COUNT(*) as order_count,
--   SUM(amount) as total_amount,
--   TUMBLE_START(event_time, INTERVAL '1' MINUTE) as window_start,
--   TUMBLE_END(event_time, INTERVAL '1' MINUTE) as window_end
-- FROM kafka_orders
-- GROUP BY 
--   customer_id,
--   TUMBLE(event_time, INTERVAL '1' MINUTE);

-- =====================================================
-- UTILITY COMMANDS
-- =====================================================

-- Show all tables
-- SHOW TABLES;

-- Describe table structure  
-- DESCRIBE bounded_pageviews;

-- Show running jobs
-- SHOW JOBS;

-- Reset configuration
-- RESET;

-- Exit SQL Client
-- QUIT;
