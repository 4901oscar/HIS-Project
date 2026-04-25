package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when no doctors are available for appointment assignment.
 * 
 * <p>This exception is thrown by the doctor assignment algorithm when:
 * <ul>
 *   <li>No doctors work during the requested time slot</li>
 *   <li>All doctors working during the time slot have day-offs</li>
 *   <li>All doctors are inactive</li>
 * </ul>
 * 
 * <p>This is a business rule violation that should be handled by returning
 * an HTTP 422 Unprocessable Entity response to the client.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class NoAvailableDoctorException extends RuntimeException {
    
    /**
     * Constructs a new NoAvailableDoctorException with the specified detail message.
     * 
     * @param message the detail message explaining why no doctors are available
     */
    public NoAvailableDoctorException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new NoAvailableDoctorException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public NoAvailableDoctorException(String message, Throwable cause) {
        super(message, cause);
    }
}
