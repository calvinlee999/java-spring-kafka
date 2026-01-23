# 🛠️ Kafka CLI Installation Guide
## Industry Best Practices for Your Optimized Environment

Based on your optimized Docker Kafka setup, here are the **industry-recommended** approaches for Kafka CLI access:

---

## 🎯 **Option 1: Docker-Based CLI (RECOMMENDED)**

**✅ This is the BEST PRACTICE for your setup:**

### Why Docker-Based CLI?
- ✅ **Version Consistency**: CLI version matches your Docker Kafka exactly
- ✅ **Zero Conflicts**: No local Java/Kafka version issues
- ✅ **Easy Maintenance**: Update via Docker images
- ✅ **Isolation**: Clean separation from local environment
- ✅ **Production-Ready**: Same tools your production team uses

### Enhanced CLI Commands Available

Your optimized `kafka-ctl.sh` now includes enterprise-grade CLI functionality:

```bash
# 📋 TOPIC MANAGEMENT
./kafka-ctl.sh topics create orders 12 1        # Create optimized topic
./kafka-ctl.sh topics list                      # List all topics
./kafka-ctl.sh topics describe orders           # Detailed topic info
./kafka-ctl.sh topics config orders             # Topic configuration

# 👥 CONSUMER GROUPS
./kafka-ctl.sh groups list                      # List consumer groups
./kafka-ctl.sh groups describe my-group         # Group details & lag
./kafka-ctl.sh groups reset my-group orders     # Reset offsets

# ⚙️ CONFIGURATION
./kafka-ctl.sh config broker                    # Broker settings
./kafka-ctl.sh config topic orders              # Topic settings

# ⚡ PERFORMANCE TESTING
./kafka-ctl.sh perf producer orders 100000 1024 # Producer test
./kafka-ctl.sh perf consumer orders 50000       # Consumer test

# 🛠️ RAW CLI ACCESS
./kafka-ctl.sh exec kafka-topics.sh --help      # Direct CLI access
./kafka-ctl.sh exec kafka-console-consumer.sh --topic orders --bootstrap-server localhost:9092
```

---

## 🔧 **Option 2: Native Kafka CLI Installation**

If you prefer local CLI tools, here's the industry-standard installation:

### 2A. Via Homebrew (Recommended for macOS)

```bash
# Install Kafka via Homebrew
brew install kafka

# Verify installation
kafka-topics --version

# Add to PATH (usually automatic)
export PATH="/opt/homebrew/bin:$PATH"
```

### 2B. Manual Installation (Cross-Platform)

```bash
# Download Kafka (Scala 2.13 version recommended)
wget https://downloads.apache.org/kafka/2.8.2/kafka_2.13-2.8.2.tgz

# Extract and setup
tar -xzf kafka_2.13-2.8.2.tgz
sudo mv kafka_2.13-2.8.2 /opt/kafka

# Add to PATH
echo 'export PATH="/opt/kafka/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

### Native CLI Usage Examples

```bash
# Connect to your Docker Kafka
kafka-topics --bootstrap-server localhost:9092 --list
kafka-console-producer --bootstrap-server localhost:9092 --topic orders
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders --from-beginning
```

---

## 🎯 **Option 3: IDE Integration**

### VS Code Kafka Extension

```bash
# Install Kafka extension for VS Code
code --install-extension jeppeandersen.vscode-kafka

# Features:
# - Topic browsing
# - Message producing/consuming
# - Consumer group monitoring
# - Configuration viewing
```

### IntelliJ IDEA Integration

- **Kafka Plugin**: Provides GUI for Kafka operations
- **Big Data Tools**: Enterprise-grade Kafka management

---

## 🏆 **Recommended Setup for Your Environment**

### **Production-Grade Configuration**

Based on your optimized Docker setup, here's the recommended approach:

```bash
# 1. Use Docker CLI for primary operations (already configured)
./kafka-ctl.sh start ui

# 2. Install native CLI as backup
brew install kafka

# 3. Create aliases for convenience
echo 'alias kt="./kafka-ctl.sh topics"' >> ~/.bashrc
echo 'alias kc="./kafka-ctl.sh consumer"' >> ~/.bashrc
echo 'alias kp="./kafka-ctl.sh producer"' >> ~/.bashrc
echo 'alias kexec="./kafka-ctl.sh exec"' >> ~/.bashrc

# 4. Source the changes
source ~/.bashrc
```

### **Workflow Integration**

```bash
# Development workflow
kt create orders 12 1          # Create topic
kp orders                      # Start producer
kc orders                      # Start consumer (in another terminal)

# Performance testing
./kafka-ctl.sh perf producer orders 100000 1024

# Monitoring
./kafka-ctl.sh groups list
./kafka-ctl.sh groups describe my-consumer-group
```

---

## 📊 **CLI Command Reference**

### **Essential Commands Matrix**

| **Operation** | **Docker CLI** | **Native CLI** |
|---------------|----------------|----------------|
| **List Topics** | `./kafka-ctl.sh topics list` | `kafka-topics --bootstrap-server localhost:9092 --list` |
| **Create Topic** | `./kafka-ctl.sh topics create orders 12 1` | `kafka-topics --bootstrap-server localhost:9092 --create --topic orders --partitions 12 --replication-factor 1` |
| **Produce Messages** | `./kafka-ctl.sh producer orders` | `kafka-console-producer --bootstrap-server localhost:9092 --topic orders` |
| **Consume Messages** | `./kafka-ctl.sh consumer orders` | `kafka-console-consumer --bootstrap-server localhost:9092 --topic orders --from-beginning` |
| **Consumer Groups** | `./kafka-ctl.sh groups list` | `kafka-consumer-groups --bootstrap-server localhost:9092 --list` |

### **Advanced Operations**

```bash
# Performance Testing
./kafka-ctl.sh perf producer test-topic 1000000 1024
./kafka-ctl.sh perf consumer test-topic 1000000

# Configuration Management
./kafka-ctl.sh config broker
./kafka-ctl.sh config topic orders

# Consumer Group Management
./kafka-ctl.sh groups describe my-group
./kafka-ctl.sh groups reset my-group orders

# Raw CLI Access (any Kafka command)
./kafka-ctl.sh exec kafka-log-dirs.sh --bootstrap-server localhost:9092 --describe
./kafka-ctl.sh exec kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

---

## 🚀 **Next Steps**

1. **Test the Enhanced CLI**:
   ```bash
   ./kafka-ctl.sh start ui
   ./kafka-ctl.sh topics create demo 6 1
   ./kafka-ctl.sh producer demo
   ```

2. **Install Native CLI (Optional)**:
   ```bash
   brew install kafka
   ```

3. **Explore Advanced Features**:
   ```bash
   ./kafka-ctl.sh cli
   ./kafka-ctl.sh perf producer demo 10000 512
   ```

Your Kafka environment now provides **enterprise-grade CLI capabilities** while maintaining the simplicity needed for learning and development!
