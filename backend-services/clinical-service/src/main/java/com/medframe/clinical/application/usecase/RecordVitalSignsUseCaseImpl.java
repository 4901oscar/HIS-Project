package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.domain.port.in.RecordVitalSignsUseCase;
import com.medframe.clinical.domain.service.VitalSignsRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of RecordVitalSignsUseCase.
 * 
 * This use case orchestrates the vital signs recording process by:
 * 1. Validating that the user has VITAL_SIGNS or DOCTOR role
 * 2. Delegating business logic to VitalSignsRecorder domain service
 * 3. The domain service handles BMI calculation, validation, and persistence
 * 
 * Requirements: Requirement 2 (Vital Signs Capture), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
public class RecordVitalSignsUseCaseImpl implements RecordVitalSignsUseCase {
    
    private final VitalSignsRecorder vitalSignsRecorder;
    private final PermissionValidator permissionValidator;
    private final com.medframe.clinical.domain.port.out.AppointmentRepository appointmentRepository;
    private final com.medframe.clinical.domain.repository.AppointmentStateTransitionRepository stateTransitionRepository;
    
    public RecordVitalSignsUseCaseImpl(VitalSignsRecorder vitalSignsRecorder,
                                       PermissionValidator permissionValidator,
                                       com.medframe.clinical.domain.port.out.AppointmentRepository appointmentRepository,
                                       com.medframe.clinical.domain.repository.AppointmentStateTransitionRepository stateTransitionRepository) {
        this.vitalSignsRecorder = vitalSignsRecorder;
        this.permissionValidator = permissionValidator;
        this.appointmentRepository = appointmentRepository;
        this.stateTransitionRepository = stateTransitionRepository;
    }
    
    /**
     * Records vital signs for a patient.
     * 
     * <p><strong>IMPORTANT:</strong> This method does NOT transition the appointment state.
     * The appointment remains in VITAL_SIGNS state after saving vital signs (Step 1).
     * State transition to CONSULTATION happens only when Manchester triage is completed (Step 2)
     * via PerformTriageUseCase.</p>
     * 
     * <p><strong>Two-Step Workflow:</strong></p>
     * <ul>
     *   <li>Step 1: Save vital signs → Appointment stays in VITAL_SIGNS (this method)</li>
     *   <li>Step 2: Save Manchester classification → Appointment transitions to CONSULTATION (PerformTriageUseCase)</li>
     * </ul>
     * 
     * @param appointmentId The appointment's unique identifier
     * @param patientId The patient's unique identifier
     * @param systolic Systolic blood pressure (50-250 mmHg)
     * @param diastolic Diastolic blood pressure (30-150 mmHg)
     * @param heartRate Heart rate (20-250 bpm)
     * @param respiratoryRate Respiratory rate (bpm)
     * @param temperature Body temperature (30-45°C)
     * @param oxygenSaturation Oxygen saturation (0-100%)
     * @param weight Weight in kg
     * @param height Height in cm
     * @param recordedBy User ID of the person recording the vital signs
     * @return The recorded VitalSigns with calculated BMI
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have VITAL_SIGNS or DOCTOR role
     * @throws com.medframe.clinical.domain.exception.InvalidVitalSignsException if values are out of physiological ranges
     * @throws com.medframe.clinical.domain.exception.AppointmentNotFoundException if appointment doesn't exist
     * @throws IllegalStateException if appointment is not in VITAL_SIGNS state
     */
    @Override
    public VitalSigns recordVitalSigns(String appointmentId, String patientId, Integer systolic, Integer diastolic,
                                       Integer heartRate, Integer respiratoryRate,
                                       Double temperature, Integer oxygenSaturation,
                                       Double weight, Double height, String recordedBy) {
        // 1. Validate permissions - VITAL_SIGNS or DOCTOR role can record vital signs
        permissionValidator.requireRole("VITAL_SIGNS", "ADMIN");
        
        // 2. Verify appointment exists and is in VITAL_SIGNS state
        com.medframe.clinical.domain.model.Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new com.medframe.clinical.domain.exception.AppointmentNotFoundException(
                        "Cita no encontrada: " + appointmentId));
        
        if (appointment.getStatus() != com.medframe.clinical.domain.model.Appointment.AppointmentStatus.VITAL_SIGNS) {
            throw new IllegalStateException(
                "Solo se pueden registrar signos vitales para citas en estado VITAL_SIGNS. " +
                "Estado actual: " + appointment.getStatus());
        }
        
        // 3. Record vital signs (no state transition - appointment stays in VITAL_SIGNS)
        VitalSigns vitalSigns = vitalSignsRecorder.recordVitalSigns(appointmentId, patientId, systolic, diastolic,
                                                    heartRate, respiratoryRate,
                                                    temperature, oxygenSaturation,
                                                    weight, height, recordedBy);
        
        // Note: Appointment remains in VITAL_SIGNS state
        // State transition to CONSULTATION will happen when Manchester triage is completed
        
        return vitalSigns;
    }
    
    /**
     * Retrieves the latest vital signs for a patient.
     * 
     * @param patientId The patient's unique identifier
     * @return The most recent VitalSigns record
     * @throws com.medframe.clinical.domain.exception.VitalSignsNotFoundException if no vital signs found
     */
    @Override
    @Transactional(readOnly = true)
    public VitalSigns getLatestVitalSigns(String patientId) {
        // No permission validation needed for reading vital signs
        // (access control is handled at the controller/API level)
        return vitalSignsRecorder.getLatestVitalSigns(patientId);
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
                                    com.medframe.clinical.domain.model.Appointment.AppointmentStatus fromState,
                                    com.medframe.clinical.domain.model.Appointment.AppointmentStatus toState,
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
            System.err.println("Error logging state transition: " + e.getMessage());
        }
    }
}
