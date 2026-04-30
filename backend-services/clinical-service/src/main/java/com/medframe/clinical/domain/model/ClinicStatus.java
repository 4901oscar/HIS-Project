package com.medframe.clinical.domain.model;

/**
 * Enumeration representing the operational status of a clinic.
 * 
 * <p>This enum defines the possible states a clinic can be in throughout its lifecycle:
 * <ul>
 *   <li><b>ACTIVE</b>: Clinic is operational and can be used for doctor associations</li>
 *   <li><b>INACTIVE</b>: Clinic is temporarily not operational but can be reactivated</li>
 *   <li><b>DELETED</b>: Clinic has been soft-deleted and should not be used (preserves historical data)</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public enum ClinicStatus {
    /**
     * Clinic is active and operational.
     * This is the default status for newly created clinics.
     */
    ACTIVE,
    
    /**
     * Clinic is inactive but can be reactivated.
     * Used for temporarily closed or suspended clinics.
     */
    INACTIVE,
    
    /**
     * Clinic has been soft-deleted.
     * The record is preserved in the database for historical purposes
     * but should not be used for new operations.
     */
    DELETED
}
