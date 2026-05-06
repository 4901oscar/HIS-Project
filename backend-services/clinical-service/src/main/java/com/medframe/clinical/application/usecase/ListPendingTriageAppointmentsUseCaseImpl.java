package com.medframe.clinical.application.usecase;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.in.ListPendingTriageAppointmentsUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ListPendingTriageAppointmentsUseCase.
 * 
 * This use case retrieves all active appointments that do not have an associated
 * triage record. It provides a simple query interface for triage staff to view
 * appointments awaiting triage assessment.
 * 
 * Requirements: 4.1, 4.3, 4.4 (List Appointments Pending Triage)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ListPendingTriageAppointmentsUseCaseImpl implements ListPendingTriageAppointmentsUseCase {
    
    private final AppointmentRepository appointmentRepository;
    
    public ListPendingTriageAppointmentsUseCaseImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * Lists all active appointments that are waiting for triage.
     * 
     * This method queries for appointments in ACTIVE status that do not have
     * an associated triage record, using an optimized NOT EXISTS query pattern.
     * 
     * @return List of appointments in ACTIVE status without associated triage records
     */
    @Override
    public List<Appointment> listPendingTriageAppointments() {
        return appointmentRepository.findPendingTriage();
    }
}
