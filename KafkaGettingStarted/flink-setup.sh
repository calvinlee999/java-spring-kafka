#!/bin/bash

# Apache Flink with Kafka Setup Script
# Based on Confluent's "Building Apache Flink Applications in Java" course

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
FLINK_WEB_UI="http://localhost:8082"
KAFKA_UI="http://localhost:8081"
CONNECT_API="http://localhost:8083"

# Helper functions
print_header() {
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1

    echo -e "${YELLOW}Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    print_error "$service_name failed to start within $((max_attempts * 2)) seconds"
    return 1
}

# Main functions
start_kafka() {
    print_header "Starting Kafka Environment"
    
    echo "Starting Kafka and PostgreSQL..."
    docker-compose -f kafka-single-node.yml up -d
    
    if wait_for_service "$KAFKA_UI" "Kafka UI"; then
        print_success "Kafka environment is running"
        echo -e "🌐 Kafka UI: ${KAFKA_UI}"
    else
        print_error "Failed to start Kafka environment"
        exit 1
    fi
}

start_flink() {
    print_header "Starting Flink Cluster"
    
    echo "Starting Flink JobManager and TaskManager..."
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml up -d
    
    if wait_for_service "$FLINK_WEB_UI/overview" "Flink Web UI"; then
        print_success "Flink cluster is running"
        echo -e "🌐 Flink Web UI: ${FLINK_WEB_UI}"
    else
        print_error "Failed to start Flink cluster"
        exit 1
    fi
}

start_flink_sql() {
    print_header "Starting Flink SQL Client"
    
    echo "Starting Flink cluster with SQL Client..."
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml --profile sql up -d
    
    if wait_for_service "$FLINK_WEB_UI/overview" "Flink Web UI"; then
        print_success "Flink cluster with SQL Client is running"
        echo -e "🌐 Flink Web UI: ${FLINK_WEB_UI}"
        echo ""
        echo "To access Flink SQL Client interactively:"
        echo "  docker exec -it flink-learning-sql-client /opt/flink/bin/sql-client.sh"
    else
        print_error "Failed to start Flink cluster"
        exit 1
    fi
}

start_connect() {
    print_header "Starting Kafka Connect"
    
    echo "Starting Kafka Connect with PostgreSQL connectors..."
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml --profile connect up -d
    
    if wait_for_service "$CONNECT_API/connectors" "Kafka Connect"; then
        print_success "Kafka Connect is running"
        echo -e "🌐 Connect API: ${CONNECT_API}"
    else
        print_error "Failed to start Kafka Connect"
        exit 1
    fi
}

start_flink_course() {
    print_header "Starting Confluent Flink 101 Course Environment"
    
    echo "Starting complete environment for course exercises..."
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml up -d
    
    if wait_for_service "$FLINK_WEB_UI/overview" "Flink Web UI"; then
        print_success "Course environment is ready!"
        echo
        echo -e "🎓 ${BLUE}Confluent Apache Flink 101 Course Environment${NC}"
        echo -e "=================================================="
        echo -e "🌐 Flink Web UI: ${FLINK_WEB_UI}"
        echo -e "🌐 Kafka UI: ${KAFKA_UI}"
        echo
        echo -e "${YELLOW}Ready for Exercise 4: Batch and Stream Processing with Flink SQL${NC}"
        echo -e "Use './flink-setup.sh sql-course' to start the interactive SQL session"
        echo
    else
        print_error "Failed to start course environment"
        exit 1
    fi
}

build_project() {
    print_header "Building Flink Applications"
    
    echo "Compiling Java code and packaging JARs..."
    mvn clean package -DskipTests
    
    if [ -f "target/kafka-getting-started-1.0.0.jar" ]; then
        print_success "Build completed successfully"
        
        # Create flink-jobs directory if it doesn't exist
        mkdir -p flink-jobs
        
        # Copy JAR to Flink jobs directory
        cp target/kafka-getting-started-1.0.0.jar flink-jobs/
        print_success "JAR copied to flink-jobs directory"
    else
        print_error "Build failed - JAR file not found"
        exit 1
    fi
}

deploy_connectors() {
    print_header "Deploying Kafka Connect Connectors"
    
    echo "Creating PostgreSQL source connector..."
    if curl -s -X POST -H "Content-Type: application/json" \
        -d @connectors/postgres-source-connector.json \
        "$CONNECT_API/connectors" > /dev/null; then
        print_success "PostgreSQL source connector created"
    else
        print_warning "Failed to create source connector (may already exist)"
    fi
    
    echo "Creating PostgreSQL sink connector..."
    if curl -s -X POST -H "Content-Type: application/json" \
        -d @connectors/postgres-sink-connector.json \
        "$CONNECT_API/connectors" > /dev/null; then
        print_success "PostgreSQL sink connector created"
    else
        print_warning "Failed to create sink connector (may already exist)"
    fi
}

create_topics() {
    print_header "Creating Kafka Topics"
    
    # Topics for course exercises
    topics=(
        "kafka.learning.orders"
        "kafka.learning.usecase" 
        "kafka.learning.processed-orders"
        "kafka.learning.aggregated-orders"
    )
    
    for topic in "${topics[@]}"; do
        echo "Creating topic: $topic"
        docker exec kafka-learning-broker kafka-topics.sh \
            --bootstrap-server localhost:9092 \
            --create --if-not-exists \
            --topic "$topic" \
            --partitions 3 \
            --replication-factor 1 > /dev/null 2>&1
        print_success "Topic $topic created"
    done
}

send_sample_data() {
    print_header "Sending Sample Data"
    
    echo "Sending sample order messages..."
    
    # Sample order data for course exercises
    sample_orders=(
        '{"orderId":"order-001","customerId":"customer-1","amount":99.99,"status":"pending"}'
        '{"orderId":"order-002","customerId":"customer-1","amount":149.50,"status":"confirmed"}'
        '{"orderId":"order-003","customerId":"customer-2","amount":75.25,"status":"pending"}'
        '{"orderId":"order-004","customerId":"customer-2","amount":200.00,"status":"confirmed"}'
        '{"orderId":"order-005","customerId":"customer-3","amount":50.75,"status":"shipped"}'
    )
    
    for order in "${sample_orders[@]}"; do
        echo "$order" | docker exec -i kafka-learning-broker kafka-console-producer.sh \
            --bootstrap-server localhost:9092 \
            --topic kafka.learning.orders > /dev/null 2>&1
    done
    
    print_success "Sample order data sent to kafka.learning.orders"
    
    # Sample use case data
    echo "Sample use case data" | docker exec -i kafka-learning-broker kafka-console-producer.sh \
        --bootstrap-server localhost:9092 \
        --topic kafka.learning.usecase > /dev/null 2>&1
    
    print_success "Sample use case data sent to kafka.learning.usecase"
}

run_sql_exercise() {
    print_header "Running Flink SQL Exercise"
    
    if ! docker ps | grep -q "flink-learning-sql-client"; then
        print_error "SQL Client not running. Start with: $0 start-sql"
        exit 1
    fi
    
    local mode=${1:-streaming}
    
    echo "Executing Flink SQL exercise in $mode mode..."
    
    # Create tables and run basic queries
    docker exec flink-learning-sql-client /opt/flink/bin/sql-client.sh \
        -f /opt/flink/sql-scripts/exercise-sql-scripts.sql
    
    print_success "SQL exercise completed"
}

interactive_sql() {
    print_header "Starting Interactive Flink SQL Session"
    
    if ! docker ps | grep -q "flink-learning-sql-client"; then
        print_error "SQL Client not running. Start with: $0 start-sql"
        exit 1
    fi
    
    echo "Starting interactive Flink SQL Client..."
    echo "Use 'QUIT;' to exit the SQL Client"
    echo ""
    
    docker exec -it flink-learning-sql-client /opt/flink/bin/sql-client.sh
}

sql_course() {
    print_header "Starting Confluent Course SQL Session"
    
    echo "Checking if Flink cluster is running..."
    if ! curl -s -f "$FLINK_WEB_UI/overview" > /dev/null 2>&1; then
        print_warning "Flink cluster not running. Starting it now..."
        start_flink_course
    fi
    
    echo
    echo -e "${BLUE}🎓 Confluent Apache Flink 101 - Exercise 4${NC}"
    echo -e "==========================================="
    echo -e "${YELLOW}Starting Flink SQL Client for course exercises...${NC}"
    echo
    echo -e "Course scripts loaded: /opt/flink/sql-scripts/course-exercises.sql"
    echo -e "Follow along with: https://developer.confluent.io/courses/apache-flink/stream-processing-exercise/"
    echo
    
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml run --rm flink-sql-client
}

run_course_exercise() {
    print_header "Running Course Exercise Examples"
    
    echo "This will demonstrate the key course concepts..."
    echo
    
    # Start SQL Client in batch mode to run course examples
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml run --rm flink-sql-client bash -c "
        echo 'Setting up course demonstration...'
        
        # Wait for JobManager
        until curl -s http://flink-jobmanager:8081/overview > /dev/null 2>&1; do
            sleep 2
        done
        
        echo '🎓 Running Confluent Course Exercise Demo'
        echo '========================================'
        echo 'Creating tables and running example queries...'
        echo
        
        /opt/flink/bin/sql-client.sh <<EOF
-- Create the bounded table from the course
CREATE TABLE \`bounded_pageviews\` (
  \`url\` STRING,
  \`user_id\` STRING,
  \`browser\` STRING,
  \`ts\` TIMESTAMP(3)
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

-- Show some sample data
SELECT * FROM bounded_pageviews LIMIT 5;

-- Run count in batch mode
SET 'execution.runtime-mode' = 'batch';
SELECT count(*) AS \`count\` FROM bounded_pageviews;

-- Exit
QUIT;
EOF
    "
    
    print_success "Course exercise demo completed!"
    echo
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Run './flink-setup.sh sql-course' for interactive session"
    echo "2. Follow the full course at: https://developer.confluent.io/courses/apache-flink/"
}

show_status() {
    print_header "Environment Status"
    
    echo "Checking service health..."
    echo
    
    # Kafka
    if curl -s -f "$KAFKA_UI" > /dev/null 2>&1; then
        echo -e "🟢 Kafka UI: ${KAFKA_UI}"
    else
        echo -e "🔴 Kafka UI: Not accessible"
    fi
    
    # Flink
    if curl -s -f "$FLINK_WEB_UI/overview" > /dev/null 2>&1; then
        echo -e "🟢 Flink Web UI: ${FLINK_WEB_UI}"
    else
        echo -e "🔴 Flink Web UI: Not accessible"
    fi
    
    # Kafka Connect
    if curl -s -f "$CONNECT_API/connectors" > /dev/null 2>&1; then
        echo -e "🟢 Kafka Connect: ${CONNECT_API}"
    else
        echo -e "🔴 Kafka Connect: Not accessible"
    fi
    
    echo
    echo "Container Status:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(kafka|flink|postgres)"
}

stop_all() {
    print_header "Stopping All Services"
    
    echo "Stopping all containers..."
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml --profile connect down
    
    print_success "All services stopped"
}

clean_all() {
    print_header "Cleaning Up Environment"
    
    echo "Stopping and removing all containers and volumes..."
    docker-compose -f kafka-single-node.yml -f docker-compose.flink.yml --profile connect down -v
    
    # Clean up build artifacts
    if [ -d "target" ]; then
        rm -rf target
        print_success "Maven target directory cleaned"
    fi
    
    if [ -d "flink-jobs" ]; then
        rm -rf flink-jobs/*.jar
        print_success "Flink jobs directory cleaned"
    fi
    
    print_success "Environment cleaned up"
}

# Help function
show_help() {
    echo "Apache Flink with Kafka Setup Script"
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  start-kafka       Start Kafka and PostgreSQL"
    echo "  start-flink       Start Flink cluster"
    echo "  start-sql         Start Flink cluster with SQL Client"
    echo "  start-connect     Start Kafka Connect"
    echo "  start-all         Start complete environment"
    echo "  start-course      Start Confluent course environment"
    echo "  build             Build Flink applications"
    echo "  deploy            Deploy Kafka connectors"
    echo "  topics            Create Kafka topics"
    echo "  sample-data       Send sample data to topics"
    echo "  sql-exercise      Run Flink SQL exercise"
    echo "  sql-interactive   Start interactive SQL session"
    echo "  sql-course        Start Confluent course SQL session"
    echo "  run-course-exercise  Run automated course demo"
    echo "  status            Show environment status"
    echo "  stop              Stop all services"
    echo "  clean             Clean up environment"
    echo "  help              Show this help message"
    echo
    echo "🎓 Confluent Apache Flink 101 Course Workflow:"
    echo "  $0 start-course         # Start course environment"
    echo "  $0 sql-course           # Interactive Flink SQL (Exercise 4)"
    echo "  $0 run-course-exercise  # Automated course demo"
    echo
    echo "Traditional Learning Path:"
    echo "  1. $0 start-all         # Start complete environment"
    echo "  2. $0 build             # Build Flink applications" 
    echo "  3. $0 topics            # Create Kafka topics"
    echo "  4. $0 sample-data       # Send test data"
    echo "  5. $0 start-sql         # Start with SQL Client"
    echo "  6. $0 sql-interactive   # Interactive SQL session"
    echo "  7. Open Flink Web UI and submit Java jobs"
    echo
    echo "📚 Course Resources:"
    echo "  - Course: https://developer.confluent.io/courses/apache-flink/"
    echo "  - Exercise 4: https://developer.confluent.io/courses/apache-flink/stream-processing-exercise/"
    echo "  - Flink Web UI: http://localhost:8082"
    echo "  - Kafka UI: http://localhost:8081"
    echo
}

# Main script logic
case "${1:-help}" in
    start-kafka)
        start_kafka
        ;;
    start-flink)
        start_flink
        ;;
    start-sql)
        start_flink_sql
        ;;
    start-connect)
        start_connect
        ;;
    start-course)
        start_flink_course
        ;;
    start-all)
        start_kafka
        start_flink
        create_topics
        print_success "Complete environment is ready!"
        show_status
        ;;
    build)
        build_project
        ;;
    deploy)
        deploy_connectors
        ;;
    topics)
        create_topics
        ;;
    sample-data)
        send_sample_data
        ;;
    sql-exercise)
        run_sql_exercise "${2:-streaming}"
        ;;
    sql-interactive)
        interactive_sql
        ;;
    sql-course)
        sql_course
        ;;
    run-course-exercise)
        run_course_exercise
        ;;
    status)
        show_status
        ;;
    stop)
        stop_all
        ;;
    clean)
        clean_all
        ;;
    help|*)
        show_help
        ;;
esac
