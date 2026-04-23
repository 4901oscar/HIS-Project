package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.model.ScanResult;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.service.AppointmentManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
    private final AppointmentSlotCache slotCache;
    private final PermissionValidator permissionValidator;
    private final com.medframe.clinical.domain.service.DoctorAssignmentService doctorAssignmentService;
    private final DoctorRepository doctorRepository;

    public ManageAppointmentUseCaseImpl(AppointmentManager appointmentManager,
                                        AppointmentRepository appointmentRepository,
                                        AppointmentSlotCache slotCache,
                                        PermissionValidator permissionValidator,
                                        com.medframe.clinical.domain.service.DoctorAssignmentService doctorAssignmentService,
                                        DoctorRepository doctorRepository) {
        this.appointmentManager = appointmentManager;
        this.appointmentRepository = appointmentRepository;
        this.slotCache = slotCache;
        this.permissionValidator = permissionValidator;
        this.doctorAssignmentService = doctorAssignmentService;
        this.doctorRepository = doctorRepository;
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
        permissionValidator.requireRole("ADMISSION", "ADMIN", "PATIENT");
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
        permissionValidator.requireRole("ADMISSION", "ADMIN", "PATIENT");
        String resolvedPatientId = resolvePatientId(patientId);
        boolean isPatient = isPatientRole();
        return appointmentManager.createAppointment(resolvedPatientId, doctorId,
                                                     date, time, notes, createdBy, isPatient);
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
    
    /**
     * Creates an appointment with automatic doctor assignment.
     * 
     * <p>This method uses the doctor assignment algorithm to automatically select
     * the doctor with the lowest workload for the requested date and time.
     * 
     * @param patientId The patient's unique identifier
     * @param date The appointment date
     * @param time The appointment time
     * @param notes Optional notes for the appointment
     * @param createdBy User ID of the person creating the appointment
     * @return The created Appointment with SCHEDULED status and assigned doctor
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws com.medframe.clinical.domain.exception.NoAvailableDoctorException if no doctors are available
     * @throws com.medframe.clinical.domain.exception.SlotNotAvailableException if the slot is already occupied
     * @throws com.medframe.clinical.domain.exception.PatientNotFoundException if patient doesn't exist
     */
    @Override
    public Appointment createAppointmentWithAutoAssignment(String patientId,
                                                           LocalDate date, LocalTime time,
                                                           String notes, String createdBy) {
        permissionValidator.requireRole("ADMISSION", "ADMIN", "PATIENT");
        String resolvedPatientId = resolvePatientId(patientId);
        String assignedDoctorId = doctorAssignmentService.assignDoctor(date, time);
        // PATIENT role: JWT already proves identity; patient may not have a patient-service record yet
        boolean isPatient = isPatientRole();
        return appointmentManager.createAppointment(resolvedPatientId, assignedDoctorId,
                                                     date, time, notes, createdBy, isPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> findAvailableSlotsForDate(LocalDate date) {
        return findAvailableSlotsForDate(date, null);
    }

    public List<LocalTime> findAvailableSlotsForDate(LocalDate date, String sessionId) {
        List<Doctor> activeDoctors = doctorRepository.findAllActive();
        TreeSet<LocalTime> available = new TreeSet<>();
        for (Doctor doctor : activeDoctors) {
            available.addAll(appointmentManager.findAvailableSlots(
                doctor.getId(), date, doctor.getShiftStart(), doctor.getShiftEnd()
            ));
        }
        // Remove slots held by other sessions
        String sid = (sessionId != null && !sessionId.isBlank()) ? sessionId : "";
        available.removeAll(slotCache.getHeldByOthers(sid, date));
        return new ArrayList<>(available);
    }

    @Override
    public boolean holdSlot(String sessionId, LocalDate date, LocalTime time) {
        return slotCache.holdTimeSlot(sessionId, date, time);
    }

    @Override
    public void releaseHold(String sessionId) {
        slotCache.releaseTimeSlotHold(sessionId);
    }

    /**
     * Scans a QR code and validates/activates the appointment based on time window.
     * No permission validation required - this is typically called by reception systems.
     * 
     * @param appointmentId The appointment ID from QR code
     * @param scanTime The time when QR was scanned
     * @return ScanResult with status (EARLY, ACTIVE, MISSED) and message
     * @throws AppointmentNotFoundException if appointment doesn't exist
     */
    @Override
    public ScanResult scanAndActivateAppointment(String appointmentId, LocalDateTime scanTime) {
        // No permission validation - QR scanning is open to reception systems
        return appointmentManager.validateAndActivateAppointment(appointmentId, scanTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> listAll() {
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> listMyAppointments() {
        permissionValidator.requireRole("PATIENT");
        String patientId = permissionValidator.getUserId();
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> listDoctorAppointments() {
        permissionValidator.requireRole("DOCTOR", "ADMIN");
        String doctorId = permissionValidator.getUserId();
        return appointmentRepository.findByDoctorId(doctorId);
    }

    private String resolvePatientId(String requestedPatientId) {
        String roles = permissionValidator.getUserRoles();
        if (roles != null && roles.contains("PATIENT")) {
            return permissionValidator.getUserId();
        }
        return requestedPatientId;
    }

    private boolean isPatientRole() {
        String roles = permissionValidator.getUserRoles();
        return roles != null && roles.contains("PATIENT");
    }
}
