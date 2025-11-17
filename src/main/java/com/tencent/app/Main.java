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

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    // Configuration from system properties
    private static String getConfig(String key, String defaultValue) {
        String value = System.getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    private static final String REGION = getConfig("region", "ap-guangzhou");
    private static final String ROLE = getConfig("role", "CVM-KMS-full");
    private static final String CMK_ID = getConfig("cmkId", "bd8d20ed-c390-11f0-82be-5254003da5b7");
    private static final int DURATION_SECONDS = Integer.parseInt(getConfig("duration", "60"));
    private static final int CONCURRENCY = Integer.parseInt(getConfig("concurrency", "20"));
    
    // Statistics
    private static final AtomicLong successCount = new AtomicLong(0);
    private static final AtomicLong errorCount = new AtomicLong(0);
    private static final AtomicLong totalLatency = new AtomicLong(0);
    private static final AtomicInteger activeThreads = new AtomicInteger(0);
    
    // Improved statistics tracking
    private static final AtomicLong lastSuccessCount = new AtomicLong(0);
    private static final AtomicLong lastErrorCount = new AtomicLong(0);
    private static volatile long lastStatsTime = System.nanoTime(); // FIXED: nanoTime not currentanoTime
    private static final AtomicDouble peakRps = new AtomicDouble(0);

    public static void main(String[] args) {
        log.info("Starting KMS Stress Test");
        log.info("Region: {}, Role: {}, CMK: {}", REGION, ROLE, CMK_ID);
        log.info("Duration: {}s, Concurrency: {}", DURATION_SECONDS, CONCURRENCY);
        
        try {
            // Initialize KMS client once
            KmsClient client = initializeKmsClient();
            
            // Create thread pool for concurrent requests
            ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
            CountDownLatch startLatch = new CountDownLatch(1);
            
            // Initialize stats time
            lastStatsTime = System.nanoTime();
            
            // Statistics reporter
            ScheduledExecutorService statsReporter = Executors.newScheduledThreadPool(1);
            statsReporter.scheduleAtFixedRate(() -> reportStats(), 5, 5, TimeUnit.SECONDS);
            
            // Start worker threads
            List<KmsWorker> workers = new ArrayList<>();
            for (int i = 0; i < CONCURRENCY; i++) {
                KmsWorker worker = new KmsWorker(client, i, startLatch);
                workers.add(worker);
                executor.submit(worker);
            }
            
            log.info("All workers started. Beginning stress test in 3 seconds...");
            Thread.sleep(3000);
            
            // Start all workers simultaneously
            long startTime = System.currentTimeMillis();
            startLatch.countDown();
            
            // Wait for specified duration
            Thread.sleep(DURATION_SECONDS * 1000L);
            
            // Stop workers gracefully
            log.info("Stress test duration completed. Stopping workers...");
            
            // Signal all workers to stop
            for (KmsWorker worker : workers) {
                worker.stop();
            }
            
            // Shutdown executor gracefully
            executor.shutdown();
            
            // Wait for threads to finish current operations
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Forcing executor shutdown...");
                executor.shutdownNow();
            }
            
            // Stop stats reporter
            statsReporter.shutdown();
            if (!statsReporter.awaitTermination(5, TimeUnit.SECONDS)) {
                statsReporter.shutdownNow();
            }
            
            long endTime = System.currentTimeMillis();
            
            // Final report
            reportFinalStats(startTime, endTime);
            
        } catch (Exception e) {
            log.error("Stress test failed: {}", e.getMessage(), e);
        } finally {
            // Clean shutdown
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        }
    }
    
    private static KmsClient initializeKmsClient() throws Exception {
        TemporaryCredential temporaryCredential = MetadataCredentialClient.getTmpAkSkByCvmRole(ROLE);
        Credential cred = new Credential(
            temporaryCredential.getSecretId(), 
            temporaryCredential.getSecretKey(), 
            temporaryCredential.getToken()
        );

        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("kms.tencentcloudapi.com");
        // Optimize for performance
        httpProfile.setReadTimeout(30 * 1000);  // 30 seconds
        httpProfile.setWriteTimeout(30 * 1000); // 30 seconds
        httpProfile.setConnTimeout(30 * 1000);  // 30 seconds

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        return new KmsClient(cred, REGION, clientProfile);
    }
    
    private static void reportStats() {
        long currentTime = System.nanoTime();
        long currentSuccess = successCount.get();
        long currentErrors = errorCount.get();
        
        long lastSuccess = lastSuccessCount.getAndSet(currentSuccess);
        long lastErrors = lastErrorCount.getAndSet(currentErrors);
        
        long elapsedNanos = currentTime - lastStatsTime;
        lastStatsTime = currentTime;
        
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        long requestsInPeriod = (currentSuccess - lastSuccess) + (currentErrors - lastErrors);
        double currentRps = elapsedSeconds > 0 ? requestsInPeriod / elapsedSeconds : 0;
        
        // Track peak RPS
        if (currentRps > peakRps.get()) {
            peakRps.set(currentRps);
        }
        
        String statsMessage = String.format(
            "STATS - Success: %d, Errors: %d, Current RPS: %.2f, Peak RPS: %.2f, Active Threads: %d",
            currentSuccess, currentErrors, currentRps, peakRps.get(), activeThreads.get()
        );
        log.info(statsMessage);
    }
    
    private static void reportFinalStats(long startTime, long endTime) {
        long totalSuccess = successCount.get();
        long totalErrors = errorCount.get();
        long totalRequests = totalSuccess + totalErrors;
        long durationMs = endTime - startTime;
        double overallRps = totalRequests > 0 ? (double) totalRequests / (durationMs / 1000.0) : 0;
        double avgLatency = totalSuccess > 0 ? (double) totalLatency.get() / totalSuccess : 0;
        double successRate = totalRequests > 0 ? (double) totalSuccess / totalRequests * 100 : 0;
        
        String finalReport = String.format(
            "=========================================%n" +
            "STRESS TEST COMPLETE%n" +
            "=========================================%n" +
            "Duration: %d ms%n" +
            "Total Requests: %d%n" +
            "Successful: %d%n" +
            "Errors: %d%n" +
            "Success Rate: %.2f%%%n" +
            "Overall RPS: %.2f%n" +
            "Peak RPS: %.2f%n" +
            "Average Latency: %.2f ms%n" +
            "=========================================",
            durationMs, totalRequests, totalSuccess, totalErrors, successRate, 
            overallRps, peakRps.get(), avgLatency
        );
        
        log.info(finalReport);
    }
    
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
                // Wait for start signal
                startLatch.await();
                activeThreads.incrementAndGet();
                
                log.debug("Worker {} started", workerId);
                
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        long startTime = System.currentTimeMillis();
                        
                        // Perform encryption operation
                        String plaintext = "StressTestData-" + workerId + "-" + System.currentTimeMillis();
                        String plaintextBase64 = Base64.getEncoder().encodeToString(
                            plaintext.getBytes(StandardCharsets.UTF_8)
                        );

                        EncryptRequest req = new EncryptRequest();
                        req.setKeyId(CMK_ID);
                        req.setPlaintext(plaintextBase64);

                        EncryptResponse resp = client.Encrypt(req);
                        
                        long latency = System.currentTimeMillis() - startTime;
                        
                        // Update statistics
                        successCount.incrementAndGet();
                        totalLatency.addAndGet(latency);
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        if (errorCount.get() % 100 == 0) { // Log every 100th error to avoid spam
                            log.debug("Worker {} request failed: {}", workerId, e.getMessage());
                        }
                    }
                }
                
            } catch (InterruptedException e) {
                // Thread was interrupted, exit gracefully
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Worker {} crashed: {}", workerId, e.getMessage());
            } finally {
                activeThreads.decrementAndGet();
                log.debug("Worker {} stopped", workerId);
            }
        }
        
        public void stop() {
            running = false;
        }
    }
    
    // Simple AtomicDouble implementation since Java doesn't have one
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