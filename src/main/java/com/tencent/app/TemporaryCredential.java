package com.tencent.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * Data model for temporary security credentials from Tencent Cloud CVM metadata service.
 * 
 * This class represents the temporary credentials that are associated with a CVM role,
 * including the temporary AccessKey ID, Secret Key, session token, and expiration information.
 * These credentials are rotated automatically by Tencent Cloud and are valid for a limited time.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemporaryCredential {

    /** Temporary AccessKey ID (SecretId) for API authentication */
    @JsonProperty("TmpSecretId")
    private String secretId;
    
    /** Temporary Secret Key for signing API requests */
    @JsonProperty("TmpSecretKey")
    private String secretKey;
    
    /** Session token required for temporary credential authentication */
    @JsonProperty("Token")
    private String token;
    
    /** Unix timestamp when credentials expire (in seconds) */
    @JsonProperty("ExpiredTime")
    private String expiredTime;
    
    /** ISO 8601 formatted expiration datetime */
    @JsonProperty("Expiration")
    private String expiration;

    /** Default constructor for Jackson deserialization */
    public TemporaryCredential() {}

    /**
     * Parses JSON string from metadata service into TemporaryCredential object.
     * 
     * @param json JSON response from metadata service
     * @return TemporaryCredential instance with populated fields
     * @throws RuntimeException if JSON parsing fails
     */
    public static TemporaryCredential fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, TemporaryCredential.class);
        } catch (Exception e) {
            throw new RuntimeException("Parse TemporaryCredential JSON failed: " + e.getMessage(), e);
        }
    }

    // Getters and setters for credential fields
    public String getSecretId() { return secretId; }
    public String getSecretKey() { return secretKey; }
    public String getToken() { return token; }
    public String getExpiredTime() { return expiredTime; }
    public String getExpiration() { return expiration; }

    public void setSecretId(String secretId) { this.secretId = secretId; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public void setToken(String token) { this.token = token; }
    public void setExpiredTime(String expiredTime) { this.expiredTime = expiredTime; }
    public void setExpiration(String expiration) { this.expiration = expiration; }

    /**
     * Returns string representation with sensitive data redacted.
     * Secret key is hidden and token presence is indicated without showing the actual value.
     */
    @Override
    public String toString() {
        return "TemporaryCredential{" +
                "secretId='" + secretId + '\'' +
                ", secretKey='" + "[HIDDEN]" + '\'' +
                ", token='" + (token != null ? "[TOKEN_PRESENT]" : "null") + '\'' +
                ", expiredTime='" + expiredTime + '\'' +
                ", expiration='" + expiration + '\'' +
                '}';
    }
}