package com.medframe.clinical.infrastructure.client;

/**
 * Exception thrown when there's an error communicating with the Billing Service.
 * This is a general exception for any Billing Service related errors.
 */
public class BillingServiceException extends RuntimeException {
    
    public BillingServiceException(String message) {
        super(message);
    }
    
    public BillingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}