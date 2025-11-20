# Tencent Cloud KMS Stress Tester

A high-performance Java-based stress testing tool for Tencent Cloud Key Management Service (KMS). This tool measures encryption/decryption performance under concurrent load, helping you understand KMS performance limits, test scalability, and validate your configuration.

## 🚀 Features

- ✅ **Concurrent KMS Operations** - Configurable number of parallel worker threads
- ✅ **Mixed Operation Testing** - Configurable ratio of encryption/decryption operations
- ✅ **Real-time Performance Monitoring** - Live RPS (requests per second) tracking
- ✅ **Peak Performance Tracking** - Automatic detection of maximum throughput
- ✅ **Comprehensive Statistics** - Detailed success rates, error analysis, and latency metrics
- ✅ **Automatic Key Management** - Optional temporary key creation and cleanup
- ✅ **CVM Role Integration** - Seamless authentication via CVM metadata service
- ✅ **Cross-Platform Support** - Runs on any system with Java 11+
- ✅ **Configurable Test Parameters** - Flexible configuration via environment variables or system properties

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#️-configuration)
- [Usage](#-usage)
- [Understanding Results](#-understanding-results)
- [Advanced Configuration](#-advanced-configuration)
- [Troubleshooting](#-troubleshooting)

## 🛠 Prerequisites

### System Requirements
- **Java**: JDK 11 or later
- **Maven**: 3.6 or later
- **Operating System**: Linux, macOS, or Windows
- **Memory**: 512MB RAM minimum, 1GB+ recommended for high concurrency

### Tencent Cloud Requirements
- **Tencent Cloud Account**
- **CVM Instance** running in your target region
- **CVM Role** with appropriate KMS permissions (see below)
- **KMS Customer Master Key (CMK)** - optional, will be auto-created if not provided

### Required CVM Role Permissions

Your CVM role must have the following KMS permissions:

```json
{
    "version": "2.0",
    "statement": [
        {
            "effect": "allow",
            "action": [
                "kms:Encrypt",
                "kms:Decrypt",
                "kms:CreateKey",
                "kms:DisableKey",
                "kms:ScheduleKeyDeletion"
            ],
            "resource": "*"
        }
    ]
}
```

**Note**: `CreateKey`, `DisableKey`, and `ScheduleKeyDeletion` are only needed if you want the tool to automatically create and cleanup temporary keys.

## 📥 Installation

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd tc-kms-stress-test-java
```

### 2. Run Setup Script

```bash
chmod +x scripts/setup-environment.sh
./scripts/setup-environment.sh
```

This will:
- Verify Java and Maven are installed
- Build the project and create the executable JAR
- Confirm the environment is ready

### 3. Verify Installation

```bash
ls -la target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You should see the JAR file (approximately 10-15 MB).

## ⚙️ Configuration

### Quick Start Configuration

Edit `scripts/stress-kms.sh` to set your test parameters:

```bash
REGION="eu-frankfurt"           # Your Tencent Cloud region
ROLE="CVM-KMS-full"             # Your CVM role name
CMK_ID=""                       # Leave empty to auto-create, or specify your CMK ID
DURATION_SECONDS="60"           # Test duration (seconds)
CONCURRENCY="20"                # Number of concurrent workers
ENCRYPT_RATIO="0.5"             # 50% encryption operations
DECRYPT_RATIO="0.5"             # 50% decryption operations
```

### Configuration Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `REGION` | `ap-guangzhou` | Tencent Cloud region where KMS is accessed |
| `ROLE` | `CVM-KMS-full` | CVM role name for obtaining temporary credentials |
| `CMK_ID` | _(empty)_ | Customer Master Key ID. If empty, a temporary key is created |
| `DURATION_SECONDS` | `60` | How long the stress test runs (in seconds) |
| `CONCURRENCY` | `20` | Number of concurrent worker threads |
| `ENCRYPT_RATIO` | `0.5` | Proportion of encryption operations (0.0 to 1.0) |
| `DECRYPT_RATIO` | `0.5` | Proportion of decryption operations (0.0 to 1.0) |

### Environment Variable Override

You can override configuration without editing the script:

```bash
export CMK_ID="your-cmk-id-here"
export DURATION_SECONDS="120"
export CONCURRENCY="50"
export ENCRYPT_RATIO="0.3"
export DECRYPT_RATIO="0.7"
./scripts/stress-kms.sh
```

## 🚀 Usage

### Basic Usage

```bash
# Run with default settings
./scripts/stress-kms.sh
```

### Custom Configuration Examples

```bash
# High concurrency test (100 workers, 5 minutes)
CONCURRENCY=100 DURATION_SECONDS=300 ./scripts/stress-kms.sh

# Encryption-heavy test (80% encrypt, 20% decrypt)
ENCRYPT_RATIO=0.8 DECRYPT_RATIO=0.2 ./scripts/stress-kms.sh

# Use existing CMK
CMK_ID="your-cmk-id" ./scripts/stress-kms.sh

# Different region
REGION="ap-singapore" ./scripts/stress-kms.sh
```

### Running Directly with Java

You can also run the JAR directly:

```bash
java -Xms512m -Xmx1g \
  -Dregion="eu-frankfurt" \
  -Drole="CVM-KMS-full" \
  -DcmkId="" \
  -Dduration="60" \
  -Dconcurrency="20" \
  -DencryptRatio="0.5" \
  -DdecryptRatio="0.5" \
  -jar target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 📊 Understanding Results

### Sample Output

```
=== Tencent Cloud KMS Stress Tester ===
Region: eu-frankfurt, Role: CVM-KMS-full
Duration: 60s, Concurrency: 55, Encrypt/Decrypt Ratio: 0.5/0.5

✅ Key created successfully!
Key ID: 83b8885f-c400-11f0-8d87-525400d81ea5

STATS - Encrypt: 6661 (1332.20RPS) | Decrypt: 6623 (1324.60RPS) | Total: 13284 (2656.80RPS)
STATS - Encrypt: 13517 (1371.20RPS) | Decrypt: 13375 (1350.40RPS) | Total: 26892 (2721.60RPS)
...

=========================================
STRESS TEST COMPLETE
Test Key: 83b8885f-c400-11f0-8d87-525400d81ea5
Operation Ratio: 0.5/0.5 (Config) | 0.5/0.5 (Actual)
=========================================
Duration: 60.02 seconds
Total Requests: 160222
Successful: 160222 (100.00%)
Errors: 0
Encryption Operations: 80233 (1336.86 RPS)
Decryption Operations: 79989 (1332.79 RPS)
Overall RPS: 2669.65
Peak Total RPS: 2770.20
Peak Encrypt RPS: 1375.40
Peak Decrypt RPS: 1394.80
Average Latency: 20.59 ms
=========================================
```

### Key Metrics Explained

- **Overall RPS**: Total requests per second (encrypt + decrypt)
- **Peak RPS**: Maximum throughput achieved during the test
- **Average Latency**: Mean time per operation in milliseconds
- **Success Rate**: Percentage of successful operations
- **Operation Ratio**: Shows configured vs. actual distribution of operations

## 🔧 Advanced Configuration

### Tuning for Maximum Performance

For high-throughput testing, consider:

```bash
# Increase JVM heap size for high concurrency
java -Xms1g -Xmx2g \
  -Dconcurrency="200" \
  -Dduration="300" \
  -jar target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Testing Specific Scenarios

**Encryption-only test:**
```bash
ENCRYPT_RATIO=1.0 DECRYPT_RATIO=0.0 ./scripts/stress-kms.sh
```

**Decryption-only test:**
```bash
ENCRYPT_RATIO=0.0 DECRYPT_RATIO=1.0 ./scripts/stress-kms.sh
```

**Long-running stability test:**
```bash
DURATION_SECONDS=3600 CONCURRENCY=30 ./scripts/stress-kms.sh  # 1 hour
```

## 🔍 Troubleshooting

### Common Issues

**"Failed to get credentials from metadata service"**
- Ensure you're running on a Tencent Cloud CVM instance
- Verify the CVM role is attached to your instance
- Check the role name in the configuration matches your CVM role

**"Metadata service returned error: 404"**
- The specified role name doesn't exist or isn't attached to the CVM
- Verify role name: `curl http://metadata.tencentyun.com/latest/meta-data/cam/security-credentials/`

**High error rate during test**
- You may be hitting KMS API rate limits
- Reduce `CONCURRENCY` value
- Check KMS service quotas in your Tencent Cloud console

**Out of memory errors**
- Increase JVM heap size: `-Xmx2g` or higher
- Reduce `CONCURRENCY` value
- Ensure sufficient system memory

**Temporary key not deleted**
- Check CloudWatch/logs for cleanup errors
- Manually delete from KMS console if needed
- The key is only scheduled for deletion (7-day minimum)

## 📈 Performance Guidelines

### Recommended Concurrency by Instance Type

| Instance Type | Recommended Concurrency |
|---------------|------------------------|
| 2C4G | 10-20 workers |
| 4C8G | 20-50 workers |
| 8C16G | 50-100 workers |
| 16C32G | 100-200 workers |

### Expected Performance

Performance varies by region, KMS plan, and network conditions:
- **Professional Plan**: 2000-3000+ RPS
- **Standard Plan**: 500-1000 RPS
- **Latency**: Typically 15-30ms per operation

## 📝 License

See [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## 📧 Support

For issues related to:
- **This tool**: Open an issue in this repository
- **Tencent Cloud KMS**: Contact Tencent Cloud support

