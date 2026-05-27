package com.medframe.clinical.infrastructure.client;

/**
 * Exception thrown when a call to the Billing Service times out.
 * This is a specific type of BillingServiceException for timeout scenarios.
 */
public class BillingServiceTimeoutException extends BillingServiceException {
    
    public BillingServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}