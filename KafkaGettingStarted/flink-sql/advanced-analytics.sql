-- =====================================================
-- PRACTICAL FLINK SQL EXAMPLES
-- Advanced exercises beyond the basic course content
-- =====================================================

-- Set initial configuration for streaming mode
SET 'execution.runtime-mode' = 'streaming';
SET 'sql-client.execution.result-mode' = 'table';

-- =====================================================
-- KAFKA-INTEGRATED REAL-TIME ANALYTICS
-- =====================================================

-- Create Kafka source for order events
CREATE TABLE real_orders (
  order_id STRING,
  customer_id STRING,
  product_id STRING,
  amount DECIMAL(10,2),
  order_status STRING,
  order_time TIMESTAMP(3) METADATA FROM 'timestamp',
  WATERMARK FOR order_time AS order_time - INTERVAL '5' SECOND
) WITH (
  'connector' = 'kafka',
  'topic' = 'kafka.learning.orders',
  'properties.bootstrap.servers' = 'kafka:29092',
  'properties.group.id' = 'flink-analytics-group',
  'format' = 'json',
  'scan.startup.mode' = 'latest-offset'
);

-- Create output table for real-time analytics
CREATE TABLE order_analytics (
  window_start TIMESTAMP(3),
  window_end TIMESTAMP(3),
  customer_id STRING,
  total_orders BIGINT,
  total_amount DECIMAL(10,2),
  avg_amount DECIMAL(10,2),
  max_amount DECIMAL(10,2),
  PRIMARY KEY (window_start, customer_id) NOT ENFORCED
) WITH (
  'connector' = 'kafka',
  'topic' = 'kafka.learning.analytics',
  'properties.bootstrap.servers' = 'kafka:29092',
  'format' = 'json'
);

-- Real-time windowed aggregation
INSERT INTO order_analytics
SELECT 
  TUMBLE_START(order_time, INTERVAL '1' MINUTE) as window_start,
  TUMBLE_END(order_time, INTERVAL '1' MINUTE) as window_end,
  customer_id,
  COUNT(*) as total_orders,
  SUM(amount) as total_amount,
  AVG(amount) as avg_amount,
  MAX(amount) as max_amount
FROM real_orders
WHERE order_status IN ('confirmed', 'shipped')
GROUP BY 
  customer_id,
  TUMBLE(order_time, INTERVAL '1' MINUTE);

-- =====================================================
-- COMPLEX EVENT PROCESSING PATTERNS
-- =====================================================

-- Detect customers with rapid order sequences
CREATE TEMPORARY VIEW rapid_orders AS
SELECT 
  customer_id,
  order_id,
  amount,
  order_time,
  LAG(order_time) OVER (
    PARTITION BY customer_id 
    ORDER BY order_time
  ) as prev_order_time
FROM real_orders;

-- Find customers placing orders within 30 seconds
SELECT 
  customer_id,
  COUNT(*) as rapid_order_count,
  SUM(amount) as total_rapid_amount
FROM rapid_orders
WHERE order_time - prev_order_time < INTERVAL '30' SECOND
GROUP BY customer_id
HAVING COUNT(*) > 1;

-- =====================================================
-- FRAUD DETECTION EXAMPLE
-- =====================================================

-- Create temporary view for fraud detection
CREATE TEMPORARY VIEW suspicious_activity AS
SELECT 
  customer_id,
  order_time,
  amount,
  COUNT(*) OVER (
    PARTITION BY customer_id 
    ORDER BY order_time 
    RANGE BETWEEN INTERVAL '5' MINUTE PRECEDING AND CURRENT ROW
  ) as orders_in_5min,
  SUM(amount) OVER (
    PARTITION BY customer_id 
    ORDER BY order_time 
    RANGE BETWEEN INTERVAL '5' MINUTE PRECEDING AND CURRENT ROW
  ) as amount_in_5min
FROM real_orders;

-- Flag potentially fraudulent activity
SELECT 
  customer_id,
  order_time,
  amount,
  orders_in_5min,
  amount_in_5min,
  'HIGH_FREQUENCY' as fraud_reason
FROM suspicious_activity
WHERE orders_in_5min > 5 OR amount_in_5min > 1000;

-- =====================================================
-- CUSTOMER SEGMENTATION
-- =====================================================

-- Create customer segments based on order patterns
CREATE TEMPORARY VIEW customer_segments AS
SELECT 
  customer_id,
  COUNT(*) as total_orders,
  SUM(amount) as total_spent,
  AVG(amount) as avg_order_value,
  MAX(order_time) as last_order_time,
  CASE 
    WHEN SUM(amount) > 1000 AND COUNT(*) > 10 THEN 'VIP'
    WHEN SUM(amount) > 500 AND COUNT(*) > 5 THEN 'PREMIUM'
    WHEN COUNT(*) > 3 THEN 'REGULAR'
    ELSE 'NEW'
  END as segment
FROM real_orders
GROUP BY customer_id;

-- =====================================================
-- TIME-BASED ANALYTICS
-- =====================================================

-- Hourly order trends
SELECT 
  HOUR(TUMBLE_START(order_time, INTERVAL '1' HOUR)) as hour_of_day,
  COUNT(*) as order_count,
  SUM(amount) as total_revenue,
  COUNT(DISTINCT customer_id) as unique_customers
FROM real_orders
GROUP BY TUMBLE(order_time, INTERVAL '1' HOUR);

-- Day-over-day comparison (using session windows)
CREATE TEMPORARY VIEW daily_metrics AS
SELECT 
  DATE_FORMAT(order_time, 'yyyy-MM-dd') as order_date,
  COUNT(*) as daily_orders,
  SUM(amount) as daily_revenue
FROM real_orders
GROUP BY DATE_FORMAT(order_time, 'yyyy-MM-dd');

-- =====================================================
-- ADVANCED AGGREGATIONS
-- =====================================================

-- Top products by revenue in sliding windows
SELECT 
  product_id,
  SUM(amount) as revenue,
  COUNT(*) as order_count,
  HOP_START(order_time, INTERVAL '5' MINUTE, INTERVAL '15' MINUTE) as window_start
FROM real_orders
GROUP BY 
  product_id,
  HOP(order_time, INTERVAL '5' MINUTE, INTERVAL '15' MINUTE)
ORDER BY revenue DESC;

-- Customer lifetime value calculation
SELECT 
  customer_id,
  FIRST_VALUE(order_time) as first_order,
  LAST_VALUE(order_time) as latest_order,
  COUNT(*) as lifetime_orders,
  SUM(amount) as lifetime_value,
  SUM(amount) / COUNT(*) as avg_order_value,
  EXTRACT(DAY FROM (MAX(order_time) - MIN(order_time))) as customer_age_days
FROM real_orders
GROUP BY customer_id;

-- =====================================================
-- UTILITY QUERIES FOR MONITORING
-- =====================================================

-- Show current table status
SHOW TABLES;

-- Check running jobs
SHOW JOBS;

-- Describe table schema
DESCRIBE real_orders;

-- Sample recent data
SELECT * FROM real_orders 
ORDER BY order_time DESC 
LIMIT 10;

-- Check Kafka topic lag (approximate)
SELECT 
  COUNT(*) as message_count,
  MAX(order_time) as latest_message_time,
  CURRENT_TIMESTAMP as current_time
FROM real_orders;
