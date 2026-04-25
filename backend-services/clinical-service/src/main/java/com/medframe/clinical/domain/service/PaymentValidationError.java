package com.medframe.clinical.domain.service;

/**
 * Enumeration of payment validation error codes.
 * These codes are used to categorize different types of payment validation failures.
 */
public enum PaymentValidationError {
    
    /**
     * The invoice exists but is in PENDING status (not paid yet).
     */
    PAYMENT_PENDING,
    
    /**
     * The invoice exists but is in CANCELLED status.
     */
    INVOICE_CANCELLED,
    
    /**
     * The invoice was not found in the Billing Service.
     */
    INVOICE_NOT_FOUND,
    
    /**
     * The Billing Service call timed out.
     */
    SERVICE_TIMEOUT,
    
    /**
     * The Billing Service returned an error (5xx).
     */
    SERVICE_ERROR,
    
    /**
     * The invoice has an unknown or unexpected status.
     */
    UNKNOWN_STATUS
}