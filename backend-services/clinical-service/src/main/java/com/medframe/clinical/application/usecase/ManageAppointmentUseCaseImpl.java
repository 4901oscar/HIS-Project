package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.model.ScanResult;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import com.medframe.clinical.domain.port.out.ConsultationRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.port.out.PrescriptionRepository;
import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.service.AppointmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ManageAppointmentUseCaseImpl.class);

    private final AppointmentManager appointmentManager;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotCache slotCache;
    private final PermissionValidator permissionValidator;
    private final com.medframe.clinical.domain.service.DoctorAssignmentService doctorAssignmentService;
    private final DoctorRepository doctorRepository;
    private final com.medframe.clinical.domain.port.out.PatientServiceClient patientServiceClient;
    private final com.medframe.clinical.domain.repository.AppointmentStateTransitionRepository stateTransitionRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;

    public ManageAppointmentUseCaseImpl(AppointmentManager appointmentManager,
                                        AppointmentRepository appointmentRepository,
                                        AppointmentSlotCache slotCache,
                                        PermissionValidator permissionValidator,
                                        com.medframe.clinical.domain.service.DoctorAssignmentService doctorAssignmentService,
                                        DoctorRepository doctorRepository,
                                        com.medframe.clinical.domain.port.out.PatientServiceClient patientServiceClient,
                                        com.medframe.clinical.domain.repository.AppointmentStateTransitionRepository stateTransitionRepository,
                                        ConsultationRepository consultationRepository,
                                        PrescriptionRepository prescriptionRepository) {
        this.appointmentManager = appointmentManager;
        this.appointmentRepository = appointmentRepository;
        this.slotCache = slotCache;
        this.permissionValidator = permissionValidator;
        this.doctorAssignmentService = doctorAssignmentService;
        this.doctorRepository = doctorRepository;
        this.patientServiceClient = patientServiceClient;
        this.stateTransitionRepository = stateTransitionRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionRepository = prescriptionRepository;
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
     * Activates a scheduled appointment (transitions from SCHEDULED to VITAL_SIGNS).
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
        
        // Get appointment to capture old status
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        
        // Delegate to domain service
        appointmentManager.activateAppointment(appointmentId);
        
        // Log state transition (now goes directly to VITAL_SIGNS)
        logStateTransition(appointmentId, oldStatus, Appointment.AppointmentStatus.VITAL_SIGNS, 
                          "Cita activada - Transición directa a signos vitales");
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
        
        // Get appointment to capture old status
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        
        // Delegate to domain service
        // The domain service will:
        //    - Retrieve appointment
        //    - Transition to CANCELLED status
        //    - Release slot in Redis
        appointmentManager.cancelAppointment(appointmentId);
        
        // Log state transition
        logStateTransition(appointmentId, oldStatus, Appointment.AppointmentStatus.CANCELLED, 
                          "Cita cancelada");
    }

    /**
     * Confirms payment for a PENDING_PAYMENT appointment and transitions to SCHEDULED.
     * 
     * @param appointmentId The appointment's unique identifier
     * @param invoiceId The invoice ID to validate
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PENDING_PAYMENT state
     * @throws com.medframe.clinical.domain.exception.PaymentValidationException if payment validation fails
     */
    @Override
    public void confirmPayment(String appointmentId, String invoiceId) {
        permissionValidator.requireRole("ADMISSION", "ADMIN", "CASHIER");
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        
        // Update invoice ID
        appointment.setInvoiceId(invoiceId);
        
        // Transition state
        appointment.confirmPayment();
        
        // Persist changes
        appointmentRepository.update(appointment);
        
        // Log state transition
        logStateTransition(appointmentId, oldStatus, appointment.getStatus(), 
                          "Pago confirmado - Invoice: " + invoiceId);
    }


    /**
     * Confirms lab payment for a PENDING_LAB_PAYMENT appointment and transitions to LABORATORY.
     * 
     * @param appointmentId The appointment's unique identifier
     * @param labInvoiceId The lab invoice ID to validate
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PENDING_LAB_PAYMENT state
     * @throws com.medframe.clinical.domain.exception.PaymentValidationException if payment validation fails
     */
    @Override
    public void confirmLabPayment(String appointmentId, String labInvoiceId) {
        permissionValidator.requireRole("ADMISSION", "ADMIN", "CASHIER");
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        
        // Update lab invoice ID
        appointment.setLabInvoiceId(labInvoiceId);
        
        // Transition state
        appointment.confirmLabPayment();
        
        // Persist changes
        appointmentRepository.update(appointment);
        
        // Log state transition
        logStateTransition(appointmentId, oldStatus, appointment.getStatus(), 
                          "Pago de laboratorio confirmado - Invoice: " + labInvoiceId);
    }

    /**
     * Completes lab tests for a LABORATORY appointment and transitions to RE_EVALUATION.
     * 
     * @param appointmentId The appointment's unique identifier
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have LAB role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in LABORATORY state
     */
    @Override
    public void completeLab(String appointmentId) {
        permissionValidator.requireRole("LAB", "ADMIN");
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        
        // Transition state
        appointment.completeLab();
        
        // Persist changes
        appointmentRepository.update(appointment);
        
        // Log state transition
        logStateTransition(appointmentId, oldStatus, appointment.getStatus(), 
                          "Laboratorio completado");
    }

    /**
     * Confirms pharmacy payment for a PENDING_PHARMACY_PAYMENT appointment and transitions to PHARMACY.
     * 
     * @param appointmentId The appointment's unique identifier
     * @param pharmacyInvoiceId The pharmacy invoice ID to validate
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMISSION or ADMIN role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PENDING_PHARMACY_PAYMENT state
     * @throws com.medframe.clinical.domain.exception.PaymentValidationException if payment validation fails
     */
    @Override
    public void confirmPharmacyPayment(String appointmentId, String pharmacyInvoiceId) {
        permissionValidator.requireRole("ADMISSION", "ADMIN", "CASHIER");
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        
        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        
        // Update pharmacy invoice ID
        appointment.setPharmacyInvoiceId(pharmacyInvoiceId);
        
        // Transition state
        appointment.confirmPharmacyPayment();
        
        // Persist changes
        appointmentRepository.update(appointment);
        
        // Log state transition
        logStateTransition(appointmentId, oldStatus, appointment.getStatus(), 
                          "Pago de farmacia confirmado - Invoice: " + pharmacyInvoiceId);
    }

    /**
     * Dispenses medication for a PHARMACY appointment and transitions to COMPLETED.
     * 
     * @param appointmentId The appointment's unique identifier
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have PHARMACY role
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PHARMACY state
     */
    @Override
    public void dispenseMedication(String appointmentId) {
        permissionValidator.requireRole("PHARMACY", "ADMIN");

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));

        Appointment.AppointmentStatus oldStatus = appointment.getStatus();

        appointment.dispenseMedication();
        appointmentRepository.update(appointment);

        logStateTransition(appointmentId, oldStatus, appointment.getStatus(), "Medicamentos dispensados");

        // Marcar prescripciones de esta cita como DISPENSED
        consultationRepository.findByAppointmentId(appointmentId).ifPresent(consultation ->
            prescriptionRepository.findByConsultationId(consultation.getId()).forEach(prescription -> {
                if (prescription.getStatus() == Prescription.PrescriptionStatus.PENDING) {
                    prescription.setStatus(Prescription.PrescriptionStatus.DISPENSED);
                    prescriptionRepository.save(prescription);
                }
            })
        );
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
        permissionValidator.requireRole("ADMISSION", "ADMIN", "CASHIER", "DOCTOR", "LABORATORY", "PHARMACY", "VITAL_SIGNS");
        return appointmentRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Appointment> listAppointmentsWithoutInvoice() {
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        return appointmentRepository.findAppointmentsWithoutInvoice();
    }
    
    @Override
    @Transactional
    public Appointment updateInvoiceId(String appointmentId, String invoiceId) {
        permissionValidator.requireRole("ADMISSION", "ADMIN");
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Cita no encontrada: " + appointmentId));
        
        appointment.setInvoiceId(invoiceId);
        return appointmentRepository.update(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> listMyAppointments() {
        permissionValidator.requireRole("PATIENT");
        String userId = permissionValidator.getUserId();
        
        // Get the patient record associated with this userId
        try {
            com.medframe.clinical.infrastructure.client.dto.PatientDTO patient = 
                (com.medframe.clinical.infrastructure.client.dto.PatientDTO) patientServiceClient.getPatient(userId);
            String patientId = patient.getId();
            return appointmentRepository.findByPatientId(patientId);
        } catch (Exception e) {
            // If patient record not found, return empty list
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> listDoctorAppointments() {
        permissionValidator.requireRole("DOCTOR", "ADMIN");
        String doctorId = permissionValidator.getUserId();
        
        // Filter appointments in CONSULTATION and RE_EVALUATION states
        // Sort RE_EVALUATION first (priority)
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CONSULTATION ||
                            a.getStatus() == Appointment.AppointmentStatus.RE_EVALUATION)
                .sorted((a1, a2) -> {
                    // RE_EVALUATION appointments have priority
                    if (a1.getStatus() == Appointment.AppointmentStatus.RE_EVALUATION &&
                        a2.getStatus() != Appointment.AppointmentStatus.RE_EVALUATION) {
                        return -1; // a1 comes first
                    }
                    if (a2.getStatus() == Appointment.AppointmentStatus.RE_EVALUATION &&
                        a1.getStatus() != Appointment.AppointmentStatus.RE_EVALUATION) {
                        return 1; // a2 comes first
                    }
                    // Same priority, sort by date and time
                    int dateCompare = a1.getAppointmentDate().compareTo(a2.getAppointmentDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    return a1.getAppointmentTime().compareTo(a2.getAppointmentTime());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private String resolvePatientId(String requestedPatientId) {
        // The patientId is already resolved correctly in the controller
        // by calling patient-service to get the real patient ID from auth_user_id
        return requestedPatientId;
    }

    private boolean isPatientRole() {
        String roles = permissionValidator.getUserRoles();
        return roles != null && roles.contains("PATIENT");
    }
    
    /**
     * Registra una transición de estado en la tabla de auditoría.
     * 
     * @param appointmentId ID de la cita
     * @param fromState Estado anterior
     * @param toState Estado nuevo
     * @param notes Notas opcionales sobre la transición
     */
    private void logStateTransition(String appointmentId, 
                                    Appointment.AppointmentStatus fromState,
                                    Appointment.AppointmentStatus toState,
                                    String notes) {
        try {
            String userId = permissionValidator.getUserId();
            
            com.medframe.clinical.domain.model.AppointmentStateTransition transition = 
                com.medframe.clinical.domain.model.AppointmentStateTransition.builder()
                    .appointmentId(appointmentId)
                    .fromState(fromState)
                    .toState(toState)
                    .transitionedBy(userId)
                    .transitionedAt(java.time.LocalDateTime.now())
                    .notes(notes)
                    .build();
            
            stateTransitionRepository.save(transition);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            // Audit logging is important but shouldn't block business operations
            log.error("Error logging state transition: {}", e.getMessage());
        }
    }
}
