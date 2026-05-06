package com.medflow.lab.exception;

/**
 * Exception thrown when an uploaded file has an invalid format.
 */
public class InvalidFileFormatException extends RuntimeException {
    
    public InvalidFileFormatException(String message) {
        super(message);
    }
}
