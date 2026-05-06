package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when a doctor's shift duration is invalid.
 * 
 * <p>This exception is thrown when:
 * <ul>
 *   <li>Shift duration is not exactly 8 hours</li>
 *   <li>Shift start or end times are null</li>
 *   <li>Shift end time is before shift start time</li>
 * </ul>
 * 
 * <p>This enforces the business rule that all doctor shifts must be exactly 8 hours long.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class InvalidShiftDurationException extends RuntimeException {
    
    /**
     * Constructs a new InvalidShiftDurationException with the specified detail message.
     * 
     * @param message the detail message explaining why the shift duration is invalid
     */
    public InvalidShiftDurationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new InvalidShiftDurationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidShiftDurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
