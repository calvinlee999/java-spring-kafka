#!/bin/bash

# MacBook Environment Setup for Java/Kafka Development
echo "🔧 Setting up MacBook Environment for Java/Kafka Development"
echo "============================================================="

# Set Java 17 as the active version
export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Add Homebrew to PATH (for Maven)
export PATH="/opt/homebrew/bin:$PATH"

echo "✅ Java 17 configured as active version"
echo "✅ Maven path configured"
echo "✅ Homebrew path configured"

echo ""
echo "📋 Environment Summary:"
echo "----------------------"
echo "JAVA_HOME: $JAVA_HOME"
echo "Java Version: $(java -version 2>&1 | head -1)"
echo "Maven Version: $(mvn -version 2>/dev/null | head -1 || echo 'Maven not found')"
echo "Docker Version: $(docker --version 2>/dev/null || echo 'Docker not found')"

echo ""
echo "🚀 To make these changes permanent, add these lines to your ~/.zshrc:"
echo "export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home"
echo "export PATH=\$JAVA_HOME/bin:/opt/homebrew/bin:\$PATH"

echo ""
echo "📝 To apply now, run: source ~/.zshrc"
