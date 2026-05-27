package com.medflow.patient.domain;

/**
 * Exception thrown when email validation fails.
 * Email must be in a valid format with @ and domain.
 */
public class InvalidEmailException extends RuntimeException {
    
    public InvalidEmailException(String message) {
        super(message);
    }
    
    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
