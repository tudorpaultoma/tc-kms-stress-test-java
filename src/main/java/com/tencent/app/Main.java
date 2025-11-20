package com.tencent.app;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.kms.v20190118.KmsClient;
import com.tencentcloudapi.kms.v20190118.models.*;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Tencent Cloud KMS Stress Testing Tool.
 * 
 * This application performs concurrent encryption and decryption operations
 * against Tencent Cloud KMS to measure performance, throughput (RPS), and
 * identify bottlenecks under various load conditions.
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    /**
     * Retrieves configuration value from system properties with fallback to default.
     * System properties can be set via -D flags when running the JAR.
     */
    private static String getConfig(String key, String defaultValue) {
        String value = System.getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    // Test configuration parameters (can be overridden via system properties)
    private static final String REGION = getConfig("region", "ap-guangzhou");
    private static final String ROLE = getConfig("role", "CVM-KMS-full");
    private static final String CMK_ID = getConfig("cmkId", ""); // If empty, a temporary key will be created
    private static final int DURATION_SECONDS = Integer.parseInt(getConfig("duration", "60"));
    private static final int CONCURRENCY = Integer.parseInt(getConfig("concurrency", "20"));
    private static final double ENCRYPT_RATIO = Double.parseDouble(getConfig("encryptRatio", "0.5")); // 50% encryption operations
    private static final double DECRYPT_RATIO = Double.parseDouble(getConfig("decryptRatio", "0.5")); // 50% decryption operations
    
    // Atomic counters for thread-safe statistics tracking across all worker threads
    private static final AtomicLong encryptSuccessCount = new AtomicLong(0);
    private static final AtomicLong decryptSuccessCount = new AtomicLong(0);
    private static final AtomicLong totalSuccessCount = new AtomicLong(0);
    private static final AtomicLong errorCount = new AtomicLong(0);
    private static final AtomicLong totalLatency = new AtomicLong(0);
    private static final AtomicInteger activeThreads = new AtomicInteger(0);
    
    // Statistics tracking for periodic reporting (stores last reported values)
    private static final AtomicLong lastEncryptSuccessCount = new AtomicLong(0);
    private static final AtomicLong lastDecryptSuccessCount = new AtomicLong(0);
    private static final AtomicLong lastTotalSuccessCount = new AtomicLong(0);
    private static final AtomicLong lastErrorCount = new AtomicLong(0);
    private static volatile long lastStatsTime = System.currentTimeMillis();
    
    // Peak performance tracking
    private static final AtomicDouble peakTotalRps = new AtomicDouble(0);
    private static final AtomicDouble peakEncryptRps = new AtomicDouble(0);
    private static final AtomicDouble peakDecryptRps = new AtomicDouble(0);

    // Shared resources for encryption/decryption workflow
    private static volatile String testKeyId = null; // The CMK ID being tested
    private static final ConcurrentLinkedQueue<String> ciphertextQueue = new ConcurrentLinkedQueue<>(); // Queue of ciphertexts for decryption

    /**
     * Main entry point for the KMS stress testing application.
     * 
     * Workflow:
     * 1. Initialize KMS client with temporary credentials from CVM role
     * 2. Create temporary test key (or use provided CMK_ID)
     * 3. Start worker threads for concurrent operations
     * 4. Run stress test for specified duration
     * 5. Report final statistics
     * 6. Clean up temporary resources
     */
    public static void main(String[] args) {
        log.info("Starting KMS Stress Test");
        log.info("Region: {}, Role: {}", REGION, ROLE);
        log.info("Duration: {}s, Concurrency: {}, Encrypt/Decrypt Ratio: {}/{}", 
                 DURATION_SECONDS, CONCURRENCY, ENCRYPT_RATIO, DECRYPT_RATIO);
        
        KmsClient client = null;
        String temporaryKeyId = null;
        
        try {
            // Initialize KMS client with temporary credentials from CVM metadata service
            client = initializeKmsClient();
            
            // Create a temporary CMK for testing if none was provided in configuration
            if (CMK_ID == null || CMK_ID.trim().isEmpty()) {
                log.info("No CMK_ID provided, creating temporary key for testing...");
                temporaryKeyId = createTemporaryKey(client);
                testKeyId = temporaryKeyId;
            } else {
                testKeyId = CMK_ID;
                log.info("Using provided CMK: {}", CMK_ID);
            }
            
            // Create fixed-size thread pool for concurrent KMS operations
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CONCURRENCY, // core pool size
                CONCURRENCY, // maximum pool size  
                60L, // keep alive time
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "kms-worker-" + threadNumber.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                }
            );

            executor.allowCoreThreadTimeOut(true);

            // Initialize timing for statistics calculation
            lastStatsTime = System.currentTimeMillis();

            // Start periodic statistics reporter (reports every 5 seconds)
            ScheduledExecutorService statsReporter = Executors.newScheduledThreadPool(1);
            statsReporter.scheduleAtFixedRate(() -> reportStats(), 5, 5, TimeUnit.SECONDS);

            // Latch to synchronize start of all worker threads
            CountDownLatch startLatch = new CountDownLatch(1);
            
            // Create and submit worker threads to executor
            List<KmsWorker> workers = new ArrayList<>(CONCURRENCY);
            List<Future<?>> futures = new ArrayList<>(CONCURRENCY);

            for (int i = 0; i < CONCURRENCY; i++) {
                KmsWorker worker = new KmsWorker(client, i, startLatch);
                workers.add(worker);
                Future<?> future = executor.submit(worker);
                futures.add(future);
            }

            // Wait for all worker threads to initialize and be ready
            int maxWaitTime = 10000; // Maximum 10 seconds to wait for thread startup
            int waitInterval = 3000;  // Check thread status every 3 seconds
            int waited = 0;
            while (activeThreads.get() < CONCURRENCY && waited < maxWaitTime) {
                Thread.sleep(waitInterval);
                waited += waitInterval;
                log.info("Waiting for threads to start: {}/{}", activeThreads.get(), CONCURRENCY);
            }

            if (activeThreads.get() < CONCURRENCY) {
                log.warn("Only {}/{} threads started successfully", activeThreads.get(), CONCURRENCY);
            } else {
                log.info("All {} threads started successfully", CONCURRENCY);
            }
            
            log.info("All workers started. Beginning stress test in 3 seconds...");
            Thread.sleep(3000);
            
            // Release the latch to start all workers simultaneously
            long startTime = System.currentTimeMillis();
            startLatch.countDown();
            
            // Run stress test for the configured duration
            Thread.sleep(DURATION_SECONDS * 1000L);
            
            // Gracefully stop all worker threads
            log.info("Stress test duration completed. Stopping workers...");
            
            // Signal each worker to stop its operation loop
            for (KmsWorker worker : workers) {
                worker.stop();
            }
            
            // Shutdown executor and wait for threads to complete
            executor.shutdown();
            
            // Wait for threads to finish current operations (max 10 seconds)
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Forcing executor shutdown...");
                executor.shutdownNow();
            }
            
            // Stop the statistics reporter thread
            statsReporter.shutdown();
            if (!statsReporter.awaitTermination(5, TimeUnit.SECONDS)) {
                statsReporter.shutdownNow();
            }
            
            long endTime = System.currentTimeMillis();
            
            // Print comprehensive final statistics report
            reportFinalStats(startTime, endTime);
            
        } catch (Exception e) {
            log.error("Stress test failed: {}", e.getMessage(), e);
        } finally {
            // Clean up: Delete temporary key if one was created for this test
            if (client != null && temporaryKeyId != null) {
                try {
                    log.info("Cleaning up temporary key: {}", temporaryKeyId);
                    disableAndScheduleKeyDeletion(client, temporaryKeyId);
                } catch (Exception e) {
                    log.warn("Failed to cleanup temporary key: {}", e.getMessage());
                }
            }
            
            // Allow time for cleanup to complete before exiting
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        }
    }
    
    /**
     * Initializes KMS client using temporary credentials from CVM metadata service.
     * Uses the internal Tencent Cloud endpoint for optimal performance.
     * 
     * @return Configured KmsClient instance
     * @throws Exception if credential retrieval or client initialization fails
     */
    private static KmsClient initializeKmsClient() throws Exception {
        TemporaryCredential temporaryCredential = MetadataCredentialClient.getTmpAkSkByCvmRole(ROLE);
        Credential cred = new Credential(
            temporaryCredential.getSecretId(), 
            temporaryCredential.getSecretKey(), 
            temporaryCredential.getToken()
        );

        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("kms.internal.tencentcloudapi.com"); // Use internal endpoint for lower latency
        // Configure timeouts for performance optimization
        httpProfile.setReadTimeout(30 * 1000);  // 30 seconds read timeout
        httpProfile.setWriteTimeout(30 * 1000); // 30 seconds write timeout
        httpProfile.setConnTimeout(30 * 1000);  // 30 seconds connection timeout

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        return new KmsClient(cred, REGION, clientProfile);
    }
    
    /**
     * Creates a temporary KMS Customer Master Key for testing purposes.
     * The key will be automatically disabled and scheduled for deletion after the test completes.
     * 
     * @param client The initialized KMS client
     * @return The KeyId of the newly created CMK
     * @throws Exception if key creation fails
     */
    private static String createTemporaryKey(KmsClient client) throws Exception {
        log.info("Creating temporary KMS key...");
        
        CreateKeyRequest req = new CreateKeyRequest();
        String keyAlias = "stress-test-key-" + System.currentTimeMillis();
        req.setAlias(keyAlias);
        req.setDescription("Temporary key for KMS stress testing - will be deleted after test");
        req.setKeyUsage("ENCRYPT_DECRYPT");
        
        log.info("Key creation parameters:");
        log.info("  Alias: {}", keyAlias);
        log.info("  Region: {}", REGION);
        log.info("  Key Usage: ENCRYPT_DECRYPT");
        
        long startTime = System.currentTimeMillis();
        CreateKeyResponse resp = client.CreateKey(req);
        long creationTime = System.currentTimeMillis() - startTime;
        
        String keyId = resp.getKeyId();
        
        log.info("✅ Key created successfully!");
        log.info("┌─────────────────────────────────────────────────────");
        log.info("│ Key ID:      {}", keyId);
        log.info("│ Alias:       {}", keyAlias);
        log.info("│ Region:      {}", REGION);
        log.info("│ Type:        Customer Master Key (CMK)");
        log.info("│ Usage:       ENCRYPT_DECRYPT");
        log.info("│ Creation:    {} ms", creationTime);
        log.info("│ Status:      ✅ Enabled");
        log.info("└─────────────────────────────────────────────────────");
        log.info("This key will be automatically disabled and scheduled for");
        log.info("deletion after the stress test completes.");
        
        return keyId;
    }
    
    /**
     * Disables a KMS key and schedules it for deletion after the minimum 7-day period.
     * This is used to clean up temporary keys created during testing.
     * 
     * @param client The initialized KMS client
     * @param keyId The ID of the key to disable and delete
     * @throws Exception if key disabling or deletion scheduling fails
     */
    private static void disableAndScheduleKeyDeletion(KmsClient client, String keyId) throws Exception {
        try {
            log.info("Initiating key cleanup for: {}", keyId);
            
            // Disable the key first
            log.info("Step 1/2: Disabling key...");
            DisableKeyRequest disableReq = new DisableKeyRequest();
            disableReq.setKeyId(keyId);
            client.DisableKey(disableReq);
            log.info("✅ Key disabled successfully");
            
            // Schedule key for deletion (7 days later)
            log.info("Step 2/2: Scheduling key for deletion...");
            ScheduleKeyDeletionRequest deleteReq = new ScheduleKeyDeletionRequest();
            deleteReq.setKeyId(keyId);
            deleteReq.setPendingWindowInDays(7L); // Minimum 7 days
            ScheduleKeyDeletionResponse deleteResp = client.ScheduleKeyDeletion(deleteReq);
            log.info("✅ Key scheduled for deletion");
            
            log.info("┌─────────────────────────────────────────────────────");
            log.info("│ Key Cleanup Summary");
            log.info("├─────────────────────────────────────────────────────");
            log.info("│ Key ID:      {}", keyId);
            log.info("│ Deletion:    ⏰ Scheduled");
            log.info("│ Window:      7 days (minimum)");
            log.info("│ Status:      ✅ Cleanup completed");
            log.info("└─────────────────────────────────────────────────────");
            log.info("Note: The key will be permanently deleted after 7 days.");
            log.info("You can cancel deletion in Tencent Cloud Console if needed.");
            
        } catch (Exception e) {
            log.error("❌ Key cleanup failed: {}", e.getMessage());
            log.warn("Manual cleanup required for key: {}", keyId);
            throw e;
        }
    }
    
    /**
     * Reports periodic statistics during the stress test.
     * Called every 5 seconds to show current RPS and operation counts.
     * Tracks peak RPS values for final reporting.
     */
    private static void reportStats() {
        long currentTime = System.currentTimeMillis();
        long currentEncryptSuccess = encryptSuccessCount.get();
        long currentDecryptSuccess = decryptSuccessCount.get();
        long currentTotalSuccess = totalSuccessCount.get();
        long currentErrors = errorCount.get();
        
        long lastEncrypt = lastEncryptSuccessCount.getAndSet(currentEncryptSuccess);
        long lastDecrypt = lastDecryptSuccessCount.getAndSet(currentDecryptSuccess);
        long lastTotal = lastTotalSuccessCount.getAndSet(currentTotalSuccess);
        long lastErrors = lastErrorCount.getAndSet(currentErrors);
        
        long elapsedMs = currentTime - lastStatsTime;
        double elapsedSeconds = elapsedMs / 1000.0;
        
        long encryptInPeriod = currentEncryptSuccess - lastEncrypt;
        long decryptInPeriod = currentDecryptSuccess - lastDecrypt;
        long totalInPeriod = currentTotalSuccess - lastTotal;
        long errorsInPeriod = currentErrors - lastErrors;
        
        double currentEncryptRps = elapsedSeconds > 0 ? (double) encryptInPeriod / elapsedSeconds : 0;
        double currentDecryptRps = elapsedSeconds > 0 ? (double) decryptInPeriod / elapsedSeconds : 0;
        double currentTotalRps = elapsedSeconds > 0 ? (double) totalInPeriod / elapsedSeconds : 0;
        
        // Track peak RPS
        if (currentEncryptRps > peakEncryptRps.get()) {
            peakEncryptRps.set(currentEncryptRps);
        }
        if (currentDecryptRps > peakDecryptRps.get()) {
            peakDecryptRps.set(currentDecryptRps);
        }
        if (currentTotalRps > peakTotalRps.get()) {
            peakTotalRps.set(currentTotalRps);
        }
        
        lastStatsTime = currentTime;
        
        String statsMessage = String.format(
            "STATS - Encrypt: %d (%.2fRPS) | Decrypt: %d (%.2fRPS) | Total: %d (%.2fRPS) | Errors: %d | Active: %d | Ratio: %.1f/%.1f",
            currentEncryptSuccess, currentEncryptRps,
            currentDecryptSuccess, currentDecryptRps,
            currentTotalSuccess, currentTotalRps,
            currentErrors, activeThreads.get(),
            ENCRYPT_RATIO, DECRYPT_RATIO
        );
        log.info(statsMessage);
    }
    
    /**
     * Generates and displays comprehensive final statistics report after stress test completion.
     * Includes total operations, RPS metrics, peak performance, latency, and success rates.
     * 
     * @param startTime Test start time in milliseconds
     * @param endTime Test end time in milliseconds
     */
    private static void reportFinalStats(long startTime, long endTime) {
        long totalEncrypt = encryptSuccessCount.get();
        long totalDecrypt = decryptSuccessCount.get();
        long totalSuccess = totalSuccessCount.get();
        long totalErrors = errorCount.get();
        long totalRequests = totalSuccess + totalErrors;
        long durationMs = endTime - startTime;
        double durationSeconds = durationMs / 1000.0;
        
        double overallRps = totalRequests > 0 ? (double) totalRequests / durationSeconds : 0;
        double encryptRps = totalEncrypt > 0 ? (double) totalEncrypt / durationSeconds : 0;
        double decryptRps = totalDecrypt > 0 ? (double) totalDecrypt / durationSeconds : 0;
        double avgLatency = totalSuccess > 0 ? (double) totalLatency.get() / totalSuccess : 0;
        double successRate = totalRequests > 0 ? (double) totalSuccess / totalRequests * 100 : 0;
        
        // Calculate actual operation ratio
        double actualEncryptRatio = totalSuccess > 0 ? (double) totalEncrypt / totalSuccess : 0;
        double actualDecryptRatio = totalSuccess > 0 ? (double) totalDecrypt / totalSuccess : 0;
        
        String finalReport = String.format(
            "=========================================%n" +
            "STRESS TEST COMPLETE%n" +
            "Test Key: %s%n" +
            "Operation Ratio: %.1f/%.1f (Config) | %.1f/%.1f (Actual)%n" +
            "=========================================%n" +
            "Duration: %.2f seconds%n" +
            "Total Requests: %d%n" +
            "Successful: %d (%.2f%%)%n" +
            "Errors: %d%n" +
            "Encryption Operations: %d (%.2f RPS)%n" +
            "Decryption Operations: %d (%.2f RPS)%n" +
            "Overall RPS: %.2f%n" +
            "Peak Total RPS: %.2f%n" +
            "Peak Encrypt RPS: %.2f%n" +
            "Peak Decrypt RPS: %.2f%n" +
            "Average Latency: %.2f ms%n" +
            "=========================================",
            testKeyId, ENCRYPT_RATIO, DECRYPT_RATIO, actualEncryptRatio, actualDecryptRatio,
            durationSeconds, totalRequests, totalSuccess, successRate, totalErrors,
            totalEncrypt, encryptRps, totalDecrypt, decryptRps,
            overallRps, peakTotalRps.get(), peakEncryptRps.get(), peakDecryptRps.get(), avgLatency
        );
        
        log.info(finalReport);
    }
    
    // Encryption operation
    private static String performEncryption(KmsClient client, String plaintext) throws Exception {
        String plaintextBase64 = Base64.getEncoder().encodeToString(
            plaintext.getBytes(StandardCharsets.UTF_8)
        );

        EncryptRequest req = new EncryptRequest();
        req.setKeyId(testKeyId);
        req.setPlaintext(plaintextBase64);

        EncryptResponse resp = client.Encrypt(req);
        return resp.getCiphertextBlob();
    }
    
    /**
     * Performs a single KMS decryption operation.
     * Decodes the Base64-encoded plaintext after decryption.
     * 
     * @param client The initialized KMS client
     * @param ciphertext The ciphertext blob to decrypt
     * @return The decrypted plaintext string
     * @throws Exception if decryption operation fails
     */
    private static String performDecryption(KmsClient client, String ciphertext) throws Exception {
        DecryptRequest req = new DecryptRequest();
        req.setCiphertextBlob(ciphertext);
        
        DecryptResponse resp = client.Decrypt(req);
        String decryptedBase64 = resp.getPlaintext();
        return new String(Base64.getDecoder().decode(decryptedBase64), StandardCharsets.UTF_8);
    }
    
    /**
     * Worker thread that performs concurrent KMS operations.
     * Each worker runs continuously until signaled to stop, alternating between
     * encryption and decryption operations based on configured ratios.
     */
    static class KmsWorker implements Runnable {
        private final KmsClient client;
        private final int workerId;
        private final CountDownLatch startLatch;
        private volatile boolean running = true;
        
        public KmsWorker(KmsClient client, int workerId, CountDownLatch startLatch) {
            this.client = client;
            this.workerId = workerId;
            this.startLatch = startLatch;
        }
        
        @Override
        public void run() {
            try {
                // Wait for the start signal from main thread (ensures synchronized start)
                startLatch.await();
                activeThreads.incrementAndGet();
                
                log.debug("Worker {} started", workerId);
                
                // Main operation loop - continues until stopped or interrupted
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        long startTime = System.currentTimeMillis();
                        
                        // Determine operation type based on configured ratios
                        // If no ciphertexts are available for decryption, default to encryption
                        double randomValue = Math.random();
                        if (randomValue < ENCRYPT_RATIO || ciphertextQueue.isEmpty()) {
                            // Perform encryption
                            performEncryptionOperation(startTime);
                        } else {
                            // Perform decryption
                            performDecryptionOperation(startTime);
                        }
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        // Log errors periodically to avoid flooding logs
                        if (errorCount.get() % 100 == 0) {
                            log.debug("Worker {} operation failed: {}", workerId, e.getMessage());
                        }
                        
                        // Brief pause on error to prevent tight error loops
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Worker {} crashed: {}", workerId, e.getMessage());
            } finally {
                activeThreads.decrementAndGet();
                log.debug("Worker {} stopped", workerId);
            }
        }
        
        /**
         * Performs an encryption operation and adds the ciphertext to the queue for later decryption.
         * 
         * @param startTime Operation start time for latency calculation
         * @throws Exception if encryption fails
         */
        private void performEncryptionOperation(long startTime) throws Exception {
            String plaintext = "quick brown fox-5-1700234567890" + workerId + "-" + System.currentTimeMillis();
            String ciphertext = performEncryption(client, plaintext);
            
            // Add to ciphertext queue for future decryption operations (limit queue size to prevent memory issues)
            if (ciphertextQueue.size() < 1000) {
                ciphertextQueue.offer(ciphertext);
            }
            
            long latency = System.currentTimeMillis() - startTime;
            encryptSuccessCount.incrementAndGet();
            totalSuccessCount.incrementAndGet();
            totalLatency.addAndGet(latency);
        }
        
        private void performDecryptionOperation(long startTime) throws Exception {
            String ciphertextToDecrypt = ciphertextQueue.poll();
            
            // If no ciphertext is available in queue, perform encryption instead
            if (ciphertextToDecrypt == null) {
                performEncryptionOperation(startTime);
                return;
            }
            
            String decryptedText = performDecryption(client, ciphertextToDecrypt);
            
            long latency = System.currentTimeMillis() - startTime;
            decryptSuccessCount.incrementAndGet();
            totalSuccessCount.incrementAndGet();
            totalLatency.addAndGet(latency);
        }
        
        /**
         * Signals this worker to stop processing operations.
         */
        public void stop() {
            running = false;
        }
    }
    
    /**
     * Thread-safe atomic double implementation for tracking decimal values.
     * Java doesn't provide AtomicDouble natively, so we use AtomicLong with bit conversion.
     */
    static class AtomicDouble {
        private AtomicLong value;
        
        public AtomicDouble() {
            this(0.0);
        }
        
        public AtomicDouble(double initialValue) {
            value = new AtomicLong(Double.doubleToLongBits(initialValue));
        }
        
        public double get() {
            return Double.longBitsToDouble(value.get());
        }
        
        public void set(double newValue) {
            value.set(Double.doubleToLongBits(newValue));
        }
        
        public boolean compareAndSet(double expect, double update) {
            return value.compareAndSet(Double.doubleToLongBits(expect), Double.doubleToLongBits(update));
        }
    }
}