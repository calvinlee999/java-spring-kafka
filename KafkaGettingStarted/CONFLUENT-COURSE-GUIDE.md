# 🎓 Confluent Apache Flink 101 Course Setup Guide

## Course Overview
This environment is configured for [Confluent's Apache Flink 101 course](https://developer.confluent.io/courses/apache-flink/), specifically **Exercise 4: Batch and Stream Processing with Flink SQL**.

## 🚀 Quick Start (Confluent Course)

### Option 1: One-Command Setup
```bash
# Start the complete course environment
./flink-setup.sh start-course

# Start interactive Flink SQL session for Exercise 4
./flink-setup.sh sql-course
```

### Option 2: VS Code Tasks
1. Open Command Palette (`Cmd+Shift+P`)
2. Type "Tasks: Run Task"
3. Select "🎓 Confluent Course: Start Environment"
4. Then select "🎓 Confluent Course: SQL Session (Exercise 4)"

## 🎯 Exercise 4: Batch and Stream Processing with Flink SQL

### Course Exercise Workflow

1. **Start the SQL Client:**
   ```bash
   ./flink-setup.sh sql-course
   ```

2. **Follow the exact course steps:** The SQL Client will display course instructions and you can execute the commands from the exercise:

   ```sql
   -- 1. Create bounded table (exactly as in course)
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

   -- 2. View sample data
   select * from bounded_pageviews limit 10;

   -- 3. Run in BATCH mode
   SET 'execution.runtime-mode' = 'batch';
   select count(*) AS `count` from bounded_pageviews;

   -- 4. Run in STREAMING mode  
   SET 'execution.runtime-mode' = 'streaming';
   select count(*) AS `count` from bounded_pageviews;

   -- 5. See the changelog
   SET 'sql-client.execution.result-mode' = 'changelog';
   select count(*) AS `count` from bounded_pageviews;

   -- 6. Create unbounded table
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

   -- 7. Stream processing with unbounded data
   SET 'sql-client.execution.result-mode' = 'table';
   select count(*) AS `count` from pageviews;

   -- Exit when done
   quit;
   ```

### Expected Behavior (Course Learning Objectives)

- **Batch Mode**: Waits for all 500 rows, then shows final count (500)
- **Streaming Mode**: Shows incremental updates (100, 200, 300, 400, 500)
- **Changelog Mode**: Shows all update operations (+I, -U, +U)
- **Unbounded Stream**: Continuously processes data, count keeps increasing

## 🔧 Development Workflow

### Compile Java Flink Jobs
```bash
# Build all Flink applications
./flink-setup.sh build

# Package and copy to Flink jobs directory  
mvn clean package
cp target/kafka-getting-started-1.0-SNAPSHOT.jar flink-jobs/
```

### Submit Jobs via Web UI
1. Open [Flink Web UI](http://localhost:8082)
2. Click "Submit New Job"
3. Upload JAR from `flink-jobs/` directory
4. Configure job parameters and submit

### Submit Jobs via REST API
```bash
# Upload JAR
curl -X POST -H "Expect:" -F "jarfile=@flink-jobs/kafka-getting-started-1.0-SNAPSHOT.jar" \
  http://localhost:8082/jars/upload

# Submit job (use JAR ID from upload response)
curl -X POST http://localhost:8082/jars/{jarId}/run \
  -H 'Content-Type: application/json' \
  -d '{"entryClass": "com.learning.kafkagettingstarted.flink.BasicKafkaFlinkJob"}'
```

## 📁 File Structure

```
KafkaGettingStarted/
├── flink-sql/
│   ├── course-exercises.sql          # Exact Confluent course exercises
│   ├── exercise-sql-scripts.sql      # Extended SQL examples
│   └── advanced-analytics.sql        # Advanced Flink SQL patterns
├── src/main/java/.../flink/
│   ├── BasicKafkaFlinkJob.java       # DataStream API example
│   ├── AdvancedKafkaFlinkJob.java    # Advanced Flink features
│   └── FlinkSqlJobRunner.java        # Execute SQL from Java
├── docker-compose.flink.yml          # Flink cluster configuration
├── flink-setup.sh                    # Automation script
└── .vscode/tasks.json               # VS Code integration
```

## 🌐 Access Points

- **Flink Web UI**: http://localhost:8082
- **Kafka UI**: http://localhost:8081
- **Kafka Connect**: http://localhost:8083

## 🎯 Course Progress Tracking

### Completed Modules
- ✅ Module 1-3: Course Introduction and Flink SQL Basics
- ✅ Module 4: **Batch and Stream Processing Exercise** ← You are here
- 🔲 Module 5-15: Advanced Topics (your environment supports these too!)

### Next Steps in Course
After completing Exercise 4, you can continue with:
- Module 5: The Flink Runtime
- Module 6: Using the Flink Web UI (Exercise)
- Module 8: Deploying an ETL Pipeline using Flink SQL (Exercise)
- Module 10: Streaming Analytics with Flink SQL (Exercise)

## 🔧 Troubleshooting

### If SQL Client won't start:
```bash
./flink-setup.sh status  # Check service health
./flink-setup.sh start-course  # Restart environment
```

### If faker connector not found:
```bash
# The setup automatically downloads it, but you can verify:
docker exec flink-learning-sql-client ls -la /opt/flink/lib/flink-faker*
```

### If you see "Connection refused":
```bash
# Wait for services to be ready
./flink-setup.sh start-course
# Wait for "Course environment is ready!" message
```

## 📚 Additional Resources

- **Course**: https://developer.confluent.io/courses/apache-flink/
- **Exercise 4**: https://developer.confluent.io/courses/apache-flink/stream-processing-exercise/
- **Flink Faker**: https://github.com/knaufk/flink-faker
- **Flink SQL Documentation**: https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/table/sql/

## 🎉 What's Different from Standard Setup

This environment is specifically configured to match the Confluent course:

1. **Flink Faker Connector**: Automatically downloaded and configured
2. **Course-specific SQL Scripts**: Exact commands from the exercise
3. **Optimized for Learning**: Resource limits, helpful startup messages
4. **Interactive Guidance**: Step-by-step instructions in SQL Client
5. **One-Command Workflow**: `./flink-setup.sh sql-course` gets you started immediately

Happy learning! 🚀
