package com.medflow.billing.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a recursos sin autorización.
 * Mapea a HTTP 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
