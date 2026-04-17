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
    
    public RecordVitalSignsUseCaseImpl(VitalSignsRecorder vitalSignsRecorder,
                                       PermissionValidator permissionValidator) {
        this.vitalSignsRecorder = vitalSignsRecorder;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Records vital signs for a patient.
     * 
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
     */
    @Override
    public VitalSigns recordVitalSigns(String patientId, Integer systolic, Integer diastolic,
                                       Integer heartRate, Integer respiratoryRate,
                                       Double temperature, Integer oxygenSaturation,
                                       Double weight, Double height, String recordedBy) {
        // 1. Validate permissions - VITAL_SIGNS or DOCTOR role can record vital signs
        permissionValidator.requireRole("VITAL_SIGNS", "DOCTOR");
        
        // 2. Delegate to domain service for business logic
        // The domain service will:
        //    - Create VitalSigns entity
        //    - Calculate BMI
        //    - Validate physiological ranges
        //    - Persist via repository
        return vitalSignsRecorder.recordVitalSigns(patientId, systolic, diastolic,
                                                    heartRate, respiratoryRate,
                                                    temperature, oxygenSaturation,
                                                    weight, height, recordedBy);
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
}
