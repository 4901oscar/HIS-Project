package com.medflow.gateway.security;

/**
 * Custom exception for JWT validation errors.
 * Thrown when JWT token is invalid, expired, or malformed.
 */
public class JwtException extends RuntimeException {
    
    public JwtException(String message) {
        super(message);
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
