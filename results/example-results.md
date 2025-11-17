root@VM-13-37-centos:~# ./stress-kms.sh 
JAR_PATH         = /root/kms1/target/kms1-1.0-SNAPSHOT-jar-with-dependencies.jar
REGION           = eu-frankfurt
ROLE             = CVM-KMS-full
CMK_ID           = bd8d20ed-c390-11f0-82be-5254003da5b7
DURATION_SECONDS = 60
CONCURRENCY      = 55
----------------------------------------
[main] INFO com.tencent.app.Main - Starting KMS Stress Test
[main] INFO com.tencent.app.Main - Region: eu-frankfurt, Role: CVM-KMS-full, CMK: bd8d20ed-c390-11f0-82be-5254003da5b7
[main] INFO com.tencent.app.Main - Duration: 60s, Concurrency: 55
[main] INFO com.tencent.app.MetadataCredentialClient - Successfully obtained temporary credentials for role: CVM-KMS-full
[main] INFO com.tencent.app.Main - All workers started. Beginning stress test in 3 seconds...
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 1367, Errors: 0, Current RPS: 273.12, Peak RPS: 273.12, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 12380, Errors: 0, Current RPS: 2203.51, Peak RPS: 2203.51, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 24910, Errors: 0, Current RPS: 2506.37, Peak RPS: 2506.37, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 37624, Errors: 0, Current RPS: 2543.01, Peak RPS: 2543.01, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 50775, Errors: 0, Current RPS: 2630.19, Peak RPS: 2630.19, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 64502, Errors: 0, Current RPS: 2745.42, Peak RPS: 2745.42, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 77877, Errors: 0, Current RPS: 2675.00, Peak RPS: 2745.42, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 90496, Errors: 0, Current RPS: 2523.80, Peak RPS: 2745.42, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 103899, Errors: 0, Current RPS: 2680.60, Peak RPS: 2745.42, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 116900, Errors: 0, Current RPS: 2600.18, Peak RPS: 2745.42, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 131133, Errors: 0, Current RPS: 2846.61, Peak RPS: 2846.61, Active Threads: 55
[pool-2-thread-1] INFO com.tencent.app.Main - STATS - Success: 144608, Errors: 0, Current RPS: 2695.02, Peak RPS: 2846.61, Active Threads: 55
[main] INFO com.tencent.app.Main - Stress test duration completed. Stopping workers...
[main] INFO com.tencent.app.Main - =========================================
STRESS TEST COMPLETE
=========================================
Duration: 60025 ms
Total Requests: 152678
Successful: 152678
Errors: 0
Success Rate: 100.00%
Overall RPS: 2543.57
Peak RPS: 2846.61
Average Latency: 21.61 ms
=========================================