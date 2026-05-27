package com.medframe.clinical.domain.exception;

/**
 * Exception thrown when attempting to access an appointment that is locked by another user.
 * This typically occurs when trying to start vital signs capture for an appointment
 * that is already in VITAL_SIGNS state (locked by another triage staff member).
 */
public class AppointmentLockedException extends RuntimeException {
    
    private final String lockedBy;
    
    public AppointmentLockedException(String message, String lockedBy) {
        super(message);
        this.lockedBy = lockedBy;
    }
    
    public String getLockedBy() {
        return lockedBy;
    }
}
