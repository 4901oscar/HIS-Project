package com.medflow.billing.model;

/**
 * Enum representing the different types of charges that can be applied to an invoice.
 * 
 * Charge types:
 * - CONSULTATION: Medical consultation services
 * - LABORATORY: Laboratory tests and diagnostics
 * - MEDICATION: Pharmacy and medication charges
 * - OTHER: Miscellaneous charges not covered by other categories
 */
public enum ChargeType {
    /**
     * Medical consultation services
     */
    CONSULTATION,
    
    /**
     * Laboratory tests and diagnostics
     */
    LABORATORY,
    
    /**
     * Pharmacy and medication charges
     */
    MEDICATION,
    
    /**
     * Miscellaneous charges
     */
    OTHER
}
