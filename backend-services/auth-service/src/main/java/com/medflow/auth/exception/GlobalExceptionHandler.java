package com.medflow.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 * 
 * <p>This handler provides consistent error responses for:
 * <ul>
 *   <li>Authentication failures (401 Unauthorized)</li>
 *   <li>Resource not found (404 Not Found)</li>
 *   <li>Account disabled (403 Forbidden)</li>
 *   <li>Generic errors (500 Internal Server Error)</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles TooManyAttemptsException.
     * Maps to 429 Too Many Requests.
     */
    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyAttemptsException(TooManyAttemptsException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Too Many Requests", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
    
    /**
     * Handles generic RuntimeException.
     * Maps to 401 Unauthorized for authentication-related errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred: {}", ex.getMessage());
        
        String message = ex.getMessage();
        
        // Map specific error messages to appropriate status codes
        if (message != null) {
            if (message.contains("Invalid credentials") || 
                message.contains("Invalid token") || 
                message.contains("blacklisted")) {
                ErrorResponse error = new ErrorResponse("Unauthorized", message);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            if (message.contains("not found")) {
                ErrorResponse error = new ErrorResponse("Not Found", message);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            if (message.contains("disabled")) {
                ErrorResponse error = new ErrorResponse("Forbidden", message);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }
        
        // Default to 500 Internal Server Error
        ErrorResponse error = new ErrorResponse("Internal Server Error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
