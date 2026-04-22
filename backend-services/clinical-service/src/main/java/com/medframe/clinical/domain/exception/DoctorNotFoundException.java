package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when a doctor is not found in the system.
 * 
 * <p>This exception is typically thrown when:
 * <ul>
 *   <li>Attempting to retrieve a doctor by ID that doesn't exist</li>
 *   <li>Attempting to mark day-off for a non-existent doctor</li>
 *   <li>Attempting to update or deactivate a non-existent doctor</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class DoctorNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new DoctorNotFoundException with the specified detail message.
     * 
     * @param message the detail message explaining why the doctor was not found
     */
    public DoctorNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DoctorNotFoundException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DoctorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
