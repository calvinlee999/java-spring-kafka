# Kafka Learning Environment - Docker Configuration

## Overview
This Kafka setup is optimized for your Docker Desktop configuration:
- **Docker Desktop**: 28.2.2 with 8 CPUs and 7.654GB RAM
- **Kafka Version**: 3.6.1 (KRaft mode - no Zookeeper needed)
- **Architecture**: Single-node setup optimized for learning and development

## Quick Start

### 1. Basic Kafka Only
```bash
# Start Kafka broker only
docker-compose -f kafka-single-node.yml up -d kafka

# Check if Kafka is running
docker-compose -f kafka-single-node.yml ps
```

### 2. Kafka + UI (Recommended for Learning)
```bash
# Start Kafka + Web UI
docker-compose -f kafka-single-node.yml --profile ui up -d

# Access Kafka UI at: http://localhost:8081
```

### 3. Development Mode (More Permissive Settings)
```bash
# Start with development overrides
docker-compose -f kafka-single-node.yml -f docker-compose.dev.yml --profile ui up -d
```

### 4. Full Stack with Monitoring
```bash
# Start everything including metrics exporter
docker-compose -f kafka-single-node.yml --profile ui --profile monitoring up -d

# Metrics available at: http://localhost:9308/metrics
```

## Configuration Highlights

### Resource Allocation (Optimized for your 8-core system)
- **Kafka**: 4 CPUs max, 2GB RAM, 1.5GB heap
- **Kafka UI**: 1 CPU max, 512MB RAM
- **Network Threads**: 8 (matching your CPU cores)
- **IO Threads**: 16 (2x network threads for optimal performance)

### Performance Tuning
- **Partitions**: 6 default (good parallelism for learning)
- **Segment Size**: 256MB (more manageable than default 1GB)
- **Compression**: Snappy (good balance of speed/compression)
- **GC**: G1GC with 100ms max pause time

### Security & Best Practices
- Explicit topic creation (auto-create disabled in production mode)
- Proper health checks with extended timeouts
- Log rotation configured
- Resource limits enforced
- Network isolation with dedicated subnet

## Useful Commands

### Topic Management
```bash
# Create a topic
docker exec kafka-learning-broker kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --topic my-topic \
  --partitions 6 --replication-factor 1

# List topics
docker exec kafka-learning-broker kafka-topics.sh \
  --bootstrap-server localhost:9092 --list

# Describe a topic
docker exec kafka-learning-broker kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe --topic my-topic
```

### Producer/Consumer Testing
```bash
# Start a console producer
docker exec -it kafka-learning-broker kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic my-topic

# Start a console consumer
docker exec -it kafka-learning-broker kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic my-topic \
  --from-beginning
```

### Monitoring and Debugging
```bash
# Check Kafka logs
docker logs kafka-learning-broker -f

# Check broker configuration
docker exec kafka-learning-broker kafka-configs.sh \
  --bootstrap-server localhost:9092 \
  --entity-type brokers --entity-name 1 --describe

# View consumer groups
docker exec kafka-learning-broker kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list
```

## Connection from Spring Boot Application

Your Spring Boot application should connect using:
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.group-id=kafka-learning-group
```

## Cleanup

```bash
# Stop all services
docker-compose -f kafka-single-node.yml down

# Remove volumes (WARNING: This deletes all data)
docker-compose -f kafka-single-node.yml down -v

# Clean up everything including networks
docker-compose -f kafka-single-node.yml down -v --remove-orphans
```

## Troubleshooting

### Common Issues
1. **Port conflicts**: Make sure ports 9092, 8081, 9308 are not in use
2. **Memory issues**: Reduce heap size in docker-compose.dev.yml if needed
3. **Startup timeout**: Kafka might take 45-60 seconds to start initially

### Health Check
```bash
# Check if Kafka is healthy
docker exec kafka-learning-broker kafka-broker-api-versions.sh \
  --bootstrap-server localhost:9092
```

### Performance Monitoring
- Use Kafka UI at http://localhost:8081 for visual monitoring
- Check metrics at http://localhost:9308/metrics (if monitoring profile enabled)
- Monitor Docker stats: `docker stats kafka-learning-broker`

## Environment Files
- `.env`: Main configuration variables
- `docker-compose.dev.yml`: Development overrides for easier local development
