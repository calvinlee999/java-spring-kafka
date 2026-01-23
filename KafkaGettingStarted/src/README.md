# Kafka Getting Started - Spring Boot Project

This is a comprehensive Spring Boot Maven project for learning Apache Kafka concepts and implementation.

## Project Structure

```
kafka-getting-started/
├── pom.xml                                 # Maven configuration with Spring Boot and Kafka dependencies
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/learning/kafkagettingstarted/
│   │   │       ├── KafkaGettingStartedApplication.java    # Spring Boot main application
│   │   │       ├── config/
│   │   │       │   └── KafkaConfig.java                   # Kafka configuration and topic definitions
│   │   │       ├── controller/
│   │   │       │   └── KafkaController.java               # REST endpoints for Kafka operations
│   │   │       ├── service/
│   │   │       │   ├── KafkaProducerService.java         # Kafka producer service
│   │   │       │   └── KafkaConsumerService.java         # Kafka consumer service
│   │   │       └── chapter5/                              # Original chapter 5 examples
│   │   │       └── chapter6/                              # Original chapter 6 examples
│   │   └── resources/
│   │       ├── application.properties                     # Spring Boot and Kafka configuration
│   │       └── chapter-*-commands.txt                     # Learning materials
│   └── test/
│       └── java/                                          # Test classes location
```

## Technologies Used

- **Spring Boot 3.2.0** - Application framework
- **Spring Kafka** - Spring integration for Apache Kafka
- **Apache Kafka 3.6.0** - Message streaming platform
- **Maven** - Build and dependency management
- **Lombok** - Reduce boilerplate code
- **Jackson** - JSON processing
- **Testcontainers** - Integration testing with Docker containers

## Features

### Spring Boot Integration
- Auto-configuration for Kafka
- Health checks and monitoring via Actuator
- RESTful API endpoints for producer operations
- Annotation-based message consumers
- Comprehensive logging and error handling

### Kafka Functionality
- **Producers**: Send messages to Kafka topics
- **Consumers**: Listen and process messages from topics
- **Topic Management**: Auto-creation of topics with proper configuration
- **Message Serialization**: String-based message handling
- **Error Handling**: Proper exception handling and retry logic

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **Apache Kafka** running locally on `localhost:9092`

## Kafka Setup

### Start Kafka (using Docker)
```bash
# Start Zookeeper and Kafka using Docker Compose
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.8
docker run -d --name kafka -p 9092:9092 --link zookeeper \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.4.0
```

### Or using Kafka directly
```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Server
bin/kafka-server-start.sh config/server.properties
```

## Running the Application

### 1. Build the project
```bash
mvn clean compile
```

### 2. Run the Spring Boot application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Verify application is running
```bash
curl http://localhost:8080/api/kafka/health
```

## API Endpoints

### Send Single Order Message
```bash
POST http://localhost:8080/api/kafka/orders
Content-Type: application/json

{
  "key": "order-123",
  "message": "New order placed for customer XYZ"
}
```

### Send Use Case Message
```bash
POST http://localhost:8080/api/kafka/usecase
Content-Type: application/json

{
  "key": "usecase-456",
  "message": "Processing use case scenario"
}
```

### Send Batch Order Messages
```bash
POST http://localhost:8080/api/kafka/orders/batch?count=5
```

### Health Check
```bash
GET http://localhost:8080/api/kafka/health
```

## Monitoring and Management

### Actuator Endpoints
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Kafka metrics: `http://localhost:8080/actuator/kafka`

### Logs
The application logs detailed information about:
- Message production and consumption
- Kafka connection status
- Error handling and retries
- Processing times and offsets

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

## Configuration

### Key Configuration Properties (application.properties)
```properties
# Kafka Bootstrap Servers
spring.kafka.bootstrap-servers=localhost:9092

# Consumer Configuration
spring.kafka.consumer.group-id=kafka-learning-group
spring.kafka.consumer.auto-offset-reset=earliest

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Logging
logging.level.org.springframework.kafka=DEBUG
```

## Topics Created

The application automatically creates the following topics:
- `kafka.learning.orders` (3 partitions, replication factor 1)
- `kafka.learning.usecase` (3 partitions, replication factor 1)

## Learning Exercises

### Exercise 1: Basic Producer
Use the REST API to send messages and observe the logs.

### Exercise 2: Consumer Processing
Monitor how messages are consumed and processed by the consumer services.

### Exercise 3: Batch Processing
Send batch messages and observe partitioning and parallel processing.

### Exercise 4: Error Handling
Introduce errors in message processing to see retry behavior.

## Original Examples

The project includes the original chapter examples:
- `chapter5/KafkaSimpleProducer.java` - Basic producer implementation
- `chapter5/KafkaSimpleConsumer.java` - Basic consumer implementation
- `chapter6/KafkaUseCaseProducer.java` - Use case producer
- `chapter6/KafkaUseCaseConsumer.java` - Use case consumer

## Troubleshooting

### Common Issues

1. **Kafka Connection Failed**
   - Ensure Kafka is running on `localhost:9092`
   - Check if topics are created properly

2. **Consumer Not Receiving Messages**
   - Verify consumer group configuration
   - Check offset reset policy

3. **Build Failures**
   - Ensure Java 17 is installed and configured
   - Run `mvn clean install` to resolve dependencies

### Useful Commands

```bash
# List topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Describe topic
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic kafka.learning.orders

# Console consumer
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic kafka.learning.orders --from-beginning
```

## Next Steps

1. Explore Kafka Streams for stream processing
2. Implement JSON message serialization/deserialization
3. Add schema registry integration
4. Implement error handling with dead letter topics
5. Add monitoring with Prometheus and Grafana

## Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
