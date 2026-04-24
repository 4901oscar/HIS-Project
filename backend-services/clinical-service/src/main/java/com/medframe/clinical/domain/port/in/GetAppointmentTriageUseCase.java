package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Triage;

import java.util.Optional;

/**
 * Use case interface for retrieving triage records by appointment ID.
 * Supports Requirement 3.1: Query Triage by Appointment.
 */
public interface GetAppointmentTriageUseCase {
    
    /**
     * Retrieves the triage record associated with a specific appointment.
     *
     * @param appointmentId the ID of the appointment
     * @return Optional containing the Triage if found, empty otherwise
     */
    Optional<Triage> getAppointmentTriage(String appointmentId);
}
