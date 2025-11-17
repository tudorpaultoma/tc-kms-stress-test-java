package com.tencent.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class MetadataCredentialClient {
    private static final Logger log = LoggerFactory.getLogger(MetadataCredentialClient.class);
    
    private static final String METADATA_URL = "http://metadata.tencentyun.com/latest/meta-data/cam/security-credentials/";

    public static TemporaryCredential getTmpAkSkByCvmRole(String roleName) throws Exception {
        try {
            String url = METADATA_URL + roleName;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1500))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("metadata service error: {}", response.statusCode());
                throw new RuntimeException("Metadata service returned error: " + response.statusCode());
            }

            String responseBody = response.body();
            log.debug("Raw metadata response: {}", responseBody);

            TemporaryCredential credential = TemporaryCredential.fromJson(responseBody);
            
            if (credential.getSecretId() == null || credential.getSecretKey() == null) {
                throw new RuntimeException("Invalid credential data from metadata service");
            }

            log.info("Successfully obtained temporary credentials for role: {}", roleName);
            return credential;

        } catch (Exception e) {
            log.error("Failed to get temporary credentials from metadata service for role: {}", roleName, e);
            throw new Exception("Failed to get credentials from metadata service: " + e.getMessage(), e);
        }
    }
}