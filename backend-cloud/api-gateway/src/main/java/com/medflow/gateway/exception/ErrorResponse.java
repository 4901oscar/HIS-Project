package com.medflow.gateway.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Standard error response format for all API Gateway errors.
 * Provides consistent error structure across all endpoints.
 */
public class ErrorResponse {
    
    private String error;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String path;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String error, String message) {
        this();
        this.error = error;
        this.message = message;
    }
    
    public ErrorResponse(String error, String message, String path) {
        this(error, message);
        this.path = path;
    }
    
    // Builder pattern for flexible error response creation
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String error;
        private String message;
        private String path;
        
        public Builder error(String error) {
            this.error = error;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(error, message, path);
        }
    }
    
    // Getters and Setters
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
