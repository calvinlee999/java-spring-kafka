-- =====================================================
-- CONFLUENT APACHE FLINK 101 COURSE EXERCISES
-- Exercise 4: Batch and Stream Processing with Flink SQL
-- https://developer.confluent.io/courses/apache-flink/stream-processing-exercise/
-- =====================================================

-- Note: These are the EXACT commands from the Confluent course
-- Execute these commands one by one in the Flink SQL Client

-- =====================================================
-- 1. CREATE BOUNDED TABLE (Exactly as in the course)
-- =====================================================

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
  'fields.ts.expression' =  '#{date.past ''5'',''1'',''SECONDS''}'
);

-- =====================================================
-- 2. CREATE UNBOUNDED TABLE (Exactly as in the course)
-- =====================================================

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
  'fields.ts.expression' =  '#{date.past ''5'',''1'',''SECONDS''}'
);

-- =====================================================
-- COURSE EXERCISE WORKFLOW
-- =====================================================

-- Step 1: Examine the data
-- select * from bounded_pageviews limit 10;

-- Step 2: Run in BATCH mode with bounded input
-- SET 'execution.runtime-mode' = 'batch';
-- select count(*) AS `count` from bounded_pageviews;

-- Step 3: Run in STREAMING mode with bounded input
-- SET 'execution.runtime-mode' = 'streaming';
-- select count(*) AS `count` from bounded_pageviews;

-- Step 4: Switch to CHANGELOG mode to see updates
-- SET 'sql-client.execution.result-mode' = 'changelog';
-- select count(*) AS `count` from bounded_pageviews;

-- Step 5: Run with unbounded input
-- SET 'sql-client.execution.result-mode' = 'table';
-- select count(*) AS `count` from pageviews;

-- Step 6: Modify table rate (optional)
-- ALTER TABLE `pageviews` SET ('rows-per-second' = '10');

-- =====================================================
-- ADDITIONAL COURSE-ALIGNED EXERCISES
-- =====================================================

-- Browser analytics (streaming)
-- select browser, count(*) as page_views
-- from pageviews
-- group by browser;

-- Popular pages (streaming with time window)
-- select 
--   url,
--   count(*) as views,
--   TUMBLE_START(ts, INTERVAL '10' SECOND) as window_start
-- from pageviews
-- group by url, TUMBLE(ts, INTERVAL '10' SECOND);

-- User activity tracking
-- select 
--   user_id,
--   count(distinct url) as unique_pages,
--   count(*) as total_views
-- from pageviews
-- group by user_id;

-- =====================================================
-- HELPFUL COMMANDS FOR THE COURSE
-- =====================================================

-- Reset to default streaming mode:
-- SET 'execution.runtime-mode' = 'streaming';

-- Reset to table display mode:
-- SET 'sql-client.execution.result-mode' = 'table';

-- Show current settings:
-- SET;

-- Show available tables:
-- SHOW TABLES;

-- Describe table structure:
-- DESCRIBE bounded_pageviews;

-- Exit SQL Client:
-- quit;

-- =====================================================
-- COURSE NOTES
-- =====================================================
-- 
-- Key differences between batch and streaming:
-- 1. Batch mode waits for complete input before producing final result
-- 2. Streaming mode produces incremental updates as data arrives
-- 3. Changelog mode shows all update operations (+I, -U, +U)
-- 4. Table mode shows only the current state
--
-- Expected behavior:
-- - Bounded table with 500 rows takes 5 seconds (100 rows/sec)
-- - Count should reach 500 in both batch and streaming modes
-- - Streaming shows incremental updates (100, 200, 300, 400, 500)
-- - Batch shows only final result (500)
