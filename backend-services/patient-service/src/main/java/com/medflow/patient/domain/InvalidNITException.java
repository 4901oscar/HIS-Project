package com.medflow.patient.domain;

/**
 * Exception thrown when NIT validation fails.
 * NIT must be "C/F" or 1-8 numeric digits.
 */
public class InvalidNITException extends RuntimeException {
    
    public InvalidNITException(String message) {
        super(message);
    }
    
    public InvalidNITException(String message, Throwable cause) {
        super(message, cause);
    }
}
