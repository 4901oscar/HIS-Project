package com.medframe.clinical.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when trying to perform an operation on an appointment
 * that is not in the correct status.
 * This exception is mapped to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAppointmentStatusException extends RuntimeException {
    
    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
}