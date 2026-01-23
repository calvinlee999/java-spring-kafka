# 🚀 Kafka Getting Started - A Complete Learning Project

## 📖 What is This Project?

This is a **complete learning project** designed to teach you Apache Kafka, Spring Boot, and PostgreSQL integration in a way that's easy to understand - even if you're new to programming!

Think of this project like a **digital post office** where:
- 📮 **Producers** are like people sending mail
- 📬 **Topics** are like different mailboxes (orders, notifications, etc.)
- 📦 **Consumers** are like mail workers who process the mail
- 🏪 **PostgreSQL** is like a filing cabinet that keeps records of all mail

## 🎯 What Will You Learn?

By the end of this project, you'll understand:

1. **Apache Kafka Basics**
   - How to send messages (Producer)
   - How to receive messages (Consumer)
   - What topics and partitions are
   - Consumer groups and load balancing

2. **Spring Boot Web Development**
   - REST API endpoints
   - Dependency injection
   - Service layers and controllers
   - Application configuration

3. **Database Integration**
   - PostgreSQL setup with Docker
   - JPA entities and repositories
   - Message persistence and retrieval
   - Database queries and transactions

4. **Real-World Patterns**
   - Event-driven architecture
   - Message acknowledgment and error handling
   - Monitoring and health checks
   - API documentation and testing

## 🏗️ Project Architecture

```
🌐 Web Browser/Client
    ↓ (HTTP Requests)
📱 REST Controller (KafkaController)
    ↓ (Business Logic)
⚙️ Producer Service → 📨 Apache Kafka → 📥 Consumer Service
                                            ↓
                                        💾 PostgreSQL Database
```

### 📂 Project Structure

```
KafkaGettingStarted/
├── 📁 src/main/java/com/learning/kafkagettingstarted/
│   ├── 🚀 KafkaGettingStartedApplication.java    # Main application starter
│   ├── 📁 controller/
│   │   └── 🎮 KafkaController.java               # Web API endpoints
│   ├── 📁 service/
│   │   ├── 📤 KafkaProducerService.java          # Message sending logic
│   │   ├── 📥 KafkaConsumerService.java          # Message receiving logic
│   │   └── 💾 KafkaMessagePersistenceService.java # Database operations
│   ├── 📁 entity/
│   │   └── 📋 KafkaMessage.java                  # Database table model
│   ├── 📁 repository/
│   │   └── 🗃️ KafkaMessageRepository.java        # Database queries
│   └── 📁 config/
│       └── ⚙️ KafkaConfig.java                   # Kafka configuration
├── 📁 src/main/resources/
│   ├── ⚙️ application.properties                 # App configuration
│   └── 🐳 kafka-single-node.yml                 # Docker Kafka setup
└── 📋 pom.xml                                    # Dependencies
```

## 🚀 Quick Start Guide

### 1. Prerequisites (What You Need)

- ☕ **Java 17 or higher** - The programming language
- 🐳 **Docker Desktop** - To run Kafka and PostgreSQL
- 💻 **VS Code or IntelliJ** - Code editor
- 🌐 **Web browser** - To test the API

### 2. Start the Infrastructure

```bash
# Start Kafka (message broker)
docker-compose -f src/main/resources/kafka-single-node.yml up -d

# Start PostgreSQL (database) - if not already running
# (Check if you have it running from another project)
```

### 3. Run the Application

```bash
# Method 1: Using Maven
mvn spring-boot:run

# Method 2: Using VS Code task
# Press Ctrl+Shift+P → "Tasks: Run Task" → "Spring Boot: Run"
```

### 4. Test Everything Works

Open your web browser and try these URLs:

```
🔍 Health Check:
http://localhost:8080/api/kafka/health

📤 Send a Message:
POST http://localhost:8080/api/kafka/orders
Body: {"key": "order123", "message": "Pizza order for John"}

📋 View All Messages:
http://localhost:8080/api/kafka/messages
```

## 🎮 How to Use the API

### 📤 Sending Messages

**Send Single Order Message:**
```bash
curl -X POST http://localhost:8080/api/kafka/orders \\
  -H "Content-Type: application/json" \\
  -d '{"key": "order123", "message": "Pizza order for John"}'
```

**Send Multiple Messages at Once:**
```bash
curl -X POST "http://localhost:8080/api/kafka/orders/batch?count=5"
```

### 📥 Retrieving Messages

**Get All Messages:**
```bash
curl http://localhost:8080/api/kafka/messages
```

**Get Messages from Specific Topic:**
```bash
curl http://localhost:8080/api/kafka/messages/topic/kafka.learning.orders
```

**Get Latest 10 Messages:**
```bash
curl http://localhost:8080/api/kafka/messages/latest?limit=10
```

## 🔧 Configuration Files Explained

### 📋 application.properties
This file contains all the settings for your application:

```properties
# Where to find Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/mydata
spring.datasource.username=cnldev
spring.datasource.password=cnldev_123

# Auto-create database tables
spring.jpa.hibernate.ddl-auto=update
```

### 🐳 Docker Configuration
The project includes Docker configurations for:
- **Kafka** - Message broker
- **Zookeeper** - Kafka's coordinator
- **PostgreSQL** - Database storage

## 🎓 Learning Path

### For Complete Beginners:
1. 📖 Start by reading the code comments in `KafkaGettingStartedApplication.java`
2. 🎮 Look at `KafkaController.java` to understand web APIs
3. 📤 Study `KafkaProducerService.java` to see how messages are sent
4. 📥 Examine `KafkaConsumerService.java` to see how messages are received
5. 💾 Check `KafkaMessage.java` to understand data storage

### For Intermediate Developers:
1. 🔧 Modify the message processing logic in consumer service
2. 📊 Add new REST endpoints for analytics
3. 🚨 Implement error handling and retry mechanisms
4. 📈 Add monitoring and metrics collection
5. 🔐 Implement security and authentication

### For Advanced Users:
1. 🏗️ Scale to multiple Kafka brokers
2. 🔄 Implement event sourcing patterns
3. 📊 Add stream processing with Kafka Streams
4. 🐳 Containerize the entire application
5. ☁️ Deploy to cloud platforms

## 🐛 Common Issues and Solutions

### "Kafka connection refused"
```bash
# Make sure Kafka is running
docker ps | grep kafka

# If not running, start it
docker-compose -f src/main/resources/kafka-single-node.yml up -d
```

### "Database connection failed"
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check connection settings in application.properties
```

### "Port 8080 already in use"
```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process or change port in application.properties
server.port=8081
```

## 🎯 Next Steps

Once you're comfortable with this project, try:

1. **🔄 Add More Message Types**
   - Create user registration messages
   - Add notification messages
   - Implement order status updates

2. **📊 Build a Dashboard**
   - Create a web UI to visualize messages
   - Add real-time message monitoring
   - Build message analytics

3. **🏗️ Microservices Architecture**
   - Split into separate services
   - Add service discovery
   - Implement API gateway

4. **☁️ Cloud Deployment**
   - Deploy to AWS/Azure/GCP
   - Use managed Kafka services
   - Implement CI/CD pipelines

## 🤝 Contributing

This is a learning project! Feel free to:
- 🐛 Report bugs or issues
- 💡 Suggest improvements
- 📝 Add more educational comments
- 🎯 Create additional examples

## 📚 Additional Resources

- 📖 [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- 🌱 [Spring Boot Guide](https://spring.io/guides/gs/spring-boot/)
- 🐘 [PostgreSQL Tutorial](https://www.postgresql.org/docs/current/tutorial.html)
- 🐳 [Docker Getting Started](https://docs.docker.com/get-started/)

## 🎉 Congratulations!

If you've made it this far, you now understand:
- ✅ Event-driven architecture
- ✅ Message queues and processing
- ✅ REST API development
- ✅ Database integration
- ✅ Modern development practices

**You're ready to build real-world applications!** 🚀

---

*Made with ❤️ for learning. Questions? Check the code comments - they're written to teach!*
