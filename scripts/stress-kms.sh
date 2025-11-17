#!/usr/bin/env bash

set -e

PROJECT_ROOT="" # Set this to the root of your project
JAR_PATH="${PROJECT_ROOT}/target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar"  # Set this to the path of your JAR file

# Default configuration
REGION="eu-frankfurt"
ROLE="CVM-KMS-full"
CMK_ID="${CMK_ID:-}"  # Empty to create temporary key
DURATION_SECONDS="${DURATION_SECONDS:-60}"
CONCURRENCY="${CONCURRENCY:-500}"
ENCRYPT_RATIO="${ENCRYPT_RATIO:-0.2}"    # NEW: 50% encryption
DECRYPT_RATIO="${DECRYPT_RATIO:-0.8}"    # NEW: 50% decryption

echo "=== Tencent Cloud KMS Stress Tester ==="
echo "JAR_PATH: $JAR_PATH"
echo "Region: $REGION"
echo "Role: $ROLE"
echo "CMK ID: ${CMK_ID:-[Auto-create temporary key]}"
echo "Duration: ${DURATION_SECONDS}s"
echo "Concurrency: $CONCURRENCY"
echo "Encrypt/Decrypt Ratio: $ENCRYPT_RATIO/$DECRYPT_RATIO"
echo "----------------------------------------"

# Build if JAR doesn't exist
if [ ! -f "$JAR_PATH" ]; then
    echo "Building project..."
    mvn -f "$PROJECT_ROOT/pom.xml" clean package
fi

# Run stress test
java -Xms512m -Xmx1g \
  -Dregion="$REGION" \
  -Drole="$ROLE" \
  -DcmkId="$CMK_ID" \
  -Dduration="$DURATION_SECONDS" \
  -Dconcurrency="$CONCURRENCY" \
  -DencryptRatio="$ENCRYPT_RATIO" \
  -DdecryptRatio="$DECRYPT_RATIO" \
  -jar "$JAR_PATH"