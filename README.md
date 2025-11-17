# Tencent Cloud KMS Stress Tester

A high-performance stress testing tool for Tencent Cloud Key Management Service (KMS) that measures encryption/decryption performance under load. This tool helps you understand your KMS performance limits, test scalability, and validate your KMS configuration.

## 🚀 Features

- ✅ Concurrent KMS operations testing
- ✅ Real-time RPS monitoring  
- ✅ Peak performance tracking
- ✅ Error rate analysis
- ✅ Configurable test parameters
- ✅ Temporary credential support via CVM roles
- ✅ Cross Platform - Runs on any system with Java 11+§


## 🛠 Prerequisites

### System Requirements
- **Java**: JDK 11 or later
- **Maven**: 3.6 or later
- **Operating System**: Linux, macOS, or Windows
- **Memory**: 512MB RAM minimum, 1GB+ recommended

### Tencent Cloud Requirements
- Tencent Cloud Account
- CVM Instance in target region
- KMS Customer Master Key (CMK)
- CVM Role with KMS permissions

### Required CVM Role Permissions
```json
{
    "version": "2.0",
    "statement": [
        {
            "effect": "allow",
            "action": [
                "kms:Encrypt",
                "kms:Decrypt", 
                "kms:GenerateDataKey"
            ],
            "resource": "*"
        }
    ]
}

## Quick Start

```bash
# Clone and build
git clone https://github.com/yourusername/kms-stress-tester.git
cd kms-stress-tester

# Run stress test
./scripts/stress-kms.sh

## 📥 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/kms-stress-tester.git
cd kms-stress-tester

### 2. Build the Project

```bash
# Build the executable JAR with all dependencies
mvn clean package

### 3. Verify Installation

```bash
# Check if JAR was created successfully
ls -la target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar

# Test basic functionality
java -jar target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar --help || echo "Build successful"

## ⚙️ Configuration


**Environment Variables**
Configure test parameters using environment variables:

```bash
# Required: Set your KMS CMK ID
export CMK_ID="your-cmk-id-here"

# Optional: Customize test parameters
export REGION="ap-guangzhou"
export ROLE="CVM-KMS-full"
export DURATION_SECONDS="60"
export CONCURRENCY="50"

**Configuration Parameters**

REGION	ap-guangzhou	Tencent Cloud region for KMS
ROLE	CVM-KMS-full	CVM role name for credentials
CMK_ID	Required	Your KMS Customer Master Key ID
DURATION_SECONDS	60	Test duration in seconds
CONCURRENCY	20	Number of concurrent workers

