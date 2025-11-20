package com.tencent.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

/**
 * Client for retrieving temporary security credentials from Tencent Cloud CVM metadata service.
 * 
 * This class accesses the CVM instance metadata endpoint to obtain temporary credentials
 * associated with a CVM role. These credentials include temporary AccessKey ID, Secret Key,
 * and a session token that can be used to make authenticated API calls to Tencent Cloud services.
 */
public class MetadataCredentialClient {
    private static final Logger log = LoggerFactory.getLogger(MetadataCredentialClient.class);
    
    /** Tencent Cloud metadata service endpoint for CAM (Cloud Access Management) credentials */
    private static final String METADATA_URL = "http://metadata.tencentyun.com/latest/meta-data/cam/security-credentials/";

    /**
     * Retrieves temporary credentials from CVM metadata service for a specified role.
     * 
     * @param roleName The name of the CVM role to retrieve credentials for
     * @return TemporaryCredential object containing SecretId, SecretKey, and Token
     * @throws Exception if the metadata service is unreachable, returns an error, 
     *                   or the credential data is invalid
     */
    public static TemporaryCredential getTmpAkSkByCvmRole(String roleName) throws Exception {
        try {
            String url = METADATA_URL + roleName;

            // Create HTTP client with short timeouts (metadata service should be fast and local)
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1500))
                    .GET()
                    .build();

            // Execute request to metadata service
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("metadata service error: {}", response.statusCode());
                throw new RuntimeException("Metadata service returned error: " + response.statusCode());
            }

            String responseBody = response.body();
            log.debug("Raw metadata response: {}", responseBody);

            // Parse JSON response into TemporaryCredential object
            TemporaryCredential credential = TemporaryCredential.fromJson(responseBody);
            
            // Validate that essential credential fields are present
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