package com.medflow.patient.domain;

/**
 * Exception thrown when DPI validation fails.
 * DPI must be exactly 13 numeric digits.
 */
public class InvalidDPIException extends RuntimeException {
    
    public InvalidDPIException(String message) {
        super(message);
    }
    
    public InvalidDPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
