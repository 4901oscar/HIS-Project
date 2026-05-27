package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.port.in.GetMedicalHistoryUseCase;
import com.medframe.clinical.domain.service.MedicalHistoryAggregator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetMedicalHistoryUseCase.
 * 
 * This use case orchestrates the medical history retrieval process by:
 * 1. Validating that the user has DOCTOR or PATIENT role
 * 2. Delegating business logic to MedicalHistoryAggregator domain service
 * 3. Enforcing access control (patients can only view their own history)
 * 
 * Requirements: Requirement 7 (Medical History), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class GetMedicalHistoryUseCaseImpl implements GetMedicalHistoryUseCase {
    
    private final MedicalHistoryAggregator medicalHistoryAggregator;
    private final PermissionValidator permissionValidator;
    
    public GetMedicalHistoryUseCaseImpl(MedicalHistoryAggregator medicalHistoryAggregator,
                                         PermissionValidator permissionValidator) {
        this.medicalHistoryAggregator = medicalHistoryAggregator;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Retrieves the complete medical history for a patient.
     * 
     * Access control:
     * - DOCTOR role: Can access any patient's history
     * - PATIENT role: Can only access their own history
     * 
     * @param patientId The patient's unique identifier
     * @param requestingUserId The ID of the user requesting the history
     * @param userRole The role of the requesting user (DOCTOR or PATIENT)
     * @return MedicalHistory containing patient demographics, consultations, vital signs, prescriptions, and lab orders
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have DOCTOR or PATIENT role,
     *         or if a PATIENT tries to access another patient's history
     */
    @Override
    public MedicalHistory getMedicalHistory(String patientId, String requestingUserId, String userRole) {
        // 1. Validate permissions - only DOCTOR or PATIENT roles can access medical history
        permissionValidator.requireRole("DOCTOR", "PATIENT");
        
        // 2. Delegate to domain service for business logic and access control
        return medicalHistoryAggregator.getMedicalHistory(patientId, requestingUserId, userRole);
    }
}
