package com.tencent.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemporaryCredential {

    @JsonProperty("TmpSecretId")
    private String secretId;
    
    @JsonProperty("TmpSecretKey")
    private String secretKey;
    
    @JsonProperty("Token")
    private String token;
    
    @JsonProperty("ExpiredTime")
    private String expiredTime;
    
    @JsonProperty("Expiration")
    private String expiration;

    public TemporaryCredential() {}

    public static TemporaryCredential fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, TemporaryCredential.class);
        } catch (Exception e) {
            throw new RuntimeException("Parse TemporaryCredential JSON failed: " + e.getMessage(), e);
        }
    }

    // Manual getters and setters
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