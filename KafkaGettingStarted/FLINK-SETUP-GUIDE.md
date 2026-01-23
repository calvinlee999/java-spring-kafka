# Apache Flink Integration Setup Guide

## Overview

This guide helps you set up Apache Flink with your existing Kafka and PostgreSQL environment, following the Confluent course "Building Apache Flink Applications in Java".

## 🚀 Quick Start

### 1. Start the Complete Environment

```bash
# Terminal 1: Start Kafka and PostgreSQL
docker-compose -f kafka-single-node.yml up

# Terminal 2: Start Flink cluster 
docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml up

# Terminal 3: Optional - Start with Kafka Connect
docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml --profile connect up
```

### 2. Verify Services

| Service | URL | Description |
|---------|-----|-------------|
| **Flink Web UI** | http://localhost:8082 | Job management and monitoring |
| **Kafka UI** | http://localhost:8081 | Kafka topic and message management |
| **Kafka Connect** | http://localhost:8083 | Connector management (if enabled) |
| **pgAdmin** | http://localhost:5050 | PostgreSQL management |

### 3. Build and Deploy Flink Jobs

```bash
# Build the project
mvn clean package

# Copy JAR to Flink jobs directory
cp target/kafka-getting-started-1.0.0.jar flink-jobs/

# Submit job via Flink Web UI or REST API
curl -X POST -H "Expect:" -F "jarfile=@flink-jobs/kafka-getting-started-1.0.0.jar" \
  http://localhost:8082/jars/upload
```

## 📚 Course Learning Path

### Module Progression

Following the Confluent course structure:

1. **Setup Flink Environment** ✅
   - Flink JobManager and TaskManager configured
   - Kafka integration ready
   - Development environment prepared

2. **Basic Flink Job** → `BasicKafkaFlinkJob.java`
   - Consume from Kafka topic
   - Simple data transformation
   - Produce to Kafka topic

3. **Advanced Features** → `AdvancedKafkaFlinkJob.java`
   - Windowing and watermarks
   - Keyed state management
   - Stream aggregation
   - Multiple data sources

### Course Learning Objectives ✅

- [x] **Datastream Programming**: Using DataStream API
- [x] **Flink Data Sources**: Kafka source configuration
- [x] **Serializers & Deserializers**: JSON and string handling
- [x] **Data Transformations**: Custom processing functions
- [x] **Flink Data Sinks**: Kafka sink configuration
- [x] **Branching Streams**: Multiple output paths
- [x] **Merging Streams**: Combining data sources
- [x] **Windowing**: Time-based aggregations
- [x] **Keyed State**: Stateful stream processing

## 🔧 Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │      Kafka      │    │      Flink      │
│   (Port 5432)   │◄──►│   (Port 9092)   │◄──►│   (Port 8082)   │
│                 │    │                 │    │                 │
│ • Data Storage  │    │ • Message Bus   │    │ • Stream Proc.  │
│ • pgAdmin UI    │    │ • Kafka UI      │    │ • JobManager    │
│ • Persistence   │    │ • Topics        │    │ • TaskManager   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │  Kafka Connect  │
                    │   (Port 8083)   │
                    │                 │
                    │ • JDBC Connectors│
                    │ • Data Integration│
                    │ • Change Data   │
                    │   Capture (CDC) │
                    └─────────────────┘
```

## 🎯 Sample Data Flow

### Example Order Processing Pipeline

1. **Input**: Order events in Kafka topic `kafka.learning.orders`
   ```json
   {
     "orderId": "order-123",
     "customerId": "customer-456", 
     "amount": 99.99,
     "status": "pending"
   }
   ```

2. **Flink Processing**:
   - Parse JSON messages
   - Enrich with customer state
   - Window aggregation (1-minute tumbling windows)
   - Calculate customer order statistics

3. **Output**: Aggregated results to `kafka.learning.aggregated-orders`
   ```
   OrderSummary{customer='customer-456', orders=3, total=299.97, window=[...]}
   ```

## 📊 Monitoring and Debugging

### Flink Web UI Features
- **Job Overview**: Running jobs and their status
- **Task Managers**: Resource utilization
- **Checkpoints**: Fault tolerance status
- **Metrics**: Throughput and latency

### Key Metrics to Monitor
- **Records/second**: Message processing rate
- **Latency**: End-to-end processing time  
- **Checkpointing**: Backup success rate
- **Memory usage**: TaskManager resource consumption

## 🔍 Troubleshooting

### Common Issues

1. **Job Submission Fails**
   ```bash
   # Check TaskManager availability
   curl http://localhost:8082/taskmanagers
   
   # Verify JAR file
   ls -la flink-jobs/
   ```

2. **Kafka Connection Issues**
   ```bash
   # Test Kafka connectivity
   docker exec kafka-learning-broker kafka-topics.sh \
     --bootstrap-server localhost:9092 --list
   ```

3. **Memory Issues**
   ```bash
   # Check container resources
   docker stats flink-learning-taskmanager
   ```

### Debugging Tips

- Use Flink Web UI for job visualization
- Check container logs: `docker logs flink-learning-jobmanager`
- Monitor Kafka topics with Kafka UI
- Verify data flow with sample messages

## 🚀 Next Steps

### Advanced Topics (Post-Course)

1. **Production Deployment**
   - Multi-node Flink cluster
   - High availability setup
   - SSL/TLS security

2. **Performance Optimization** 
   - Parallelism tuning
   - Memory configuration
   - Checkpoint optimization

3. **Complex Event Processing**
   - Pattern detection
   - Complex windowing
   - Custom state backends

4. **Integration Patterns**
   - Schema Registry (Avro)
   - Exactly-once semantics
   - Backpressure handling

## 📖 Additional Resources

- [Confluent Flink Course](https://developer.confluent.io/courses/flink-java/overview/)
- [Apache Flink Documentation](https://nightlies.apache.org/flink/flink-docs-master/)
- [Flink DataStream API](https://nightlies.apache.org/flink/flink-docs-master/docs/learn-flink/datastream_api/)
- [Kafka Connector for Flink](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/datastream/kafka/)

## 🎓 Course Completion Certificate

Upon completing all exercises and understanding the concepts, you'll have practical experience with:

- Modern stream processing architectures
- Real-time data transformation
- Stateful stream processing
- Window-based analytics
- Fault-tolerant data pipelines

**Happy Streaming! 🚀**
