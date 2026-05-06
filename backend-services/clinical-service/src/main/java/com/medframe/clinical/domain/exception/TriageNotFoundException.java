package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when a triage record is not found.
 * Used specifically for GET /appointments/{id}/triage endpoint.
 */
public class TriageNotFoundException extends RuntimeException {
    
    public TriageNotFoundException(String message) {
        super(message);
    }
}
