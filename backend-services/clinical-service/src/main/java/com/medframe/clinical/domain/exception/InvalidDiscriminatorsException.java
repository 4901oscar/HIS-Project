package com.medframe.clinical.domain.exception;

public class InvalidDiscriminatorsException extends RuntimeException {
    
    public InvalidDiscriminatorsException(String message) {
        super(message);
    }
    
    public InvalidDiscriminatorsException(String message, Throwable cause) {
        super(message, cause);
    }
}
