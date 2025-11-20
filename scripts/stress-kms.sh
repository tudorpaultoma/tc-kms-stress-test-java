#!/usr/bin/env bash
#
# Tencent Cloud KMS Stress Test Runner
# 
# This script launches the KMS stress testing application with configurable parameters.
# It builds the project if needed and runs the stress test with the specified configuration.
#

set -e

# Project paths - set these to match your project structure
PROJECT_ROOT=""  # Set this to the root of your project
JAR_PATH="${PROJECT_ROOT}/target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar"

# Test configuration - override via environment variables
REGION="eu-frankfurt"                      # Tencent Cloud region
ROLE="CVM-KMS-full"                        # CVM role name with KMS permissions
CMK_ID="${CMK_ID:-}"                       # KMS Customer Master Key ID (auto-create if empty)
DURATION_SECONDS="${DURATION_SECONDS:-60}" # Test duration in seconds
CONCURRENCY="${CONCURRENCY:-500}"          # Number of concurrent worker threads
ENCRYPT_RATIO="${ENCRYPT_RATIO:-0.2}"      # Ratio of encryption operations (0.0-1.0)
DECRYPT_RATIO="${DECRYPT_RATIO:-0.8}"      # Ratio of decryption operations (0.0-1.0)

echo "=== Tencent Cloud KMS Stress Tester ==="
echo "JAR_PATH: $JAR_PATH"
echo "Region: $REGION"
echo "Role: $ROLE"
echo "CMK ID: ${CMK_ID:-[Auto-create temporary key]}"
echo "Duration: ${DURATION_SECONDS}s"
echo "Concurrency: $CONCURRENCY"
echo "Encrypt/Decrypt Ratio: $ENCRYPT_RATIO/$DECRYPT_RATIO"

# Build project if JAR doesn't exist
if [ ! -f "$JAR_PATH" ]; then
    echo "Building project..."
    mvn -f "$PROJECT_ROOT/pom.xml" clean package
fi

# Run the stress test with configured parameters
# Memory settings: -Xms512m (initial heap), -Xmx1g (max heap)
java -Xms512m -Xmx1g \
  -Dregion="$REGION" \
  -Drole="$ROLE" \
  -DcmkId="$CMK_ID" \
  -Dduration="$DURATION_SECONDS" \
  -Dconcurrency="$CONCURRENCY" \
  -DencryptRatio="$ENCRYPT_RATIO" \
  -DdecryptRatio="$DECRYPT_RATIO" \
  -jar "$JAR_PATH"