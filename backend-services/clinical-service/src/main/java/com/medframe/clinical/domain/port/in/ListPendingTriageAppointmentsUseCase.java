package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Appointment;

import java.util.List;

/**
 * Use case interface for listing appointments pending triage.
 * Supports Requirement 4.1: List Appointments Pending Triage.
 * 
 * Returns all ACTIVE appointments that do not have an associated triage record.
 */
public interface ListPendingTriageAppointmentsUseCase {
    
    /**
     * Lists all active appointments that are waiting for triage.
     * 
     * @return List of appointments in ACTIVE status without associated triage records
     */
    List<Appointment> listPendingTriageAppointments();
}
