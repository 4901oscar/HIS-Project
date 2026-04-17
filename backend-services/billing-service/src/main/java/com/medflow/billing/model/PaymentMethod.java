package com.medflow.billing.model;

/**
 * Enum representing the different payment methods available in the billing system.
 * 
 * Payment methods:
 * - CASH: Cash payment
 * - CARD: Credit or debit card payment
 * - TRANSFER: Bank transfer payment
 */
public enum PaymentMethod {
    /**
     * Cash payment
     */
    CASH,
    
    /**
     * Credit or debit card payment
     */
    CARD,
    
    /**
     * Bank transfer payment
     */
    TRANSFER
}
