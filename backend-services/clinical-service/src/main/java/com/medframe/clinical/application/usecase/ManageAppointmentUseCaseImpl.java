package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.service.AppointmentManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementation of ManageAppointmentUseCase.
 * 
 * This use case orchestrates appointment management operations by:
 * 1. Validating that the user has ADMISSION or ADMIN role
 * 2. Delegating business logic to AppointmentManager domain service
 * 3. The domain service handles slot availability, Redis cache, and persistence
 * 
 * Requirements: Requirement 3 (Appointment Management), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
public class ManageAppointmentUseCaseImpl implements ManageAppointmentUseCase {
    
    private final AppointmentManager appointmentManager;
    private final AppointmentRepository appointmentRepository;
    private final PermissionValidator permissionValidator;
    
    public ManageAppointmentUseCaseImpl(AppointmentManager appointmentManager,
                                        AppointmentRepository appointmentRepository,
                                        PermissionValidator permissionValidator) {
        this.appointmentManager = appointmentManager;
        this.appointmentRepository = appointmentRepository;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Finds available appointment slots for a doctor on a specific date.
     * 
     * @param doctorId The doctor's unique identifier
     * @param date The date to check for available slots
     * @return List of available time slots (08:00 - 16:30, 30-minute intervals)
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     */
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> findAvailableSlots(String doctorId, LocalDate date) {
        // Validate permissions - ADMISSION or ADMIN role can view available slots
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        
        // Delegate to domain service
        return appointmentManager.findAvailableSlots(doctorId, date);
    }
    
    /**
     * Creates a new appointment for a patient with a specific doctor.
     * 
     * @param patientId The patient's unique identifier
     * @param doctorId The doctor's unique identifier
     * @param date The appointment date
     * @param time The appointment time
     * @param notes Optional notes for the appointment
     * @param createdBy User ID of the person creating the appointment
     * @return The created Appointment with SCHEDULED status
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws com.medframe.clinical.domain.exception.SlotNotAvailableException if the slot is already occupied
     * @throws com.medframe.clinical.domain.exception.PatientNotFoundException if patient doesn't exist
     */
    @Override
    public Appointment createAppointment(String patientId, String doctorId,
                                         LocalDate date, LocalTime time,
                                         String notes, String createdBy) {
        // Validate permissions - ADMISSION or ADMIN role can create appointments
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        
        // Delegate to domain service
        // The domain service will:
        //    - Validate patient exists (via PatientServiceClient)
        //    - Reserve slot atomically in Redis
        //    - Create and persist appointment
        //    - Rollback Redis on failure
        return appointmentManager.createAppointment(patientId, doctorId,
                                                     date, time, notes, createdBy);
    }
    
    /**
     * Activates a scheduled appointment (transitions from SCHEDULED to ACTIVE).
     * 
     * @param appointmentId The appointment's unique identifier
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws IllegalStateException if appointment is not in SCHEDULED status
     */
    @Override
    public void activateAppointment(String appointmentId) {
        // Validate permissions - ADMISSION or ADMIN role can activate appointments
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        
        // Delegate to domain service
        appointmentManager.activateAppointment(appointmentId);
    }
    
    /**
     * Cancels an appointment and releases the slot in Redis cache.
     * 
     * @param appointmentId The appointment's unique identifier
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws IllegalStateException if appointment is already COMPLETED
     */
    @Override
    public void cancelAppointment(String appointmentId) {
        // Validate permissions - ADMISSION or ADMIN role can cancel appointments
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        
        // Delegate to domain service
        // The domain service will:
        //    - Retrieve appointment
        //    - Transition to CANCELLED status
        //    - Release slot in Redis
        appointmentManager.cancelAppointment(appointmentId);
    }
}
