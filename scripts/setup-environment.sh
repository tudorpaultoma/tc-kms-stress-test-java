#!/usr/bin/env bash
#
# Environment Setup Script for KMS Stress Tester
#
# This script verifies that all required tools are installed and builds the project.
# Run this once before executing the stress test for the first time.
#

echo "Setting up KMS Stress Tester environment..."

# Verify Java is installed (JDK 11 or later required)
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11 or later"
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

# Verify Maven is installed (3.6 or later recommended)
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6 or later"
    exit 1
fi
echo "✅ Maven found: $(mvn -version | head -n 1)"

# Build the project
echo "Building project..."
mvn clean package

if [ $? -eq 0 ]; then
    echo "✅ Setup complete! You can now run: ./scripts/stress-kms.sh"
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi