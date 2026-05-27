package com.medflow.billing.model;

/**
 * Enum representing the possible states of an invoice in the billing system.
 * 
 * State transitions:
 * - PENDING: Initial state when invoice is created
 * - PAID: Invoice has been paid
 * - CANCELLED: Invoice has been cancelled
 * 
 * Valid transitions: PENDING → PAID | CANCELLED
 */
public enum InvoiceStatus {
    /**
     * Initial state - invoice created but not yet paid
     */
    PENDING,
    
    /**
     * Invoice has been paid successfully
     */
    PAID,
    
    /**
     * Invoice has been cancelled
     */
    CANCELLED
}
