## 📊 Example Test Results

### Optimal Performance Test (Professional Plan)

This example demonstrates excellent performance with 55 concurrent workers on a Professional Plan KMS instance.

#### Test Configuration
```bash

root@VM-13-37-centos:~# ./stress-kms.sh
=== Tencent Cloud KMS Stress Tester ===
JAR_PATH: /root/kms1/target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar
Region: eu-frankfurt
Role: CVM-KMS-full
CMK ID: [Auto-create temporary key]
Duration: 60s
Concurrency: 55
Encrypt/Decrypt Ratio: 0.5/0.5
----------------------------------------
[main] INFO com.tencent.app.Main - Starting KMS Stress Test
[main] INFO com.tencent.app.Main - Region: eu-frankfurt, Role: CVM-KMS-full
[main] INFO com.tencent.app.Main - Duration: 60s, Concurrency: 55, Encrypt/Decrypt Ratio: 0.5/0.5
[main] INFO com.tencent.app.MetadataCredentialClient - Successfully obtained temporary credentials for role: CVM-KMS-full
[main] INFO com.tencent.app.Main - No CMK_ID provided, creating temporary key for testing...
[main] INFO com.tencent.app.Main - Creating temporary KMS key...
[main] INFO com.tencent.app.Main - Key creation parameters:
[main] INFO com.tencent.app.Main -   Alias: stress-test-key-1763416692744
[main] INFO com.tencent.app.Main -   Region: eu-frankfurt
[main] INFO com.tencent.app.Main -   Key Usage: ENCRYPT_DECRYPT
[main] INFO com.tencent.app.Main - ✅ Key created successfully!
[main] INFO com.tencent.app.Main - ┌─────────────────────────────────────────────────────
[main] INFO com.tencent.app.Main - │ Key ID:      83b8885f-c400-11f0-8d87-525400d81ea5
[main] INFO com.tencent.app.Main - │ Alias:       stress-test-key-1763416692744
[main] INFO com.tencent.app.Main - │ Region:      eu-frankfurt
[main] INFO com.tencent.app.Main - │ Type:        Customer Master Key (CMK)
[main] INFO com.tencent.app.Main - │ Usage:       ENCRYPT_DECRYPT
[main] INFO com.tencent.app.Main - │ Creation:    1021 ms
[main] INFO com.tencent.app.Main - │ Status:      ✅ Enabled
[main] INFO com.tencent.app.Main - └─────────────────────────────────────────────────────
[main] INFO com.tencent.app.Main - This key will be automatically disabled and scheduled for
[main] INFO com.tencent.app.Main - deletion after the stress test completes.
[main] INFO com.tencent.app.Main - Waiting for threads to start: 0/55
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 0 (0.00RPS) | Decrypt: 0 (0.00RPS) | Total: 0 (0.00RPS) | Errors: 0 | Active: 0 | Ratio: 0.5/0.5
[main] INFO com.tencent.app.Main - Waiting for threads to start: 0/55
[main] INFO com.tencent.app.Main - Waiting for threads to start: 0/55
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 0 (0.00RPS) | Decrypt: 0 (0.00RPS) | Total: 0 (0.00RPS) | Errors: 0 | Active: 0 | Ratio: 0.5/0.5
[main] INFO com.tencent.app.Main - Waiting for threads to start: 0/55
[main] WARN com.tencent.app.Main - Only 0/55 threads started successfully
[main] INFO com.tencent.app.Main - All workers started. Beginning stress test in 3 seconds...
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 0 (0.00RPS) | Decrypt: 0 (0.00RPS) | Total: 0 (0.00RPS) | Errors: 0 | Active: 0 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 6661 (1332.20RPS) | Decrypt: 6623 (1324.60RPS) | Total: 13284 (2656.80RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 13517 (1371.20RPS) | Decrypt: 13375 (1350.40RPS) | Total: 26892 (2721.60RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 20138 (1324.20RPS) | Decrypt: 19954 (1315.80RPS) | Total: 40092 (2640.00RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 27015 (1375.40RPS) | Decrypt: 26928 (1394.80RPS) | Total: 53943 (2770.20RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 33831 (1363.20RPS) | Decrypt: 33756 (1365.60RPS) | Total: 67587 (2728.80RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 40453 (1324.40RPS) | Decrypt: 40380 (1324.80RPS) | Total: 80833 (2649.20RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 46755 (1260.40RPS) | Decrypt: 46643 (1252.60RPS) | Total: 93398 (2513.00RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 53429 (1334.80RPS) | Decrypt: 53215 (1314.40RPS) | Total: 106644 (2649.20RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 59853 (1284.80RPS) | Decrypt: 59817 (1320.40RPS) | Total: 119670 (2605.20RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 66573 (1344.00RPS) | Decrypt: 66429 (1322.40RPS) | Total: 133002 (2666.40RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 73332 (1351.80RPS) | Decrypt: 73199 (1354.00RPS) | Total: 146531 (2705.80RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[pool-1-thread-1] INFO com.tencent.app.Main - STATS - Encrypt: 80190 (1371.60RPS) | Decrypt: 79952 (1350.60RPS) | Total: 160142 (2722.20RPS) | Errors: 0 | Active: 55 | Ratio: 0.5/0.5
[main] INFO com.tencent.app.Main - Stress test duration completed. Stopping workers...
[main] INFO com.tencent.app.Main - =========================================
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
[main] INFO com.tencent.app.Main - Cleaning up temporary key: 83b8885f-c400-11f0-8d87-525400d81ea5
[main] INFO com.tencent.app.Main - Initiating key cleanup for: 83b8885f-c400-11f0-8d87-525400d81ea5
[main] INFO com.tencent.app.Main - Step 1/2: Disabling key...
[main] INFO com.tencent.app.Main - ✅ Key disabled successfully
[main] INFO com.tencent.app.Main - Step 2/2: Scheduling key for deletion...
[main] INFO com.tencent.app.Main - ✅ Key scheduled for deletion
[main] INFO com.tencent.app.Main - ┌─────────────────────────────────────────────────────
[main] INFO com.tencent.app.Main - │ Key Cleanup Summary
[main] INFO com.tencent.app.Main - ├─────────────────────────────────────────────────────
[main] INFO com.tencent.app.Main - │ Key ID:      83b8885f-c400-11f0-8d87-525400d81ea5
[main] INFO com.tencent.app.Main - │ Deletion:    ⏰ Scheduled
[main] INFO com.tencent.app.Main - │ Window:      7 days (minimum)
[main] INFO com.tencent.app.Main - │ Status:      ✅ Cleanup completed
[main] INFO com.tencent.app.Main - └─────────────────────────────────────────────────────
[main] INFO com.tencent.app.Main - Note: The key will be permanently deleted after 7 days.
[main] INFO com.tencent.app.Main - You can cancel deletion in Tencent Cloud Console if needed.

```