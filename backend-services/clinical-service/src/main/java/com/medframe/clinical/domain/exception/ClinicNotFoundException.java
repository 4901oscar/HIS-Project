package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when a clinic is not found in the system.
 * 
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>Attempting to retrieve a clinic by ID that doesn't exist</li>
 *   <li>Attempting to update a non-existent clinic</li>
 *   <li>Attempting to delete a non-existent clinic</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class ClinicNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new ClinicNotFoundException with the specified detail message.
     * 
     * @param message the detail message explaining why the clinic was not found
     */
    public ClinicNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ClinicNotFoundException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ClinicNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
