package com.medflow.auth.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * 
 * <p>This exception is thrown when a user exceeds the maximum number
 * of login attempts allowed per time window (5 attempts per minute).
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
public class TooManyAttemptsException extends RuntimeException {
    
    public TooManyAttemptsException(String message) {
        super(message);
    }
}
