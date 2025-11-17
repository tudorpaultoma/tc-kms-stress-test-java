#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR_PATH="${PROJECT_ROOT}/target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar"

# Default configuration
REGION="${REGION:-ap-guangzhou}"
ROLE="${ROLE:-CVM-KMS-full}"
CMK_ID="${CMK_ID:-your-cmk-id-here}"
DURATION_SECONDS="${DURATION_SECONDS:-60}"
CONCURRENCY="${CONCURRENCY:-20}"

echo "=== Tencent Cloud KMS Stress Tester ==="
echo "JAR_PATH: $JAR_PATH"
echo "Region: $REGION"
echo "Role: $ROLE"
echo "CMK ID: $CMK_ID"
echo "Duration: ${DURATION_SECONDS}s"
echo "Concurrency: $CONCURRENCY"
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
  -jar "$JAR_PATH"