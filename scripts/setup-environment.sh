#!/usr/bin/env bash
# Environment setup script for KMS stress tester

echo "Setting up KMS Stress Tester environment..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "Java not found. Please install Java 11+"
    exit 1
fi

# Check Maven  
if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Please install Maven 3.6+"
    exit 1
fi

# Build project
echo "Building project..."
mvn clean package

echo "Setup complete! Run: ./scripts/stress-kms.sh"