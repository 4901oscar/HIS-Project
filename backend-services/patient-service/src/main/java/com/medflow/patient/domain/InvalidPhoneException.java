package com.medflow.patient.domain;

/**
 * Exception thrown when phone validation fails.
 * Phone must be exactly 8 numeric digits (Guatemala format).
 */
public class InvalidPhoneException extends RuntimeException {
    
    public InvalidPhoneException(String message) {
        super(message);
    }
    
    public InvalidPhoneException(String message, Throwable cause) {
        super(message, cause);
    }
}
