package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.port.in.RegisterConsultationUseCase;
import com.medframe.clinical.domain.service.ConsultationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of RegisterConsultationUseCase.
 * 
 * This use case orchestrates the consultation registration process by:
 * 1. Validating that the user has DOCTOR role
 * 2. Delegating business logic to ConsultationManager domain service
 * 3. Automatically completing associated appointment if provided
 * 
 * Requirements: Requirement 4 (Medical Consultation), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
public class RegisterConsultationUseCaseImpl implements RegisterConsultationUseCase {
    
    private final ConsultationManager consultationManager;
    private final PermissionValidator permissionValidator;
    
    public RegisterConsultationUseCaseImpl(ConsultationManager consultationManager,
                                           PermissionValidator permissionValidator) {
        this.consultationManager = consultationManager;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Registers a medical consultation with diagnoses and treatment plan.
     * 
     * @param patientId The patient's unique identifier
     * @param doctorId The doctor performing the consultation
     * @param appointmentId Optional appointment ID to link and complete
     * @param chiefComplaint The main reason for consultation
     * @param symptoms Patient's symptoms description
     * @param primaryDiagnosis Primary diagnosis using CIE-10 code
     * @param secondaryDiagnoses List of secondary diagnoses (CIE-10 codes)
     * @param medicalNotes Doctor's medical notes
     * @param treatmentPlan Prescribed treatment plan
     * @return The registered Consultation with all details
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have DOCTOR role
     */
    @Override
    public Consultation registerConsultation(String patientId, String doctorId,
                                              String appointmentId, String chiefComplaint,
                                              String symptoms, String primaryDiagnosis,
                                              List<String> secondaryDiagnoses,
                                              String medicalNotes, String treatmentPlan,
                                              boolean hasLabOrders, boolean hasPrescription) {
        permissionValidator.requireRole("DOCTOR");

        return consultationManager.registerConsultation(patientId, doctorId,
                                                         appointmentId, chiefComplaint,
                                                         symptoms, primaryDiagnosis,
                                                         secondaryDiagnoses,
                                                         medicalNotes, treatmentPlan,
                                                         hasLabOrders, hasPrescription);
    }
}
