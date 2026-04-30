package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when clinic data fails validation rules.
 * 
 * <p>This exception is thrown when clinic data violates business validation rules such as:
 * <ul>
 *   <li>Clinic code contains non-numeric characters</li>
 *   <li>Clinic name contains non-alphanumeric characters</li>
 *   <li>Clinic description contains non-alphanumeric characters</li>
 *   <li>Required fields are missing or empty</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class InvalidClinicDataException extends RuntimeException {
    
    /**
     * Constructs a new InvalidClinicDataException with the specified detail message.
     * 
     * @param message the detail message explaining the validation failure
     */
    public InvalidClinicDataException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new InvalidClinicDataException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidClinicDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
