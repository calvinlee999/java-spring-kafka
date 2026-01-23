#!/bin/bash

# Kafka Learning Environment - Quick Start Script
# Based on Docker Desktop: 8 CPUs, 7.654GB RAM

set -e

KAFKA_COMPOSE_FILE="kafka-single-node.yml"
PROJECT_NAME="kafka-learning"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_usage() {
    echo -e "${BLUE}Kafka Learning Environment - Enterprise CLI${NC}"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  start [ui|monitoring]    Start Kafka (optionally with UI and/or monitoring)"
    echo "  stop                     Stop all services"
    echo "  restart                  Restart all services"
    echo "  status                   Show service status"
    echo "  logs [service]           Show logs (kafka, kafka-ui, kafka-exporter)"
    echo "  topic create <name>      Create a topic"
    echo "  topic list               List all topics"
    echo "  topic delete <name>      Delete a topic"
    echo "  consumer <topic>         Start console consumer"
    echo "  producer <topic>         Start console producer"
    echo "  clean                    Stop and remove all data (WARNING: destroys data)"
    echo "  dev                      Start in development mode"
    echo ""
    echo "Examples:"
    echo "  $0 start ui              # Start Kafka with web UI"
    echo "  $0 start ui monitoring   # Start with UI and monitoring"
    echo "  $0 topic create orders   # Create 'orders' topic"
    echo "  $0 consumer orders       # Start consuming from 'orders' topic"
    echo ""
}

wait_for_kafka() {
    echo -e "${YELLOW}Waiting for Kafka to be ready...${NC}"
    while ! docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 >/dev/null 2>&1; do
        sleep 2
        echo -n "."
    done
    echo -e "${GREEN}Kafka is ready!${NC}"
}

case "$1" in
    start)
        profiles=""
        shift
        while [[ $# -gt 0 ]]; do
            case $1 in
                ui) profiles="$profiles --profile ui" ;;
                monitoring) profiles="$profiles --profile monitoring" ;;
                *) echo -e "${RED}Unknown profile: $1${NC}"; exit 1 ;;
            esac
            shift
        done
        
        echo -e "${GREEN}Starting Kafka Learning Environment...${NC}"
        docker-compose -f $KAFKA_COMPOSE_FILE $profiles up -d
        wait_for_kafka
        
        echo -e "${GREEN}✅ Kafka is running!${NC}"
        echo ""
        echo "Connection details:"
        echo "  Bootstrap servers: localhost:9092"
        if [[ $profiles == *"ui"* ]]; then
            echo "  Web UI: http://localhost:8081"
        fi
        if [[ $profiles == *"monitoring"* ]]; then
            echo "  Metrics: http://localhost:9308/metrics"
        fi
        ;;
        
    dev)
        echo -e "${GREEN}Starting Kafka in Development Mode...${NC}"
        docker-compose -f $KAFKA_COMPOSE_FILE -f docker-compose.dev.yml --profile ui up -d
        wait_for_kafka
        echo -e "${GREEN}✅ Kafka development environment is ready!${NC}"
        echo "  Bootstrap servers: localhost:9092"
        echo "  Web UI: http://localhost:8081"
        echo "  Internal port (debugging): localhost:29092"
        ;;
        
    stop)
        echo -e "${YELLOW}Stopping Kafka Learning Environment...${NC}"
        docker-compose -f $KAFKA_COMPOSE_FILE --profile ui --profile monitoring down
        echo -e "${GREEN}✅ Stopped successfully${NC}"
        ;;
        
    restart)
        echo -e "${YELLOW}Restarting Kafka Learning Environment...${NC}"
        docker-compose -f $KAFKA_COMPOSE_FILE --profile ui --profile monitoring restart
        wait_for_kafka
        echo -e "${GREEN}✅ Restarted successfully${NC}"
        ;;
        
    status)
        echo -e "${BLUE}Kafka Learning Environment Status:${NC}"
        docker-compose -f $KAFKA_COMPOSE_FILE ps
        echo ""
        echo -e "${BLUE}Resource Usage:${NC}"
        docker stats --no-stream kafka-learning-broker kafka-learning-ui kafka-learning-exporter 2>/dev/null || true
        ;;
        
    logs)
        service=${2:-kafka}
        case $service in
            kafka) container="kafka-learning-broker" ;;
            ui) container="kafka-learning-ui" ;;
            exporter) container="kafka-learning-exporter" ;;
            *) echo -e "${RED}Unknown service: $service${NC}"; exit 1 ;;
        esac
        docker logs -f $container
        ;;
        
    topic)
        case "$2" in
            create)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    exit 1
                fi
                echo -e "${GREEN}Creating topic: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 \
                    --create --topic "$3" \
                    --partitions 6 --replication-factor 1
                ;;
            list)
                echo -e "${BLUE}Available topics:${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 --list
                ;;
            delete)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    exit 1
                fi
                echo -e "${YELLOW}Deleting topic: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 \
                    --delete --topic "$3"
                ;;
            *)
                echo -e "${RED}Topic command required: create, list, or delete${NC}"
                exit 1
                ;;
        esac
        ;;
        
    consumer)
        if [[ -z "$2" ]]; then
            echo -e "${RED}Topic name required${NC}"
            exit 1
        fi
        echo -e "${GREEN}Starting consumer for topic: $2${NC}"
        echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
        docker exec -it kafka-learning-broker /opt/bitnami/kafka/bin/kafka-console-consumer.sh \
            --bootstrap-server localhost:9092 \
            --topic "$2" \
            --from-beginning
        ;;
        
    producer)
        if [[ -z "$2" ]]; then
            echo -e "${RED}Topic name required${NC}"
            exit 1
        fi
        echo -e "${GREEN}Starting producer for topic: $2${NC}"
        echo -e "${YELLOW}Type messages and press Enter. Press Ctrl+C to stop${NC}"
        docker exec -it kafka-learning-broker /opt/bitnami/kafka/bin/kafka-console-producer.sh \
            --bootstrap-server localhost:9092 \
            --topic "$2"
        ;;
        
    clean)
        echo -e "${RED}⚠️  WARNING: This will delete all Kafka data!${NC}"
        echo -n "Are you sure? (y/N): "
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            echo -e "${YELLOW}Cleaning up Kafka environment...${NC}"
            docker-compose -f $KAFKA_COMPOSE_FILE --profile ui --profile monitoring down -v --remove-orphans
            echo -e "${GREEN}✅ Cleanup completed${NC}"
        else
            echo -e "${BLUE}Cleanup cancelled${NC}"
        fi
        ;;
        
    # === CLI TOOLS SECTION ===
    cli)
        case "$2" in
            topics)
                echo -e "${BLUE}Kafka Topics CLI${NC}"
                echo "Available commands:"
                echo "  ./kafka-ctl.sh cli topics list"
                echo "  ./kafka-ctl.sh cli topics create <topic> [partitions] [replication]"
                echo "  ./kafka-ctl.sh cli topics delete <topic>"
                echo "  ./kafka-ctl.sh cli topics describe <topic>"
                echo "  ./kafka-ctl.sh cli topics config <topic>"
                ;;
            consumer-groups)
                echo -e "${BLUE}Kafka Consumer Groups CLI${NC}"
                echo "Available commands:"
                echo "  ./kafka-ctl.sh cli consumer-groups list"
                echo "  ./kafka-ctl.sh cli consumer-groups describe <group>"
                echo "  ./kafka-ctl.sh cli consumer-groups reset <group> <topic>"
                ;;
            configs)
                echo -e "${BLUE}Kafka Configs CLI${NC}"
                echo "Available commands:"
                echo "  ./kafka-ctl.sh cli configs broker"
                echo "  ./kafka-ctl.sh cli configs topic <topic>"
                ;;
            acls)
                echo -e "${BLUE}Kafka ACLs CLI${NC}"
                echo "Available commands:"
                echo "  ./kafka-ctl.sh cli acls list"
                echo "  ./kafka-ctl.sh cli acls add <resource> <operation> <principal>"
                ;;
            *)
                echo -e "${BLUE}Kafka CLI Tools${NC}"
                echo ""
                echo "Usage: ./kafka-ctl.sh cli <command> [options]"
                echo ""
                echo "Available CLI modules:"
                echo "  topics           Topic management"
                echo "  consumer-groups  Consumer group operations"
                echo "  configs          Configuration management"
                echo "  acls             Access Control Lists"
                echo "  performance      Performance testing"
                echo ""
                echo "Examples:"
                echo "  ./kafka-ctl.sh cli topics"
                echo "  ./kafka-ctl.sh cli consumer-groups"
                ;;
        esac
        ;;
        
    # Enhanced topic management with CLI options
    topics)
        case "$2" in
            create)
                topic_name="$3"
                partitions="${4:-6}"
                replication="${5:-1}"
                
                if [[ -z "$topic_name" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    echo "Usage: ./kafka-ctl.sh topics create <name> [partitions] [replication]"
                    exit 1
                fi
                
                echo -e "${GREEN}Creating topic: $topic_name (partitions: $partitions, replication: $replication)${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 \
                    --create --topic "$topic_name" \
                    --partitions "$partitions" \
                    --replication-factor "$replication" \
                    --config compression.type=snappy \
                    --config min.insync.replicas=1
                ;;
            list)
                echo -e "${BLUE}Available topics:${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 --list
                ;;
            describe)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    echo "Usage: ./kafka-ctl.sh topics describe <name>"
                    exit 1
                fi
                echo -e "${BLUE}Topic details for: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 \
                    --describe --topic "$3"
                ;;
            delete)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    echo "Usage: ./kafka-ctl.sh topics delete <name>"
                    exit 1
                fi
                echo -e "${YELLOW}Deleting topic: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-topics.sh \
                    --bootstrap-server localhost:9092 \
                    --delete --topic "$3"
                ;;
            config)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    echo "Usage: ./kafka-ctl.sh topics config <name>"
                    exit 1
                fi
                echo -e "${BLUE}Configuration for topic: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-configs.sh \
                    --bootstrap-server localhost:9092 \
                    --entity-type topics --entity-name "$3" \
                    --describe
                ;;
            *)
                echo -e "${BLUE}Topic Management Commands:${NC}"
                echo ""
                echo "Usage: ./kafka-ctl.sh topics <command> [options]"
                echo ""
                echo "Commands:"
                echo "  create <name> [partitions] [replication]  Create a new topic"
                echo "  list                                      List all topics"
                echo "  describe <name>                          Show topic details"
                echo "  delete <name>                            Delete a topic"
                echo "  config <name>                            Show topic configuration"
                echo ""
                echo "Examples:"
                echo "  ./kafka-ctl.sh topics create orders 12 1"
                echo "  ./kafka-ctl.sh topics describe orders"
                ;;
        esac
        ;;
        
    # Consumer groups management
    groups)
        case "$2" in
            list)
                echo -e "${BLUE}Consumer Groups:${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-consumer-groups.sh \
                    --bootstrap-server localhost:9092 --list
                ;;
            describe)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Group name required${NC}"
                    echo "Usage: ./kafka-ctl.sh groups describe <group>"
                    exit 1
                fi
                echo -e "${BLUE}Consumer group details: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-consumer-groups.sh \
                    --bootstrap-server localhost:9092 \
                    --group "$3" --describe
                ;;
            reset)
                if [[ -z "$3" || -z "$4" ]]; then
                    echo -e "${RED}Group and topic names required${NC}"
                    echo "Usage: ./kafka-ctl.sh groups reset <group> <topic>"
                    exit 1
                fi
                echo -e "${YELLOW}Resetting consumer group $3 for topic $4${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-consumer-groups.sh \
                    --bootstrap-server localhost:9092 \
                    --group "$3" --topic "$4" \
                    --reset-offsets --to-earliest --execute
                ;;
            *)
                echo -e "${BLUE}Consumer Groups Management:${NC}"
                echo ""
                echo "Commands:"
                echo "  list                    List all consumer groups"
                echo "  describe <group>        Show group details"
                echo "  reset <group> <topic>   Reset group offsets to earliest"
                ;;
        esac
        ;;
        
    # Configuration management
    config)
        case "$2" in
            broker)
                echo -e "${BLUE}Broker Configuration:${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-configs.sh \
                    --bootstrap-server localhost:9092 \
                    --entity-type brokers --entity-name 1 \
                    --describe
                ;;
            topic)
                if [[ -z "$3" ]]; then
                    echo -e "${RED}Topic name required${NC}"
                    echo "Usage: ./kafka-ctl.sh config topic <name>"
                    exit 1
                fi
                echo -e "${BLUE}Topic Configuration: $3${NC}"
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-configs.sh \
                    --bootstrap-server localhost:9092 \
                    --entity-type topics --entity-name "$3" \
                    --describe
                ;;
            *)
                echo -e "${BLUE}Configuration Management:${NC}"
                echo ""
                echo "Commands:"
                echo "  broker              Show broker configuration"
                echo "  topic <name>        Show topic configuration"
                ;;
        esac
        ;;
        
    # Performance testing
    perf)
        case "$2" in
            producer)
                topic="${3:-perf-test}"
                records="${4:-10000}"
                record_size="${5:-1024}"
                
                echo -e "${GREEN}Running producer performance test${NC}"
                echo "Topic: $topic, Records: $records, Size: $record_size bytes"
                
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-producer-perf-test.sh \
                    --topic "$topic" \
                    --num-records "$records" \
                    --record-size "$record_size" \
                    --throughput -1 \
                    --producer-props bootstrap.servers=localhost:9092
                ;;
            consumer)
                topic="${3:-perf-test}"
                messages="${4:-10000}"
                
                echo -e "${GREEN}Running consumer performance test${NC}"
                echo "Topic: $topic, Messages: $messages"
                
                docker exec kafka-learning-broker /opt/bitnami/kafka/bin/kafka-consumer-perf-test.sh \
                    --topic "$topic" \
                    --messages "$messages" \
                    --bootstrap-server localhost:9092
                ;;
            *)
                echo -e "${BLUE}Performance Testing:${NC}"
                echo ""
                echo "Commands:"
                echo "  producer [topic] [records] [size]    Run producer performance test"
                echo "  consumer [topic] [messages]          Run consumer performance test"
                echo ""
                echo "Examples:"
                echo "  ./kafka-ctl.sh perf producer test-topic 50000 512"
                echo "  ./kafka-ctl.sh perf consumer test-topic 50000"
                ;;
        esac
        ;;
        
    # Raw CLI access
    exec)
        if [[ -z "$2" ]]; then
            echo -e "${RED}Command required${NC}"
            echo "Usage: ./kafka-ctl.sh exec <kafka-command> [args...]"
            echo ""
            echo "Examples:"
            echo "  ./kafka-ctl.sh exec /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list"
            echo "  ./kafka-ctl.sh exec /opt/bitnami/kafka/bin/kafka-console-consumer.sh --topic test --bootstrap-server localhost:9092"
            exit 1
        fi
        
        # Remove 'exec' from arguments and pass the rest to docker exec
        shift
        echo -e "${BLUE}Executing: $@${NC}"
        docker exec -it kafka-learning-broker "$@"
        ;;

    *)
        print_usage
        exit 1
        ;;
esac
