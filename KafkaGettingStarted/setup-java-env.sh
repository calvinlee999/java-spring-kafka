#!/bin/bash
# JAVA/MAVEN PROJECT ENVIRONMENT SETUP
# Use this for all Java/Spring Boot/Kafka projects

echo "🔧 Setting up Java development environment with jenv..."

# Ensure jenv is in PATH
export PATH="$HOME/.jenv/shims:$PATH"
export PATH="$HOME/.jenv/bin:$PATH"
export PATH="/opt/homebrew/bin:$PATH"

# Initialize jenv
if command -v jenv &> /dev/null; then
    eval "$(jenv init -)"
    export JAVA_HOME="$(jenv javahome)"
    echo "✅ Java environment configured:"
    echo "   Java version: $(java -version 2>&1 | head -1)"
    echo "   JAVA_HOME: $JAVA_HOME"
    echo "   Maven: $(mvn --version | head -1)"
else
    echo "❌ jenv not found. Please install jenv first."
    exit 1
fi

# Verify tools are available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven with: brew install maven"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker Desktop"
    exit 1
fi

echo "🚀 Java development environment ready!"
echo ""
echo "Available commands:"
echo "  mvn clean compile    - Compile the project"
echo "  mvn test            - Run all tests"
echo "  mvn spring-boot:run - Start the application"
echo "  docker-compose up   - Start Kafka containers"
