package com.medflow.lab.exception;

/**
 * Exception thrown when an uploaded file exceeds the maximum allowed size.
 */
public class FileSizeExceededException extends RuntimeException {
    
    public FileSizeExceededException(String message) {
        super(message);
    }
}
