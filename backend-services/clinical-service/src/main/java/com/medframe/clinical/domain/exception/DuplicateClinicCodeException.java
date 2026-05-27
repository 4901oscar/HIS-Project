package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when attempting to create or update a clinic with a code that already exists.
 * 
 * <p>This exception enforces the business rule that clinic codes must be unique across the system.
 * It is typically thrown when:
 * <ul>
 *   <li>Creating a new clinic with a code that already exists</li>
 *   <li>Updating a clinic's code to one that is already in use by another clinic</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class DuplicateClinicCodeException extends RuntimeException {
    
    /**
     * Constructs a new DuplicateClinicCodeException with the specified detail message.
     * 
     * @param message the detail message explaining the duplicate code violation
     */
    public DuplicateClinicCodeException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DuplicateClinicCodeException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DuplicateClinicCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
